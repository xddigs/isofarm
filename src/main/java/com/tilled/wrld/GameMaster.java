package com.tilled.wrld;

import com.tilled.data.*;
import com.tilled.graphics.*;
import com.tilled.gui.GUI;
import com.tilled.gui.UIManager;
import com.tilled.gui.UITextField;
import com.tilled.input.*;
import com.tilled.service.*;
import com.tilled.utils.K;
import com.tilled.utils.Settings;
import imgui.ImGui;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

@SuppressWarnings("all")
public class GameMaster {
    private static final Logger log = LoggerFactory.getLogger(GameMaster.class);
    private final long windowHandle;
    private final World world;
    private final WorldGenerator generator;
    private final Map<Chunk, Mesh> chunkMeshes;
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
    private final Map<CropType, SpriteSheet> cropSpritesheets;
    private final Matrix4f modelMatrix;
    private final Matrix4f viewProjMatrix;
    private final FrustumIntersection frustum;
    private final Sunlight sunlight;
    private Shader defaultShader;
    private Shader outlineShader;
    private Shader rainShader;
    private Framebuffer maskFbo;
    private Mesh screenQuadMesh;
    private Mesh blockMesh;
    private Mesh selectionMesh;
    private Mesh spriteMesh;
    private SpriteSheet wheat;
    private SpriteSheet carrot;
    private SpriteSheet potato;
    private SpriteSheet beetroot;
    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet toolIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet blocksTexture;
    private SpriteSheet waterTexture;
    private Camera camera;
    private CameraController cameraController;
    private StepController stepController;
    private Hit hoveredCell = null;

    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;

    private Player player;
    private Shop shop;
    private boolean isPromptingForInput = false;
    private boolean isInventoryOpen = false;
    private boolean isHUDShown = true;

    private float genDelta;
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;

    public GameMaster(long windowHandle) {
        this.windowHandle = windowHandle;
        this.modelMatrix = new Matrix4f();
        this.viewProjMatrix = new Matrix4f();
        this.frustum = new FrustumIntersection();

        this.world = new World();
        this.generator = new WorldGenerator(world);
        this.chunkMeshes = new HashMap<>();
        this.windowWidth = K.Window.DEFAULT_WIDTH;
        this.windowHeight = K.Window.DEFAULT_HEIGHT;
        this.soundService = new SoundService();

        this.cropService = new CropService(world);
        this.timeService = new TimeService();
        this.commandRegistry = new CommandRegistry();
        this.toastService = new ToastService();
        this.commandService = new CommandService(commandRegistry, toastService);
        this.itemRegistry = new ItemRegistry();
        this.sunlight = new Sunlight(K.Sunlight.DEFAULT_DIRECTION);
        this.particles = new ParticleEngine();
        this.rainEngine = new RainEngine();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);

        this.defaultShader = new Shader(K.Paths.DEFAULT_VERT_SHADER, K.Paths.DEFAULT_FRAG_SHADER);
        this.outlineShader = new Shader(K.Paths.OUTLINE_VERT_SHADER, K.Paths.OUTLINE_FRAG_SHADER);
        this.rainShader = new Shader(K.Paths.RAIN_VERT_SHADER, K.Paths.RAIN_FRAG_SHADER);

        this.maskFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.screenQuadMesh = Mesh.screenQuad();

        this.blockMesh = Mesh.createMesh(K.World.DEFAULT_BLOCK_DEPTH);
        this.selectionMesh = Mesh.selection();
        this.spriteMesh = Mesh.createCrop();

        try {
            this.blocksTexture = new SpriteSheet(K.Paths.BLOCKS, K.UI.BLOCK_ATLAS_FRAMES);
        } catch (Exception e) {
            log.warn("Could not load blocks.png atlas, falling back to base colors: {}", e.getMessage());
            this.blocksTexture = null;
        }

