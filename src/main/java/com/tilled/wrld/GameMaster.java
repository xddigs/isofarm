package com.tilled.wrld;

import com.tilled.data.*;
import com.tilled.graphics.*;
import com.tilled.input.GameInteraction;
import com.tilled.input.Keyboard;
import com.tilled.input.Mouse;
import com.tilled.service.*;
import com.tilled.utils.K;
import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

@SuppressWarnings("all")
public class GameMaster {
    private static final Logger log = LoggerFactory.getLogger(GameMaster.class);
    private final long windowHandle;
    private final World world;
    private final GameUIService gameUIservice;
    private final GameInteraction gameInteraction;
    private final CropService cropService;
    private final TimeService timeService;
    private final BlockService blockService;
    private final CommandService commandService;
    private final ParticleEngine particles;
    private final WeatherService weatherService;

    private final CommandRegistry commandRegistry;
    private final ItemRegistry itemRegistry;

    private Shader defaultShader;
    private Shader outlineShader;
    private Framebuffer maskFbo;
    private Mesh screenQuadMesh;

    private Mesh blockMesh;
    private Mesh selectionMesh;
    private Mesh spriteMesh;
    private SpriteSheet wheat;
    private SpriteSheet carrot;
    private SpriteSheet potato;
    private SpriteSheet beetroot;

    private final Map<CropType, SpriteSheet> cropSpritesheets;
    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet toolIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet blocksTexture;
    private SpriteSheet waterTexture;

    private Camera camera;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Sunlight sunlight;

    private Vector2i hoveredCell = null;

    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;

    private Player player;
    private Shop shop;
    private boolean isPromptingForInput = false;

