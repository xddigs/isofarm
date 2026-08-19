package com.tilled.wrld;

import com.tilled.data.*;
import com.tilled.entity.Entity;
import com.tilled.entity.Moon;
import com.tilled.entity.Player;
import com.tilled.entity.Sun;
import com.tilled.graphics.*;
import com.tilled.gui.GUI;
import com.tilled.gui.UIManager;
import com.tilled.input.*;
import com.tilled.service.*;
import com.tilled.utils.K;
import com.tilled.utils.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("all")
public class GameMaster {
    private static final Logger log = LoggerFactory.getLogger(GameMaster.class);
    private static final Random random = new Random();
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
    private Camera camera;
    private CameraController cameraController;
    private StepController stepController;
    private Hit hoveredCell = null;

    private float windowWidth;
    private float windowHeight;

    private final List<Entity> entities;

    private Player player;
    private Shop shop;
    private boolean isPromptingForInput = false;
    private boolean isInventoryOpen = false;
    private boolean isHUDShown = true;

    private float genDelta;

    public GameMaster(long windowHandle) {
        this.windowHandle = windowHandle;

        this.world = new World(getSeed());
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

        this.maskFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.sceneFbo = new Framebuffer((int) windowWidth, (int) windowHeight);

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
                K.Camera.DEFAULT_HEIGHT, Settings.renderDistance);

        this.camera.setPosition(0.0f, 0.0f, 0.0f);
        this.gameRenderer.initCamera(camera);

        this.cameraController = new CameraController(camera);
        this.stepController = new StepController();
        this.weatherService = new WeatherService(rainEngine, camera);

        this.gameInteraction = new GameInteraction(this,
                resourceManager.getBlocksTexture());

        recenter();
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
    public Hit getHoveredCell() { return hoveredCell; }
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
    public float getWindowWidth() { return windowWidth; }
    public float getWindowHeight() { return windowHeight; }
    public Camera getCamera() { return camera; }
    public CameraController getCameraController() { return cameraController; }
    public GameInteraction getGameInteraction() { return gameInteraction; }

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

    public boolean isPromptingForInput() {
        return isPromptingForInput;
    }

    public void setPromptingForInput(boolean isPromptingForInput) {
        this.isPromptingForInput = isPromptingForInput;
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

    public List<Entity> getEntities() {
        return List.copyOf(entities);
    }

    public void addEntity(Entity entity) {
        if (entity == null) return;
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        if (entity == null) return;
        entities.remove(entity);
    }

    private void updateEntities(float delta) {
        for (Entity entity : entities) {
            entity.update(delta);
        }
    }

    public void update(float delta) {
        if (weatherService.isRaining()) {
            rainEngine.update(delta, camera.getPosition());
        }

        if (Keyboard.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)) {
            if (!isPromptingForInput()) {
                setPromptingForInput(true);
                gameUIservice.openChat();
            } else {
                String command = gameUIservice.getChatText();
                if (command != null && !command.isEmpty()) {
                    gameUIservice.addChatMessage("> " + command);
                    commandService.execute(command);
                }
                setPromptingForInput(false);
                gameUIservice.closeChat();
            }
        }

        gameUIservice.update(delta);

        if (player == null) {
            return;
        }

        genDelta = delta;
        timeService.update(delta, weatherService);
        float timeOfDay = timeService.getHour() + (timeService.getMinute() / 60.0f);
        celestialLighting.update(timeOfDay);
        particles.update(delta);
        shop.update(timeService);
        cropService.update(delta, weatherService.getWeather());

        cameraController.update(this, delta);
        camera.update(delta);
        itemRenderer.update(delta);

        stepController.update(this, player, soundService, delta);

        Item selectedInventoryItem = gameUIservice.getInventoryUI().getSelectedItem();
        hoveredCell = gameInteraction.update(this, selectedInventoryItem);

        if (player != null) {
            chunkManager.update(player.getPosition().x, player.getPosition().z);
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

        if (player == null) {
            this.player = new Player(gameUIservice.getEnteredPlayerName(),
                    world, toastService);

            chunkManager.updateLoadedChunks(0, 0);
            float spawnY = world.getHighestY(0.0f, 0.0f) + 1.0f;
            player.setPosition(0.5f, spawnY, 0.5f);

            gameUIservice.setPlayer(player);
            this.shop.setPlayer(player);

            log.info("Player created: {}", player.getName());
            toastService.info("Use E to open the inventory");
            Library.initItems(itemRegistry, player);
            Library.initCommands(genDelta, this);
        }
    }

    public void dispose() {
        chunkManager.dispose();
        resourceManager.dispose();
        itemRenderer.dispose();

        GUI.dispose();
        maskFbo.dispose();
        sceneFbo.dispose();
        rainEngine.dispose();
        shadowMap.dispose();

        cameraController.release(this);
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
                    Settings.renderDistance);
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

    public static long getSeed() {
        return random.nextLong(1111111111111L, 9999999999999L);
    }
}