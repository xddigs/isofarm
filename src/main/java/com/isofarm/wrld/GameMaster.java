package com.isofarm.wrld;

import com.isofarm.craft.Recipe;
import com.isofarm.craft.RecipeRegistry;
import com.isofarm.data.*;
import com.isofarm.entity.*;
import com.isofarm.graphics.*;
import com.isofarm.gui.GUI;
import com.isofarm.gui.GameUIService;
import com.isofarm.gui.UIManager;
import com.isofarm.input.*;
import com.isofarm.item.Item;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.service.*;
import com.isofarm.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

/**
 * Provides game master behavior.
 */
@Singleton
public class GameMaster {
    public static final GameMaster game = new GameMaster();
    private static final Logger log = LoggerFactory.getLogger(GameMaster.class);
    private final long windowHandle = Intro.getWindow();
    private final World world = World.wrld;
    private final Sun sun = new Sun("Sun");
    private final Moon moon = new Moon("Moon");
    private final CelestialLighting celestialLighting = new CelestialLighting(sun, moon);
    private final UIManager uiManager = Intro.getUiManager();
    private final CommandRegistry commandRegistry = new CommandRegistry();
    private final CommandService commandService = new CommandService(commandRegistry);
    private final ItemRegistry itemRegistry = new ItemRegistry();
    private final RainEngine rainEngine = new RainEngine();
    private final List<Entity> entities = new LinkedList<>();
    private List<Recipe> recipes;
    private ShadowMap shadowMap;
    private ChunkManager chunkManager;
    private ItemRenderer itemRenderer;
    private GameUIService gameUIservice;
    private Framebuffer sceneFbo;
    private Framebuffer blurFbo;
    private Camera orthoCamera;
    private CameraController orthoCameraController;
    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;
    private Player player;
    private Shop shop;
    private Difficulty difficulty = Difficulty.NORMAL;

    private boolean isChatOpen = false;
    private boolean isInventoryOpen = false;
    private boolean isHUDShown = true;

    private float genDelta;

    /**
     * Creates a new {@code GameMaster} instance.
     */
    private GameMaster() {}

    /**
     * Loads the resources.
     * @param progressCallback the progress callback value
     */
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

        this.sceneFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.blurFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.chunkManager = new ChunkManager(world, WaterSimulation.ws);
        this.itemRenderer = new ItemRenderer();
        this.shop = new Shop();
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.orthoCamera = new Camera(windowWidth, windowHeight, Settings.getRenderDistance());

        this.orthoCameraController = new CameraController(orthoCamera);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.recenter();
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        recipes = RecipeRegistry.reg.init();
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        this.player = new Player(null, world);
        addEntity(player);
        shop.setPlayer(player);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        Library.initItems(itemRegistry, player);
        notifyProgress(progressCallback, ++currentStep / totalSteps);