    public GameMaster(long windowHandle) {
        this.windowHandle = windowHandle;
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService();
        this.blockService = new BlockService();
        this.commandRegistry = new CommandRegistry();
        this.commandService = new CommandService(commandRegistry);
        this.itemRegistry = new ItemRegistry();
        this.sunlight = new Sunlight(K.Sunlight.DEFAULT_DIRECTION);
        this.particles = new ParticleEngine();
        this.weatherService = new WeatherService();

        int center = K.World.STARTING_GRID_SIZE / 2;

        for (int x = 0; x < K.World.STARTING_GRID_SIZE; x++) {
            for (int z = 0; z < K.World.STARTING_GRID_SIZE; z++) {
                Block block = new Block(BlockData.TILLED_DIRT, x, 0, z);
                blockService.setBlock(block.getType(), block.getX(), block.getZ());
                world.addBlock(block);
            }
        }

        for (int x = center - 1; x <= center + 1; x++) {
            for (int z = center - 1; z <= center + 1; z++) {
                Block block = blockService.find(x, z);
                if (block != null) {
                    block.setUnlocked(true);
                }
            }
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);

        this.defaultShader = new Shader(K.Paths.DEFAULT_VERT_SHADER, K.Paths.DEFAULT_FRAG_SHADER);
        this.outlineShader = new Shader(K.Paths.OUTLINE_VERT_SHADER, K.Paths.OUTLINE_FRAG_SHADER);

        this.maskFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.screenQuadMesh = Mesh.screenQuad();

        this.blockMesh = Mesh.createMesh(K.World.DEFAULT_BLOCK_DEPTH);
        this.selectionMesh = Mesh.selection();
        this.spriteMesh = Mesh.createCrop();

        try {
            this.blocksTexture = new SpriteSheet(K.Paths.BLOCKS, K.UI.WATER_FRAMES);
        } catch (Exception e) {
            log.warn("Could not load blocks.png atlas, falling back to base colors: {}", e.getMessage());
            this.blocksTexture = null;
        }

        this.waterTexture = new SpriteSheet(K.Paths.WATER, K.UI.BLOCK_ATLAS_FRAMES);
        this.cropSpritesheets = new EnumMap(CropType.class);
        this.wheat = new SpriteSheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.carrot = new SpriteSheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.potato = new SpriteSheet(K.Paths.POTATO_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.beetroot = new SpriteSheet(K.Paths.BEETROOT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);

        this.seedIcons = new SpriteSheet(K.Paths.SEED_ICONS, K.UI.ICON_SEED_CROPS_FRAMES);
        this.cropIcons = new SpriteSheet(K.Paths.CROP_ICONS, K.UI.ICON_SEED_CROPS_FRAMES);
        this.toolIcons = new SpriteSheet(K.Paths.TOOL_ICONS, K.UI.ICON_TOOL_FRAMES);
        this.blockIcons = new SpriteSheet(K.Paths.BLOCK_ICONS, K.UI.ICON_BLOCK_FRAMES);

        this.gameUIservice = new GameUIService(windowHandle, commandService,
                seedIcons, cropIcons, blockIcons, toolIcons);

        this.shop = new Shop();
        this.gameUIservice.setShop(shop);

        cropSpritesheets.put(CropType.WHEAT, wheat);
        cropSpritesheets.put(CropType.CARROT, carrot);
        cropSpritesheets.put(CropType.POTATO, potato);
        cropSpritesheets.put(CropType.BEETROOT, beetroot);

        this.camera = new Camera(K.Camera.DEFAULT_WIDTH, K.Camera.DEFAULT_HEIGHT);
        this.camera.setPosition(0.0f, 0.0f, 0.0f);
        this.gameInteraction = new GameInteraction(cropService, gameUIservice,
                blockService, timeService, particles, camera);

        recenter();
        log.info("GameMaster initialized with grid size: {}x{}",
                K.World.STARTING_GRID_SIZE, K.World.STARTING_GRID_SIZE);
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

    public float getWindowWidth() {
        return windowWidth;
    }

    public float getWindowHeight() {
        return windowHeight;
    }

    public boolean isPromptingForInput() {
        return isPromptingForInput;
    }

    public void setPromptingForInput(boolean promptingForInput) {
        isPromptingForInput = promptingForInput;
    }

    public SpriteSheet getCropSpriteSheet(CropType type) {
        return cropSpritesheets.get(type);
    }

    public void update(float delta) {
        if (player == null) {
            return;
        }

        timeService.update(delta, weatherService);
        particles.update(delta);
        shop.update(timeService);
        cropService.update(delta, weatherService.getWeather());
        camera.update(delta);
        gameUIservice.update(delta);

        Item selectedInventoryItem = gameUIservice.getSelectedInventoryItem();
        if (!ImGui.getIO().getWantCaptureMouse()) {
            hoveredCell = gameInteraction.update(this, selectedInventoryItem);
        } else {
            hoveredCell = null;
        }

        Mouse.update();
        Keyboard.update();
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
            defaultShader.setUniform("uTexture", K.Render.PRIMARY_TEXTURE_UNIT);
            defaultShader.setUniform("uUseFaceAtlas", true);

            BlockData dirt = BlockData.DIRT;
            defaultShader.setUniform("uAtlasScale", dirt.getAtlasScale());
            defaultShader.setUniform("uTopAtlasOffset", dirt.getTopAtlasOffset());
            defaultShader.setUniform("uBottomAtlasOffset", dirt.getBottomAtlasOffset());
            defaultShader.setUniform("uSideAtlasOffset", dirt.getSideAtlasOffset());
        } else {
            defaultShader.setUniform("uUseTexture", false);
            defaultShader.setUniform("uBaseColor", K.Colors.DEFAULT_DIRT);
        }

        blockService.renderAll(defaultShader, blockMesh, modelMatrix, sunlight);
        particles.render(defaultShader, blockMesh, blocksTexture, modelMatrix);

        world.getActiveCrops().forEach(crop -> {
            SpriteSheet sheet = cropSpritesheets.get(crop.getType());
            sheet.bind();
            defaultShader.setUniform("uUseFaceAtlas", false);
            defaultShader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
            defaultShader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
            defaultShader.setUniform("uTotalFrames", sheet.getTotalFrames());
            modelMatrix.identity().translate(crop.getX(), K.World.CROP_ELEVATION_Y, crop.getZ());
            defaultShader.setUniform("uModel", modelMatrix);
            defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());
            spriteMesh.render();
            sheet.unbind();
        });

        if (blocksTexture != null) {
            defaultShader.setUniform("uUseFaceAtlas", true);
        }

        world.getBlocks().values().forEach(block -> {
            if (block.getY() == 0) return;

            modelMatrix.identity().translate(block.getX(), block.getY(), block.getZ());
            BlockData blockData = block.getType();
            defaultShader.setUniform("uModel", modelMatrix);

            if (blocksTexture != null) {
                defaultShader.setUniform("uAtlasScale", blockData.getAtlasScale());
                defaultShader.setUniform("uTopAtlasOffset", blockData.getTopAtlasOffset());
                defaultShader.setUniform("uBottomAtlasOffset", blockData.getBottomAtlasOffset());
                defaultShader.setUniform("uSideAtlasOffset", blockData.getSideAtlasOffset());
            } else {
                defaultShader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
                defaultShader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
            }

            blockMesh.render();
        });

        defaultShader.setUniform("uUseFaceAtlas", false);

        if (blocksTexture != null) {
            blocksTexture.unbind();
        }

        if (hoveredCell != null) {
            defaultShader.setUniform("uUseTexture", false);
            Block hoveredBlock = world.getBlocks().values().stream()
                    .filter(block ->
                            Math.round(block.getX()) == hoveredCell.x &&
                                    Math.round(block.getZ()) == hoveredCell.y
                    )
                    .findFirst()
                    .orElse(null);
            if (hoveredBlock != null) {
                modelMatrix.identity().translate(
                        hoveredBlock.getX(),
                        hoveredBlock.getY(),
                        hoveredBlock.getZ());
                defaultShader.setUniform("uModel", modelMatrix);
                selectionMesh.renderLines();

            } else {
                modelMatrix.identity().translate(hoveredCell.x * K.World.TILE_SIZE, 0.0f,
                        hoveredCell.y * K.World.TILE_SIZE);
                defaultShader.setUniform("uModel", modelMatrix);
                selectionMesh.renderLines();
            }
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

            world.getActiveCrops().stream()
                    .filter(c -> Math.round(c.getX()) == hoveredCell.x
                            && Math.round(c.getZ()) == hoveredCell.y)
                    .findFirst()
                    .ifPresent(crop -> {
                        SpriteSheet sheet = cropSpritesheets.get(crop.getType());
                        sheet.bind();
                        modelMatrix.identity().translate(crop.getX(), K.World.CROP_ELEVATION_Y, crop.getZ());
                        defaultShader.setUniform("uModel", modelMatrix);
                        defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());
                        spriteMesh.render();
                        sheet.unbind();
                    });

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

        gameUIservice.beginFrame();
        gameUIservice.renderHUD();

        if (hoveredCell != null && player != null) {
            gameUIservice.renderTooltip(hoveredCell, world);
        }

        if (player == null) {
            if (gameUIservice.renderNewPlayer()) {
                this.player = new Player(gameUIservice.getEnteredPlayerName());
                gameUIservice.setPlayer(player);
                this.shop.setPlayer(player);
                log.info("Player created: {}", player.getName());
                InitService.initItems(itemRegistry, player);
                InitService.initCommands(commandRegistry,
                        itemRegistry, player);
            }
        }

        if (isPromptingForInput()) {
            String command = gameUIservice.inputCommand();

            if (command != null) {
                commandService.execute(command);
                setPromptingForInput(false);
            }
        }

        gameUIservice.endFrame();
    }