        this.waterTexture = new SpriteSheet(K.Paths.WATER, K.UI.WATER_FRAMES);
        this.cropSpritesheets = new EnumMap(CropType.class);
        this.wheat = new SpriteSheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.carrot = new SpriteSheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.potato = new SpriteSheet(K.Paths.POTATO_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.beetroot = new SpriteSheet(K.Paths.BEETROOT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);

        this.seedIcons = new SpriteSheet(K.Paths.SEED_ICONS, K.UI.ICON_SEED_CROPS_FRAMES);
        this.cropIcons = new SpriteSheet(K.Paths.CROP_ICONS, K.UI.ICON_SEED_CROPS_FRAMES);
        this.toolIcons = new SpriteSheet(K.Paths.TOOL_ICONS, K.UI.ICON_TOOL_FRAMES);
        this.blockIcons = new SpriteSheet(K.Paths.BLOCK_ICONS, K.UI.ICON_BLOCK_FRAMES);

        this.uiManager = new UIManager(windowWidth, windowHeight);
        this.gameUIservice = new GameUIService(windowHandle, this,
                uiManager, seedIcons, cropIcons, blockIcons, toolIcons);

        this.shop = new Shop();
        this.gameUIservice.setShop(shop);

        cropSpritesheets.put(CropType.WHEAT, wheat);
        cropSpritesheets.put(CropType.CARROT, carrot);
        cropSpritesheets.put(CropType.POTATO, potato);
        cropSpritesheets.put(CropType.BEETROOT, beetroot);

        this.camera = new Camera(K.Camera.DEFAULT_WIDTH, K.Camera.DEFAULT_HEIGHT, Settings.renderDistance);
        this.camera.setPosition(0.0f, 0.0f, 0.0f);
        this.cameraController = new CameraController(camera);
        this.stepController = new StepController();
        this.weatherService = new WeatherService(rainEngine, camera);

        this.gameInteraction = new GameInteraction(cropService, gameUIservice,
                timeService, particles, camera, blocksTexture);

        recenter();
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

    public ToastService getToastService() {
        return toastService;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public WeatherService getWeatherService() {
        return weatherService;
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

    public int getLastPlayerChunkX() {
        return lastPlayerChunkX;
    }

    public void setLastPlayerChunkX(int lastPlayerChunkX) {
        this.lastPlayerChunkX = lastPlayerChunkX;
    }

    public int getLastPlayerChunkZ() {
        return lastPlayerChunkZ;
    }

    public void setLastPlayerChunkZ(int lastPlayerChunkZ) {
        this.lastPlayerChunkZ = lastPlayerChunkZ;
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
    }

    public boolean isHUDShown() {
        return isHUDShown;
    }

    public void toggleHUD() {
        this.isHUDShown = !isHUDShown;
    }

    public SpriteSheet getCropSpriteSheet(CropType type) {
        return cropSpritesheets.get(type);
    }

    public Season getSeason() {
        return timeService.getCurrentSeason();
    }

    public void update(float delta) {
        if (weatherService.isRaining()) {
            rainEngine.update(delta, camera.getPosition());
        }

        gameUIservice.update(delta);
        Mouse.update();
        Keyboard.update();

        if (player == null) {
            return;
        }

        genDelta = delta;
        timeService.update(delta, weatherService);
        particles.update(delta);
        shop.update(timeService);
        cropService.update(delta, weatherService.getWeather());

        cameraController.update(this, delta);
        camera.update(delta);

        stepController.update(player, world, soundService, delta);

        Item selectedInventoryItem = gameUIservice.getSelectedInventoryItem();

        if (!ImGui.getIO().getWantCaptureMouse()) {
            hoveredCell = gameInteraction.update(this, selectedInventoryItem);
        } else {
            hoveredCell = null;
        }

        if (player != null) {
            int playerChunkX = Math.floorDiv((int) player.getPosition().x, Chunk.SIZE_X);
            int playerChunkZ = Math.floorDiv((int) player.getPosition().z, Chunk.SIZE_Z);

            if (playerChunkX != lastPlayerChunkX || playerChunkZ != lastPlayerChunkZ) {
                updateLoadedChunks(playerChunkX, playerChunkZ);
                lastPlayerChunkX = playerChunkX;
                lastPlayerChunkZ = playerChunkZ;
            }
        }
    }

    private void updateLoadedChunks(int centerChunkX, int centerChunkZ) {
        int r = Settings.renderDistance;
        int unloadDist = r + Settings.unloadMargin;
        chunkMeshes.entrySet().removeIf(entry -> {
            Chunk chunk = entry.getKey();
            int dx = Math.abs(chunk.getChunkX() - centerChunkX);
            int dz = Math.abs(chunk.getChunkZ() - centerChunkZ);

            if (dx > unloadDist || dz > unloadDist) {
                Mesh mesh = entry.getValue();
                if (mesh != null) {
                    mesh.dispose();
                }
                world.getChunks().remove(chunk.getChunkX(), chunk.getChunkZ());
                return true;
            }
            return false;
        });

        for (int cx = centerChunkX - r; cx <= centerChunkX + r; cx++) {
            for (int cz = centerChunkZ - r; cz <= centerChunkZ + r; cz++) {
                if ((cx - centerChunkX) * (cx - centerChunkX) + (cz - centerChunkZ) * (cz - centerChunkZ) > r * r) {
                    continue;
                }

                Chunk chunk = world.getOrCreateChunk(cx, cz);
                if (!chunkMeshes.containsKey(chunk)) {
                    generator.generateChunk(cx, cz);

                    Mesh mesh = ChunkMeshBuilder.buildMesh(chunk);
                    chunkMeshes.put(chunk, mesh);
                }
            }
        }
    }

    public void render() {
        glActiveTexture(GL_TEXTURE0);
        defaultShader.bind();
        defaultShader.setUniform("uIsMaskPass", false);

        defaultShader.setUniform("uProjection", camera.getProjectionMatrix());
        defaultShader.setUniform("uView", camera.getViewMatrix());

        defaultShader.setUniform("uSunColor", TimeService.getSunLightColor());
        defaultShader.setUniform("uLightIntensity", TimeService.getSunIntensity());
        defaultShader.setUniform("uLightDirection", sunlight.getDirection());

        defaultShader.setUniform("uTotalFrames", 1);
        defaultShader.setUniform("uFrameIndex", 0);
        defaultShader.setUniform("uUseFaceAtlas", false);

        if (blocksTexture != null) {
            blocksTexture.bind();
            defaultShader.setUniform("uUseTexture", true);
        }

        if (blocksTexture != null) {
            blocksTexture.bind();
            defaultShader.setUniform("uUseTexture", true);
            defaultShader.setUniform("uUseFaceAtlas", true);
            defaultShader.setUniform("uTexture", K.Render.PRIMARY_TEXTURE_UNIT);
            defaultShader.setUniform("uTotalFrames", 1);
            defaultShader.setUniform("uFrameIndex", 0);
            defaultShader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
            defaultShader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
        }

        viewProjMatrix.set(camera.getProjectionMatrix()).mul(camera.getViewMatrix());
        frustum.set(viewProjMatrix);

        chunkMeshes.forEach((chunk, mesh) -> {
            if (mesh != null && mesh.getIndicesCount() > 0) {
                float minX = chunk.getChunkX() * Chunk.SIZE_X;
                float minY = 0;
                float minZ = chunk.getChunkZ() * Chunk.SIZE_Z;
                float maxX = minX + Chunk.SIZE_X;
                float maxY = Chunk.SIZE_Y;
                float maxZ = minZ + Chunk.SIZE_Z;
                if (frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ)) {
                    modelMatrix.identity().translate(minX, 0, minZ);
                    defaultShader.setUniform("uModel", modelMatrix);
                    mesh.render();
                }
            }
        });

        world.forEach(block -> {
            if (!(block instanceof Crop crop)) return;

            SpriteSheet sheet = cropSpritesheets.get(crop.getCropType());
            if (sheet == null) {
                log.warn("No spritesheet found for crop type: {}", crop.getCropType());
                return;
            }

            sheet.bind();
            defaultShader.setUniform("uUseTexture", true);
            defaultShader.setUniform("uTexture", K.Render.PRIMARY_TEXTURE_UNIT);
            defaultShader.setUniform("uUseFaceAtlas", false);

            defaultShader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
            defaultShader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));

            defaultShader.setUniform("uTotalFrames", sheet.getTotalFrames());
            defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());

            float renderX = crop.getX() + 0.5f;
            float renderY = crop.getY() + K.World.SHORTER_BLOCK_HEIGHT;
            float renderZ = crop.getZ() + 0.5f;

            modelMatrix.identity().translate(renderX, renderY, renderZ);
            defaultShader.setUniform("uModel", modelMatrix);
            spriteMesh.render();
            sheet.unbind();
        });

