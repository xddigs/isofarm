package com.isofarm.wrld;

import com.isofarm.data.CropType;
import com.isofarm.data.Season;
import com.isofarm.data.Shop;
import com.isofarm.data.SoundGroup;
import com.isofarm.entity.*;
import com.isofarm.graphics.*;
import com.isofarm.gui.GUI;
import com.isofarm.gui.UIManager;
import com.isofarm.input.*;
import com.isofarm.item.Item;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.service.*;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

@SuppressWarnings("all")
public class GameMaster {
    private static final Logger log = LoggerFactory.getLogger(GameMaster.class);
    private final long windowHandle;
    private final World world;
    private final Sun sun;
    private final Moon moon;
    private final CelestialLighting celestialLighting;
    private final SoundService soundService;
    private final UIManager uiManager;
    private final CropService cropService;
    private final TimeService timeService;
    private final CommandService commandService;
    private final ParticleEngine particles;
    private final RainEngine rainEngine;
    private final ToastService toastService;
    private final CommandRegistry commandRegistry;
    private final ItemRegistry itemRegistry;
    private final List<Entity> entities;
    private ShadowMap shadowMap;
    private GameInteraction gameInteraction;
    private WeatherService weatherService;
    private ResourceManager resourceManager;
    private ChunkManager chunkManager;
    private GameRenderer gameRenderer;
    private ItemRenderer itemRenderer;
    private GameUIService gameUIservice;
    private Framebuffer maskFbo;
    private Framebuffer sceneFbo;
    private Framebuffer blurFbo;
    private OrthographicCamera orthoCamera;
    private Camera camera;
    private CameraController cameraController;
    private OrthographicCameraController orthoCameraController;
    private StepController stepController;
    private float windowWidth;
    private float windowHeight;
    private Player player;
    private Shop shop;
    private boolean isChatOpen = false;
    private boolean isInventoryOpen = false;
    private boolean isHUDShown = true;

    private float genDelta;

    public GameMaster(long windowHandle, UIManager uiManager) {
        this.windowHandle = windowHandle;
        this.uiManager = uiManager;

        this.windowWidth = K.Window.DEFAULT_WIDTH;
        this.windowHeight = K.Window.DEFAULT_HEIGHT;

        this.world = new World();
        this.sun = new Sun("Sun");
        this.moon = new Moon("Moon");
        this.celestialLighting = new CelestialLighting(sun, moon);

        this.soundService = new SoundService();
        this.particles = new ParticleEngine();
        this.cropService = new CropService(world, particles);
        this.timeService = new TimeService();
        this.commandRegistry = new CommandRegistry();
        this.toastService = new ToastService();
        this.commandService = new CommandService(commandRegistry);
        this.itemRegistry = new ItemRegistry();
        this.rainEngine = new RainEngine();
        this.entities = new LinkedList<>();
    }

    public void loadResources(Consumer<Float> progressCallback) {
        float totalSteps = 10.0f;
        int currentStep = 0;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.shadowMap = new ShadowMap((int) Settings.getShadowMapSize(),
                (int) Settings.getShadowMapSize());
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.maskFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.sceneFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.blurFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.resourceManager = new ResourceManager();
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.chunkManager = new ChunkManager(world);
        this.gameRenderer = new GameRenderer();
        this.itemRenderer = new ItemRenderer();
        this.shop = new Shop();
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.camera = new Camera(windowWidth, windowHeight, Settings.getRenderDistance());
        this.orthoCamera = new OrthographicCamera(windowWidth, windowHeight, Settings.getRenderDistance());

        this.camera.setPosition(0.0f, 0.0f, 0.0f);
        this.cameraController = new CameraController(camera);
        this.orthoCameraController = new OrthographicCameraController(orthoCamera);
        this.stepController = new StepController();
        this.weatherService = new WeatherService(rainEngine, camera);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.recenter();
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.player = new Player(null, world, this);
        player.setSoundService(soundService);
        addEntity(player);
        shop.setPlayer(player);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        Library.initItems(itemRegistry, player);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        Library.initCommands(genDelta, this);
        notifyProgress(progressCallback, ++currentStep / totalSteps);
    }

    private void notifyProgress(Consumer<Float> callback, float progress) {
        if (callback != null) {
            callback.accept(progress);
        }
    }

    public void initUI() {
        gameUIservice = new GameUIService(windowHandle, this,
                uiManager, resourceManager.getSeedIcons(), resourceManager.getCropIcons(),
                resourceManager.getBlockIcons(), resourceManager.getToolIcons(),
                resourceManager.getMaterialIcons(),
                resourceManager.getInventoryIcons());

        gameInteraction = new GameInteraction(this,
                resourceManager.getBlocksTexture());

        gameUIservice.setPlayer(this.player);
        commandService.setGameUIService(gameUIservice);
        gameUIservice.setShop(shop);
        player.setName(gameUIservice.getPlayerName());
    }