    public void dispose() {
        gameUIservice.dispose();

        blockMesh.dispose();
        selectionMesh.dispose();
        spriteMesh.dispose();
        screenQuadMesh.dispose();

        if (blocksTexture != null || waterTexture != null) {
            blocksTexture.dispose();
            waterTexture.dispose();
        }

        wheat.dispose();
        carrot.dispose();
        potato.dispose();

        cropIcons.dispose();
        seedIcons.dispose();
        toolIcons.dispose();
        blockIcons.dispose();

        maskFbo.dispose();
        defaultShader.dispose();
        outlineShader.dispose();
        log.info("GameMaster resources successfully cleaned up");
    }

    public void recenter() {
        float center = (K.World.STARTING_GRID_SIZE - 1) / 2.0f;
        float worldCenter = center * K.World.TILE_SIZE;
        this.camera.setPosition(worldCenter, 0.0f, worldCenter);
    }

    public void onResize(int newWidth, int newHeight) {
        this.windowWidth = newWidth;
        this.windowHeight = newHeight;

        if (camera != null) {
            camera.updateProjection(newWidth, newHeight);
        }

        if (maskFbo != null) {
            maskFbo.dispose();
            maskFbo = new Framebuffer(newWidth, newHeight);
        }

        if (gameUIservice != null) {
            gameUIservice.onResize(newWidth, newHeight);
        }
    }
}