        Library.initCommands(genDelta, this);
        notifyProgress(progressCallback, ++currentStep / totalSteps);
    }

    /**
     * Performs the notify progress operation.
     * @param callback the callback value
     * @param progress the progress value
     */
    private void notifyProgress(Consumer<Float> callback, float progress) {
        if (callback != null) {
            callback.accept(progress);
        }
    }

    /**
     * Initializes the ui.
     */
    public void initUI() {
        gameUIservice = new GameUIService(windowHandle, this,
                uiManager, ResourceManager.rem.getSeedIcons(), ResourceManager.rem.getCropIcons(),
                ResourceManager.rem.getBlockIcons(), ResourceManager.rem.getToolIcons(),
                ResourceManager.rem.getMaterialIcons(),
                ResourceManager.rem.getInventoryIcons());

        gameUIservice.setPlayer(this.player);
        commandService.setGameUIService(gameUIservice);
        gameUIservice.setShop(shop);

        String[] defaultNames = {"Alex", "Avery", "Blake", "Casey", "Charlie", "Cameron", "Dakota",
                "Drew", "Eden", "Emery", "Finley", "Harper", "Hayden", "Jamie", "Jordan", "Jesse",
                "Kai", "Kendall", "Logan", "Morgan", "Parker", "Quinn", "Reese", "Riley", "River",
                "Robin", "Rowan", "Sam", "Shawn", "Skyler", "Taylor", "Terry", "Tristan", "Wren"};
        player.setName(defaultNames[(int) (Math.random() * defaultNames.length)]);
        ToastFactory.info(Local.lang.f("toast.open_inventory", player.getName()));
    }

    /**
     * Performs the spawn operation.
     */
    public void spawn() {
        chunkManager.updateLoadedChunks(0, 0);
        GridPos spawn = world.getHighestY(0.5f, 0.5f);
        float spawnY = spawn.y() + 1.8f;
        player.setPosition(0.5f, spawnY, 0.5f);
        orthoCamera.setPosition(0.5f, spawnY + 10.0f, 0.5f);
    }

    /**
     * Returns the sun.
     * @return the sun
     */
    public Sun getSun() {
        return sun;
    }

    /**
     * Returns the moon.
     * @return the moon
     */
    public Moon getMoon() {
        return moon;
    }

    /**
     * Returns the celestial lighting.
     * @return the celestial lighting
     */
    public CelestialLighting getCelestialLighting() {
        return celestialLighting;
    }

    /**
     * Returns the shadow map.
     * @return the shadow map
     */
    public ShadowMap getShadowMap() {
        return shadowMap;
    }

    /**
     * Returns the chunk manager.
     * @return the chunk manager
     */
    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    /**
     * Returns the item renderer.
     * @return the item renderer
     */
    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Returns the scene fbo.
     * @return the scene fbo
     */
    public Framebuffer getSceneFbo() {
        return sceneFbo;
    }

    /**
     * Returns the rain engine.
     * @return the rain engine
     */
    public RainEngine getRainEngine() {
        return rainEngine;
    }

    /**
     * Returns the window handle.
     * @return the window handle
     */
    public long getWindowHandle() {
        return windowHandle;
    }

    /**
     * Returns the world.
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns the player.
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the command registry.
     * @return the command registry
     */
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    /**
     * Returns the item registry.
     * @return the item registry
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Returns the game uiservice.
     * @return the game uiservice
     */
    public GameUIService getGameUIService() {
        return gameUIservice;
    }

    /**
     * Returns the command service.
     * @return the command service
     */
    public CommandService getCommandService() {
        return commandService;
    }

    /**
     * Returns the window width.
     * @return the window width
     */
    public float getWindowWidth() {
        return windowWidth;
    }

    /**
     * Returns the window height.
     * @return the window height
     */
    public float getWindowHeight() {
        return windowHeight;
    }

    /**
     * Returns the ortho camera.
     * @return the ortho camera
     */
    public Camera getOrthoCamera() {
        return orthoCamera;
    }

    /**
     * Returns the blur fbo.
     * @return the blur fbo
     */
    public Framebuffer getBlurFbo() {
        return blurFbo;
    }

    /**
     * Checks whether the chat open condition is met.
     * @return {@code true} if chat open; otherwise {@code false}
     */
    public boolean isChatOpen() {
        return isChatOpen;
    }

    /**
     * Sets the chat open.
     * @param isChatOpen the is chat open value
     */
    public void setChatOpen(boolean isChatOpen) {
        this.isChatOpen = isChatOpen;
    }

    /**
     * Checks whether the inventory open condition is met.
     * @return {@code true} if inventory open; otherwise {@code false}
     */
    public boolean isInventoryOpen() {
        return isInventoryOpen;
    }

    /**
     * Sets the inventory open.
     * @param isInventoryOpen the is inventory open value
     */
    public void setInventoryOpen(boolean isInventoryOpen) {
        this.isInventoryOpen = isInventoryOpen;
    }

    /**
     * Checks whether the hudshown condition is met.
     * @return {@code true} if hudshown; otherwise {@code false}
     */
    public boolean isHUDShown() {
        return isHUDShown;
    }

    /**
     * Performs the toggle hud operation.
     */
    public void toggleHUD() {
        this.isHUDShown = !isHUDShown;
    }

    /**
     * Returns the crop sprite sheet.
     * @param type the type value
     * @return the crop sprite sheet
     */
    public SpriteSheet getCropSpriteSheet(CropType type) {
        return ResourceManager.rem.getCropSpritesheets().get(type);
    }

    /**
     * Returns the season.
     * @return the season
     */
    public Season getSeason() {
        return TimeService.ts.getCurrentSeason();
    }

    /**
     * Returns the entities immutable.
     * @return the entities immutable
     */
    public List<Entity> getEntitiesImmutable() {
        return List.copyOf(entities);
    }

    /**
     * Returns the entities.
     * @return the entities
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * Returns the recipes.
     * @return the recipes
     */
    public List<Recipe> getRecipes() {
        return recipes;
    }

    /**
     * Returns the difficulty.
     * @return the difficulty
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the difficulty.
     * @param difficulty the difficulty value
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Returns the active camera.
     * @return the active camera
     */
    public CameraView getActiveCamera() {
        return orthoCamera;
    }

    /**
     * Adds the entity.
     * @param entity the entity value
     */
    public void addEntity(Entity entity) {
        if (entity == null) {
            return;
        }

        if (entity instanceof WorldItem worldItem) {
            worldItem.setWorld(world);
        }

        entities.add(entity);
    }

    /**
     * Removes the entity.
     * @param entity the entity value
     */
    public void removeEntity(Entity entity) {
        if (entity == null) return;
        entities.remove(entity);
    }

    /**
     * Updates the entities.
     * @param delta the delta value
     */
    private void updateEntities(float delta) {
        if (player != null) {
            player.update(HoveredCell.get(this), delta);
        }

        for (Entity entity : entities) {
            if (entity instanceof Player) continue;
            entity.update(HoveredCell.get(this), delta);
        }
        entities.removeIf(e -> e != player && !e.isAlive());
    }

    /**
     * Returns the gen delta.
     * @return the gen delta
     */
    public float getGenDelta() {
        return genDelta;
    }

    /**
     * Returns the fps.
     * @return the fps
     */
    public String getFps() {
        return String.format("%.0f", 1.0f / genDelta) + " FPS";
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    public void update(float delta) {
        if (WeatherService.isRaining()) {
            rainEngine.update(delta);
            if (Settings.doEnableMusic()) {
                SoundService.fx.setBackgroundSound(SoundGroup.RAIN);
            } else {
                SoundService.fx.setBackgroundSound(null);
            }
        } else {
            if (Settings.doEnableMusic()) {
                SoundService.fx.setBackgroundSound(SoundGroup.NATURE);
            } else {
                SoundService.fx.setBackgroundSound(null);
            }
        }

        BookService.bs.update();
        gameUIservice.update(delta);

        if (player == null) {
            return;
        }

        genDelta = delta;
        TimeService.ts.update(delta, WeatherService.wes);
        float timeOfDay = TimeService.ts.getHour() + (TimeService.ts.getMinute() / 60.0f);
        celestialLighting.update(HoveredCell.get(this), timeOfDay);
        shop.update(TimeService.ts);
        CropService.cs.update(delta, WeatherService.wes.getWeather());
        TreeService.ts.update(this);
        updateEntities(delta);
        orthoCameraController.update(this, delta);
        ParticleEngine.peng.update(delta);
        StepController.step.update(this, player, SoundService.fx, delta);
        GameInteraction.gami.update(this, Settings.selectedItem);

        WaterSimulation.ws.update(delta);
        if (player != null) {
            chunkManager.update(player.getPosition().x,
                    player.getPosition().z, delta);
        }

        Mouse.update();
        Keyboard.update();
        Joystick.update();
    }

    /**
     * Renders render.
     */
    public void render() {
        GameRenderer.gamr.render(this, chunkManager.getChunkMeshes());
        gameUIservice.render(isHUDShown(), this);
    }

    /**
     * Performs the dispose operation.
     */
    public void dispose() {
        chunkManager.dispose();
        ResourceManager.rem.dispose();
        itemRenderer.dispose();

        GUI.dispose();
        sceneFbo.dispose();
        blurFbo.dispose();

        rainEngine.dispose();
        shadowMap.dispose();

        orthoCameraController.release(this);
        SoundService.fx.cleanup();
        log.info("GameMaster resources successfully cleaned up");
    }

    /**
     * Performs the recenter operation.
     */
    public void recenter() {
        float center = (K.World.MAP_WORLD_SIZE - 1) / 2.0f;
        float worldCenter = center * K.World.TILE_SIZE;
    }

    /**
     * Returns the world item.
     * @param item the item value
     * @return the world item
     */
    public WorldItem getWorldItem(Item item) {
        return entities.stream()
                .filter(WorldItem.class::isInstance)
                .map(WorldItem.class::cast)
                .filter(worldItem -> worldItem.getItem().equals(item))
                .findFirst()
                .orElse(null);
    }

    /**
     * Performs the toggle inventory operation.
     */
    public void toggleInventory() {
        setInventoryOpen(!isInventoryOpen());
    }

    /**
     * Performs the on resize operation.
     * @param newWidth the new width value
     * @param newHeight the new height value
     */
    public void onResize(int newWidth, int newHeight) {
        this.windowWidth = newWidth;
        this.windowHeight = newHeight;

        if (orthoCamera != null) {
            orthoCamera.updateProjection(newWidth, newHeight,
                    Settings.getRenderDistance());
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
        ToastFactory.onResize(newWidth);
    }

    /**
     * Performs the rebuild chunk mesh at operation.
     * @param worldX the world x value
     * @param worldZ the world z value
     */
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

    /**
     * Performs the rebuild chunk mesh at operation.
     * @param pos the pos value
     */
    public void rebuildChunkMeshAt(BlockPos pos) {
        rebuildChunkMeshAt(pos.x(), pos.z());
    }
}