    public void spawn() {
        chunkManager.updateLoadedChunks(0, 0);
        GridPos spawn = world.getHighestY(0.5f, 0.5f);
        float spawnY = spawn.y() + 1.8f;
        player.setPosition(0.5f, spawnY, 0.5f);
        camera.setPosition(0.5f, spawnY, 0.5f);
        orthoCamera.setPosition(0.5f, spawnY + 10.0f, 0.5f);
    }

    public Sun getSun() {
        return sun;
    }

    public Moon getMoon() {
        return moon;
    }

    public CelestialLighting getCelestialLighting() {
        return celestialLighting;
    }

    public ShadowMap getShadowMap() {
        return shadowMap;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public GameRenderer getGameRenderer() {
        return gameRenderer;
    }

    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    public ParticleEngine getParticles() {
        return particles;
    }

    public Framebuffer getMaskFbo() {
        return maskFbo;
    }

    public Framebuffer getSceneFbo() {
        return sceneFbo;
    }

    public RainEngine getRainEngine() {
        return rainEngine;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public SoundService getSoundService() {
        return soundService;
    }

    public CropService getCropService() {
        return cropService;
    }

    public ToastService getToastService() {
        return toastService;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public GameUIService getGameUIService() {
        return gameUIservice;
    }

    public WeatherService getWeatherService() {
        return weatherService;
    }

    public TimeService getTimeService() {
        return timeService;
    }

    public CommandService getCommandService() {
        return commandService;
    }

    public float getWindowWidth() {
        return windowWidth;
    }

    public float getWindowHeight() {
        return windowHeight;
    }

    public Camera getCamera() {
        return camera;
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    public OrthographicCamera getOrthoCamera() {
        return orthoCamera;
    }

    public OrthographicCameraController getOrthoCameraController() {
        return orthoCameraController;
    }

    public GameInteraction getGameInteraction() {
        return gameInteraction;
    }

    public Framebuffer getBlurFbo() {
        return blurFbo;
    }

    public int getLastPlayerChunkX() {
        return chunkManager.getLastPlayerChunkX();
    }

    public void setLastPlayerChunkX(int lastPlayerChunkX) {
        chunkManager.setLastPlayerChunkX(lastPlayerChunkX);
    }

    public int getLastPlayerChunkZ() {
        return chunkManager.getLastPlayerChunkZ();
    }

    public void setLastPlayerChunkZ(int lastPlayerChunkZ) {
        chunkManager.setLastPlayerChunkZ(lastPlayerChunkZ);
    }

    public boolean isChatOpen() {
        return isChatOpen;
    }

    public void setChatOpen(boolean isChatOpen) {
        this.isChatOpen = isChatOpen;
    }

    public boolean isInventoryOpen() {
        return isInventoryOpen;
    }

    public void setInventoryOpen(boolean isInventoryOpen) {
        this.isInventoryOpen = isInventoryOpen;
        if (!isInventoryOpen) {
            this.cameraController.release(this);
        }
    }

    public boolean isHUDShown() {
        return isHUDShown;
    }

    public void toggleHUD() {
        this.isHUDShown = !isHUDShown;
    }

    public SpriteSheet getCropSpriteSheet(CropType type) {
        return resourceManager.getCropSpritesheets().get(type);
    }

    public Season getSeason() {
        return timeService.getCurrentSeason();
    }

    public List<Entity> getEntitiesImmutable() {
        return List.copyOf(entities);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void changeCamera() {
        Settings.toggleOrthographic();
        if (Settings.isOrthographic()) {
            orthoCamera.setPosition(camera.getPosition().x,
                    orthoCamera.getPosition().y, camera.getPosition().z);
            cameraController.release(this);
        } else {
            camera.setPosition(orthoCamera.getPosition().x,
                    camera.getPosition().y, orthoCamera.getPosition().z);
            orthoCameraController.release(this);
        }

        gameRenderer.initCamera(getActiveCamera());
        toastService.info("Camera changed to " + (Settings.isOrthographic() ?
                "orthographic" : "first person"));
    }

    public boolean isOrthographicCamera() {
        return Settings.isOrthographic();
    }

    public CameraView getActiveCamera() {
        return Settings.isOrthographic() ? orthoCamera : camera;
    }

    public void addEntity(Entity entity) {
        if (entity == null) {
            return;
        }

        if (entity instanceof WorldItem worldItem) {
            worldItem.setWorld(world);
        }

        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        if (entity == null) return;
        entities.remove(entity);
    }

    private void updateEntities(float delta) {
        for (Entity entity : entities) {
            if (entity instanceof Player player
                    && !player.isAlive()) {
                player.respawn();
            }
            entities.removeIf(e -> !e.isAlive());
            entity.update(HoveredCell.get(this), delta);
        }
    }

    public float getGenDelta() {
        return genDelta;
    }

    public String getFps() {
        return String.format("%.0f", 1.0f / genDelta) + " FPS";
    }

    public void update(float delta) {
        if (weatherService.isRaining()) {
            rainEngine.update(delta);
            if (Settings.doEnableMusic()) {
                soundService.setBackgroundSound(SoundGroup.RAIN);
            } else {
                soundService.setBackgroundSound(null);
            }
        } else {
            if (Settings.doEnableMusic()) {
                soundService.setBackgroundSound(SoundGroup.NATURE);
            } else {
                soundService.setBackgroundSound(null);
            }
        }

        gameUIservice.update(delta);
        if (player == null) {
            return;
        }

        genDelta = delta;
        timeService.update(delta, weatherService);
        float timeOfDay = timeService.getHour() + (timeService.getMinute() / 60.0f);
        celestialLighting.update(HoveredCell.get(this), timeOfDay);
        shop.update(timeService);
        cropService.update(delta, weatherService.getWeather());
        updateEntities(delta);
        itemRenderer.update(delta);

        if (Settings.isOrthographic()) {
            orthoCameraController.update(this, delta);
        } else {
            if (!isChatOpen()) {
                cameraController.update(this, delta);
            }
            camera.update(delta);
        }

        particles.update(delta);
        itemRenderer.update(delta);
        stepController.update(this, player, soundService, delta);

        Item selectedInventoryItem = gameUIservice.getInventoryUI().getSelectedItem();
        gameInteraction.update(this, selectedInventoryItem);

        if (player != null) {
            chunkManager.update(player.getPosition().x,
                    player.getPosition().z, delta);
        }

        Mouse.update();
        Keyboard.update();
    }

    public void render() {
        gameRenderer.render(this, resourceManager, chunkManager.getChunkMeshes());
        Item selectedItem = gameUIservice.getHotbarUI().getSelectedItem();

        if (selectedItem != null && !isInventoryOpen() && isHUDShown()) {
            SpriteSheet spriteSheet = resourceManager.getItemSpriteSheet(selectedItem);
            Shader itemShader = resourceManager.getShader("item");

            glClear(GL_DEPTH_BUFFER_BIT);
            itemRenderer.render(this, selectedItem, spriteSheet,
                    itemShader, celestialLighting);
        }

        gameUIservice.render(isHUDShown(), this);
    }

    public void dispose() {
        chunkManager.dispose();
        resourceManager.dispose();
        itemRenderer.dispose();

        GUI.dispose();
        maskFbo.dispose();
        sceneFbo.dispose();
        blurFbo.dispose();

        rainEngine.dispose();
        shadowMap.dispose();

        cameraController.release(this);
        orthoCameraController.release(this);
        soundService.cleanup();
        log.info("GameMaster resources successfully cleaned up");
    }

    public void recenter() {
        float center = (K.World.MAP_WORLD_SIZE - 1) / 2.0f;
        float worldCenter = center * K.World.TILE_SIZE;
        this.camera.setPosition(worldCenter, 0.0f, worldCenter);
    }

    public WorldItem getWorldItem(Item item) {
        return entities.stream()
                .filter(WorldItem.class::isInstance)
                .map(WorldItem.class::cast)
                .filter(worldItem -> worldItem.getItem().equals(item))
                .findFirst()
                .orElse(null);
    }

    public void toggleInventory() {
        setInventoryOpen(!isInventoryOpen());
        if (isInventoryOpen()) {
            gameUIservice.getBackpackInventoryUI().hide();
            gameUIservice.getInventoryUI().closeBackpack();
        }
    }

    public void onResize(int newWidth, int newHeight) {
        this.windowWidth = newWidth;
        this.windowHeight = newHeight;

        if (camera != null) {
            camera.updateProjection(newWidth, newHeight,
                    Settings.getRenderDistance());
        }

        if (orthoCamera != null) {
            orthoCamera.updateProjection(newWidth, newHeight,
                    Settings.getRenderDistance());
        }

        if (maskFbo != null) {
            maskFbo.dispose();
            maskFbo = new Framebuffer(newWidth, newHeight);
        }

        if (sceneFbo != null) {
            sceneFbo.dispose();
            sceneFbo = new Framebuffer(newWidth, newHeight);
        }

        if (uiManager != null) {
            uiManager.resize(newWidth, newHeight);
            GUI.resize(newWidth, newHeight);
        }

        if (gameUIservice != null) {
            gameUIservice.onResize(newWidth, newHeight);
        }
    }

    public void rebuildChunkMeshAt(int worldX, int worldZ) {
        chunkManager.rebuildChunkMeshAt(worldX, worldZ);
        int localX = Math.floorMod(worldX, Chunk.SIZE_X);
        int localZ = Math.floorMod(worldZ, Chunk.SIZE_Z);

        if (localX == 0) {
            chunkManager.rebuildChunkMeshAt(worldX - 1, worldZ);
        }

        if (localX == Chunk.SIZE_X - 1) {
            chunkManager.rebuildChunkMeshAt(worldX + 1, worldZ);
        }

        if (localZ == 0) {
            chunkManager.rebuildChunkMeshAt(worldX, worldZ - 1);
        }

        if (localZ == Chunk.SIZE_Z - 1) {
            chunkManager.rebuildChunkMeshAt(worldX, worldZ + 1);
        }
    }
}