        particles.render(defaultShader, spriteMesh);

        if (blocksTexture != null) {
            blocksTexture.unbind();
        }

        if (hoveredCell != null) {
            glEnable(GL_DEPTH_TEST);
            glDepthMask(false);
            defaultShader.bind();
            defaultShader.setUniform("uUseTexture", false);
            defaultShader.setUniform("uUseFaceAtlas", false);
            defaultShader.setUniform("uBaseColor", K.Colors.OUTLINE_DEFAULT);

            modelMatrix.identity().translate(hoveredCell.x(), hoveredCell.y(), hoveredCell.z());

            defaultShader.setUniform("uModel", modelMatrix);
            selectionMesh.renderLines();
            glDepthMask(true);
        }

        if (hoveredCell != null) {
            maskFbo.bind();
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            defaultShader.setUniform("uIsMaskPass", true);
            defaultShader.setUniform("uUseTexture", true);
            defaultShader.setUniform("uUseFaceAtlas", false);
            defaultShader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
            defaultShader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));

            maskFbo.unbind((int) windowWidth, (int) windowHeight);
            glDisable(GL_DEPTH_TEST);

            outlineShader.bind();
            outlineShader.setUniform("uScreenSize", new Vector2f(windowWidth, windowHeight));
            outlineShader.setUniform("uOutlineColor", K.Colors.OUTLINE_DEFAULT);
            outlineShader.setUniform("uMaskTexture", K.Render.PRIMARY_TEXTURE_UNIT);

            glBindTexture(GL_TEXTURE_2D, maskFbo.getTextureId());
            screenQuadMesh.render();

            outlineShader.unbind();
            glEnable(GL_DEPTH_TEST);
        }

        defaultShader.unbind();

        if (weatherService.isRaining()) {
            rainEngine.render(rainShader, camera.getViewMatrix(), camera.getProjectionMatrix());
        }

        gameUIservice.render();
        if (isHUDShown()) {
            // TODO render HUD with GUI API
        }

        if (player == null) {
            this.player = new Player(gameUIservice.getEnteredPlayerName(),
                    world, toastService);

            updateLoadedChunks(0, 0);
            float spawnY = world.getHighestY(0.0f, 0.0f) + 1.0f;
            player.setPosition(0.5f, spawnY, 0.5f);

            gameUIservice.setPlayer(player);
            this.shop.setPlayer(player);

            log.info("Player created: {}", player.getName());
            toastService.success("Welcome, " + player.getName() + "!");
            toastService.info("Use E to open the inventory");
            Library.initItems(itemRegistry, player);
            Library.initCommands(genDelta, this);
        }

        if (isPromptingForInput()) {
            String command = gameUIservice.inputCommand();

            if (command != null) {
                commandService.execute(command);
                setPromptingForInput(false);
            }
        }
    }

    public void dispose() {
        chunkMeshes.values().forEach(Mesh::dispose);
        chunkMeshes.clear();

        blockMesh.dispose();
        selectionMesh.dispose();
        spriteMesh.dispose();
        screenQuadMesh.dispose();
        GUI.dispose();

        if (blocksTexture != null) blocksTexture.dispose();
        if (waterTexture != null) waterTexture.dispose();

        wheat.dispose();
        carrot.dispose();
        potato.dispose();
        beetroot.dispose();

        cropIcons.dispose();
        seedIcons.dispose();
        toolIcons.dispose();
        blockIcons.dispose();

        maskFbo.dispose();
        defaultShader.dispose();
        outlineShader.dispose();
        rainShader.dispose();
        rainEngine.dispose();
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

        if (gameUIservice != null) {
            gameUIservice.onResize(newWidth, newHeight);
        }

        if (uiManager != null) {
            uiManager.resize(newWidth, newHeight);
            GUI.resize(newWidth, newHeight);
        }
    }

    public void rebuildChunkMeshAt(int worldX, int worldZ) {
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        Chunk chunk = world.getOrCreateChunk(chunkX, chunkZ);
        if (chunk != null) {
            Mesh oldMesh = chunkMeshes.get(chunk);
            if (oldMesh != null) {
                oldMesh.dispose();
            }
            chunkMeshes.put(chunk, ChunkMeshBuilder.buildMesh(chunk));
        }
    }
}