package com.isofarm.wrld;

import com.isofarm.data.*;
import com.isofarm.entity.*;
import com.isofarm.graphics.*;
import com.isofarm.gui.GUI;
import com.isofarm.gui.UIManager;
import com.isofarm.input.*;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.service.*;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

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
    private final ShadowMap shadowMap;
    private final SoundService soundService;
    private final UIManager uiManager;
    private final GameUIService gameUIservice;
    private final GameInteraction gameInteraction;
    private final CropService cropService;
    private final TimeService timeService;
    private final CommandService commandService;
    private final ParticleEngine particles;
    private final RainEngine rainEngine;
    private final WeatherService weatherService;
    private final ToastService toastService;

    private final CommandRegistry commandRegistry;
    private final ItemRegistry itemRegistry;

    private final ResourceManager resourceManager;
    private final ChunkManager chunkManager;
    private final GameRenderer gameRenderer;
    private final ItemRenderer itemRenderer;

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

    private final List<Entity> entities;

    private Player player;
    private Shop shop;
    private boolean isChatOpen = false;
    private boolean isInventoryOpen = false;
    private boolean isHUDShown = true;

    private float genDelta;

    public GameMaster(long windowHandle) {
        this.windowHandle = windowHandle;

        this.world = new World();
        this.sun = new Sun("Sun");
        this.moon = new Moon("Moon");
        this.celestialLighting = new CelestialLighting(sun, moon);
        this.shadowMap = new ShadowMap(4096, 4096);

        this.windowWidth = K.Window.DEFAULT_WIDTH;
        this.windowHeight = K.Window.DEFAULT_HEIGHT;
        this.soundService = new SoundService();

        this.particles = new ParticleEngine();
        this.cropService = new CropService(world, particles);
        this.timeService = new TimeService();
        this.commandRegistry = new CommandRegistry();
        this.toastService = new ToastService();
        this.commandService = new CommandService(commandRegistry);
        this.itemRegistry = new ItemRegistry();
        this.rainEngine = new RainEngine();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);

        this.maskFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.sceneFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.blurFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.resourceManager = new ResourceManager();
        this.chunkManager = new ChunkManager(world);
        this.gameRenderer = new GameRenderer();
        this.itemRenderer = new ItemRenderer();

        this.uiManager = new UIManager(windowWidth, windowHeight);
        this.gameUIservice = new GameUIService(windowHandle, this,
                uiManager, resourceManager.getSeedIcons(), resourceManager.getCropIcons(),
                resourceManager.getBlockIcons(), resourceManager.getToolIcons(),
                resourceManager.getInventoryIcons());
        this.commandService.setGameUIService(gameUIservice);

        this.shop = new Shop();
        this.entities = new LinkedList<>();
        this.gameUIservice.setShop(shop);

        this.camera = new Camera(K.Camera.DEFAULT_WIDTH,
                K.Camera.DEFAULT_HEIGHT, Settings.getRenderDistance());

        this.orthoCamera = new OrthographicCamera(K.Camera.DEFAULT_WIDTH,
                K.Camera.DEFAULT_HEIGHT, Settings.getRenderDistance());

        this.camera.setPosition(0.0f, 0.0f, 0.0f);

        this.cameraController = new CameraController(camera);
        this.orthoCameraController = new OrthographicCameraController(orthoCamera);
        this.stepController = new StepController();
        this.weatherService = new WeatherService(rainEngine, camera);

        this.gameInteraction = new GameInteraction(this,
                resourceManager.getBlocksTexture());
        recenter();

        this.player = new Player(gameUIservice.getEnteredPlayerName(),
                world, toastService, soundService);
        addEntity(player);

        chunkManager.updateLoadedChunks(0, 0);
        GridPos spawn = world.getHighestY(0.0f, 0.0f);

        player.setPosition(0.5f, spawn.y() + 1.0f, 0.5f);
        gameUIservice.setPlayer(player);
        shop.setPlayer(player);

        log.info("Player created: {}", player.getName());
        toastService.info("Use E to open the inventory");
        Library.initItems(itemRegistry, player);
        Library.initCommands(genDelta, this);
    }

    public Sun getSun() { return sun; }
    public Moon getMoon() { return moon; }
    public CelestialLighting getCelestialLighting() { return celestialLighting; }
    public ShadowMap getShadowMap() { return shadowMap; }
    public ResourceManager getResourceManager() { return resourceManager; }
    public ChunkManager getChunkManager() { return chunkManager; }
    public GameRenderer getGameRenderer() { return gameRenderer; }
    public ItemRenderer getItemRenderer() { return itemRenderer; }
    public ParticleEngine getParticles() { return particles; }
    public Framebuffer getMaskFbo() { return maskFbo; }
    public Framebuffer getSceneFbo() { return sceneFbo; }
    public RainEngine getRainEngine() { return rainEngine; }
    public long getWindowHandle() { return windowHandle; }
    public World getWorld() { return world; }
    public Player getPlayer() { return player; }
    public SoundService getSoundService() { return soundService; }
    public CropService getCropService() { return cropService; }
    public ToastService getToastService() { return toastService; }
    public CommandRegistry getCommandRegistry() { return commandRegistry; }
    public ItemRegistry getItemRegistry() { return itemRegistry; }
    public GameUIService getGameUIService() { return gameUIservice; }
    public WeatherService getWeatherService() { return weatherService; }
    public TimeService getTimeService() { return timeService; }
    public CommandService getCommandService() { return commandService; }
    public float getWindowWidth() { return windowWidth; }
    public float getWindowHeight() { return windowHeight; }
    public Camera getCamera() { return camera; }
    public CameraController getCameraController() { return cameraController; }
    public OrthographicCamera getOrthoCamera() { return orthoCamera; }
    public OrthographicCameraController getOrthoCameraController() { return orthoCameraController; }
    public GameInteraction getGameInteraction() { return gameInteraction; }
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
                "orthographic" : "perspective"));
    }

    public boolean isOrthographicCamera() {
        return Settings.isOrthographic();
    }

    public CameraView getActiveCamera() {
        return Settings.isOrthographic() ? orthoCamera : camera;
    }

    public void addEntity(Entity entity) {
        if (entity == null) return;
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
            entity.update(HoveredCell.get(this), delta);
        }
    }

    public float getGenDelta() {
        return genDelta;
    }

    public String getFps() {
        return String.format("%.0f", 1.0f / genDelta + " FPS");
    }

    public void update(float delta) {
        if (weatherService.isRaining()) {
            rainEngine.update(delta);
            if (Settings.doEnableMusic()){
                soundService.setBackgroundSound(SoundGroup.RAIN);
            } else {
                soundService.setBackgroundSound(null);
            }
        } else {
            if (Settings.doEnableMusic()){
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
        particles.update(delta);
        shop.update(timeService);
        cropService.update(delta, weatherService.getWeather());
        updateEntities(delta);

        if (Settings.isOrthographic()) {
            orthoCameraController.update(this, delta);
        } else {
            if (!isChatOpen()) {
                cameraController.update(this, delta);
            }
            camera.update(delta);
        }

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

            itemRenderer.render(this, selectedItem, spriteSheet,
                    itemShader, celestialLighting, genDelta);
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

    public void toggleInventory() {
        setInventoryOpen(!isInventoryOpen());
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

        if (gameUIservice != null) {
            gameUIservice.onResize(newWidth, newHeight);
        }

        if (uiManager != null) {
            uiManager.resize(newWidth, newHeight);
            GUI.resize(newWidth, newHeight);
        }
    }

    public void rebuildChunkMeshAt(int worldX, int worldZ) {
        chunkManager.rebuildChunkMeshAt(worldX, worldZ);
    }
}