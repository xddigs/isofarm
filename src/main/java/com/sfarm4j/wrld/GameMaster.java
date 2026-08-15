package com.sfarm4j.wrld;

import com.sfarm4j.data.*;
import com.sfarm4j.graphics.*;
import com.sfarm4j.input.GameInteraction;
import com.sfarm4j.input.Keyboard;
import com.sfarm4j.input.Mouse;
import com.sfarm4j.service.CellService;
import com.sfarm4j.service.CropService;
import com.sfarm4j.service.GameUIService;
import com.sfarm4j.service.TimeService;
import com.sfarm4j.utils.K;
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
    private final World world;
    private final GameUIService gameUIservice;
    private final GameInteraction gameInteraction;
    private final CropService cropService;
    private final TimeService timeService;
    private final CellService cellService;

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
    private final Map<CropType, SpriteSheet> cropSpritesheets;
    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private Camera camera;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Sunlight sunlight;

    private Vector2i hoveredCell = null;

    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;

    private Player player;
    private Shop shop;

    public GameMaster(long windowHandle) {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService();
        this.cellService = new CellService();

        this.sunlight = new Sunlight(K.Sunlight.DEFAULT_DIRECTION);

        int center = K.World.GRID_SIZE / 2;

        for (int x = 0; x < K.World.GRID_SIZE; x++) {
            for (int z = 0; z < K.World.GRID_SIZE; z++) {
                cellService.setCell(CellType.TILLED, x, z);
            }
        }

        for (int x = center - 1; x <= center + 1; x++) {
            for (int z = center - 1; z <= center + 1; z++) {
                Cell cell = cellService.find(x, z);
                if (cell != null) {
                    cell.setUnlocked(true);
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
        this.cropSpritesheets = new EnumMap(CropType.class);
        this.wheat = new SpriteSheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.carrot = new SpriteSheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.potato = new SpriteSheet(K.Paths.POTATO_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.seedIcons = new SpriteSheet(K.Paths.SEED_ICONS, K.UI.ICON_ATLAS_FRAMES);
        this.cropIcons = new SpriteSheet(K.Paths.CROP_ICONS, K.UI.ICON_ATLAS_FRAMES);
        this.gameUIservice = new GameUIService(windowHandle, seedIcons, cropIcons);
        this.shop = new Shop();
        this.gameUIservice.setShop(shop);

        cropSpritesheets.put(CropType.WHEAT, wheat);
        cropSpritesheets.put(CropType.CARROT, carrot);
        cropSpritesheets.put(CropType.POTATO, potato);

        this.camera = new Camera(K.Camera.DEFAULT_WIDTH, K.Camera.DEFAULT_HEIGHT);
        this.camera.setPosition(0.0f, 0.0f, 0.0f);
        this.gameInteraction = new GameInteraction(cropService, gameUIservice,
                cellService, timeService, camera);

        recenter();
        log.info("GameMaster initialized with grid size: {}x{}",
                K.World.GRID_SIZE, K.World.GRID_SIZE);
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

    public void update(float delta) {
        if (player == null) {
            return;
        }

        timeService.update(delta);
        shop.update(timeService);
        cropService.update(delta);
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

        defaultShader.setUniform("uUseTexture", false);
        defaultShader.setUniform("uBaseColor", K.Colors.CELL_EVEN);
        cellService.renderAll(defaultShader, blockMesh, modelMatrix, sunlight);

        defaultShader.setUniform("uUseTexture", true);
        defaultShader.setUniform("uTexture", K.Render.PRIMARY_TEXTURE_UNIT);
        defaultShader.setUniform("uTotalFrames", wheat.getTotalFrames());

        world.getActiveCrops().forEach(crop -> {
            SpriteSheet sheet = cropSpritesheets.get(crop.getType());
            sheet.bind();

            modelMatrix.identity().translate(crop.getX(), K.World.CROP_ELEVATION_Y, crop.getZ());
            defaultShader.setUniform("uModel", modelMatrix);
            defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());
            spriteMesh.render();

            sheet.unbind();
        });

        if (hoveredCell != null) {
            defaultShader.setUniform("uUseTexture", false);
            modelMatrix.identity().translate(hoveredCell.x, 0.0f, hoveredCell.y);
            defaultShader.setUniform("uModel", modelMatrix);
            selectionMesh.renderLines();
        }

        if (hoveredCell != null) {
            maskFbo.bind();
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            defaultShader.setUniform("uIsMaskPass", true);
            defaultShader.setUniform("uUseTexture", true);

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

        wheat.dispose();
        carrot.dispose();
        potato.dispose();

        cropIcons.dispose();
        seedIcons.dispose();

        maskFbo.dispose();
        defaultShader.dispose();
        outlineShader.dispose();
        log.info("GameMaster resources successfully cleaned up");
    }

    public void recenter() {
        float center = (K.World.GRID_SIZE - 1) / 2.0f;
        this.camera.setPosition(center, 0.0f, center);
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