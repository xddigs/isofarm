package com.sfarm4j.wrld;

import com.sfarm4j.data.CellType;
import com.sfarm4j.data.Crop;
import com.sfarm4j.data.CropType;
import com.sfarm4j.data.Player;
import com.sfarm4j.graphics.*;
import com.sfarm4j.input.Keyboard;
import com.sfarm4j.input.Mouse;
import com.sfarm4j.service.CellService;
import com.sfarm4j.service.CropService;
import com.sfarm4j.service.TimeService;
import com.sfarm4j.utils.K;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImString;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

@SuppressWarnings("all")
public class GameMaster {
    private static final Logger log = LoggerFactory.getLogger(GameMaster.class);

    private final World world;
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
    private Spritesheet wheat;
    private Camera camera;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Sunlight sunlight;

    private Vector2i hoveredCell = null;

    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;
    private Player player;

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final ImString nameBuffer = new ImString("", 32);

    public GameMaster(long windowHandle) {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService(cropService);
        this.cellService = new CellService();
        this.sunlight = new Sunlight(new Vector3f(-0.5f, -1.0f, -0.5f));

        for (int x = 0; x < K.World.GRID_SIZE; x++) {
            for (int z = 0; z < K.World.GRID_SIZE; z++) {
                cellService.setCell(CellType.TILLED, x, z);
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
        this.wheat = new Spritesheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);

        this.camera = new Camera(K.Camera.DEFAULT_WIDTH, K.Camera.DEFAULT_HEIGHT);
        this.camera.setPosition(0.0f, 0.0f, 0.0f);
        recenter();

        ImGui.createContext();
        ImGui.getIO().setIniFilename(null);

        ImGuiIO io = ImGui.getIO();
        if (new File(K.Paths.FONT).exists()) {
            io.getFonts().addFontFromFileTTF(K.Paths.FONT, K.Style.FONT_SIZE);
        }

        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(K.Style.WINDOW_ROUNDING);
        style.setFrameRounding(K.Style.FRAME_ROUNDING);
        style.setWindowBorderSize(K.Style.WINDOW_BORDER_SIZE);
        style.setFrameBorderSize(K.Style.FRAME_BORDER_SIZE);
        style.setWindowPadding(K.Style.WINDOW_PADDING_X, K.Style.WINDOW_PADDING_Y);
        style.setFramePadding(K.Style.FRAME_PADDING_X, K.Style.FRAME_PADDING_Y);
        style.setItemSpacing(K.Style.ITEM_SPACING_X, K.Style.ITEM_SPACING_Y);
        setColor(style, ImGuiCol.WindowBg,        K.Style.COLOR_WINDOW_BG);
        setColor(style, ImGuiCol.FrameBg,         K.Style.COLOR_FRAME_BG);
        setColor(style, ImGuiCol.FrameBgHovered,  K.Style.COLOR_FRAME_BG_HOVERED);
        setColor(style, ImGuiCol.FrameBgActive,   K.Style.COLOR_FRAME_BG_ACTIVE);
        setColor(style, ImGuiCol.Button,          K.Style.COLOR_BUTTON);
        setColor(style, ImGuiCol.ButtonHovered,   K.Style.COLOR_BUTTON_HOVERED);
        setColor(style, ImGuiCol.ButtonActive,    K.Style.COLOR_BUTTON_ACTIVE);
        setColor(style, ImGuiCol.Text,            K.Style.COLOR_TEXT);

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init(K.Render.GLSL_VERSION);
        log.info("GameMaster initialized with grid size: {}x{}", K.World.GRID_SIZE, K.World.GRID_SIZE);
    }

    private void setColor(ImGuiStyle style, int target, float[] rgba) {
        style.setColor(target, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public void update(float delta) {
        if (player == null) {
            return;
        }

        timeService.update(delta);
        camera.update(delta);

        boolean isCtrlDown = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL) ||
                Keyboard.isKeyDown(GLFW_KEY_RIGHT_CONTROL);
        if (Mouse.isButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (isCtrlDown) {
                camera.rotateYaw(Mouse.getDeltaX() * K.Camera.ROTATION_SENSITIVITY);
            } else {
                camera.pan(Mouse.getDeltaX(), Mouse.getDeltaY(), K.Camera.PAN_SENSITIVITY);
            }
        }

        float scrollY = Mouse.getScrollY();
        if (scrollY != 0.0f) {
            camera.zoom(scrollY);
        }

        if (Mouse.isButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
            recenter();
        }

        Vector2i cell = camera.highlight(Mouse.getX(), Mouse.getY(), windowWidth, windowHeight);
        if (cell != null && cell.x >= 0 && cell.x < K.World.GRID_SIZE
                && cell.y >= 0 && cell.y < K.World.GRID_SIZE) {
            hoveredCell = cell;
        } else {
            hoveredCell = null;
        }

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && hoveredCell != null
                && player.hasSeeds()) {
            CropType selectedType = CropType.WHEAT;
            Crop plantedCrop = cropService.plant(
                    hoveredCell.x,
                    hoveredCell.y,
                    player,
                    cellService.getCell(hoveredCell.x, hoveredCell.y),
                    selectedType,
                    timeService.getCurrentSeason());
        }

        if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT) &&
                Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) &&
                hoveredCell != null) {
            Crop crop = world.getCropAt(hoveredCell.x(), hoveredCell.y());
            if (crop != null) {
                cropService.rip(crop);
            }
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
        defaultShader.setUniform("uTexture", 0);
        defaultShader.setUniform("uTotalFrames", wheat.getTotalFrames());

        wheat.bind();
        world.getActiveCrops().forEach(crop -> {
            modelMatrix.identity().translate(crop.getX(), 0.0f, crop.getZ());
            defaultShader.setUniform("uModel", modelMatrix);
            defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());
            spriteMesh.render();
        });
        wheat.unbind();

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
            wheat.bind();

            world.getActiveCrops().stream()
                    .filter(c -> Math.round(c.getX()) == hoveredCell.x && Math.round(c.getZ()) == hoveredCell.y)
                    .findFirst()
                    .ifPresent(crop -> {
                        modelMatrix.identity().translate(crop.getX(), 0.0f, crop.getZ());
                        defaultShader.setUniform("uModel", modelMatrix);
                        defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());
                        spriteMesh.render();
                    });

            wheat.unbind();
            maskFbo.unbind((int) windowWidth, (int) windowHeight);
            glDisable(GL_DEPTH_TEST);

            outlineShader.bind();
            outlineShader.setUniform("uScreenSize", new Vector2f(windowWidth, windowHeight));
            outlineShader.setUniform("uOutlineColor", K.Colors.OUTLINE_DEFAULT);
            outlineShader.setUniform("uMaskTexture", 0);

            glBindTexture(GL_TEXTURE_2D, maskFbo.getTextureId());
            screenQuadMesh.render();

            outlineShader.unbind();
            glEnable(GL_DEPTH_TEST);
        }

        defaultShader.unbind();

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        if (player == null) {
            ImGui.setNextWindowPos(windowWidth / 2.0f,
                    windowHeight / 2.0f,
                    ImGuiCond.Always,
                    0.5f, 0.5f);

            ImGui.setNextWindowSize(340.0f, 200.0f);
            int windowFlags = ImGuiWindowFlags.NoTitleBar
                    | ImGuiWindowFlags.NoResize
                    | ImGuiWindowFlags.NoMove
                    | ImGuiWindowFlags.NoCollapse;

            ImGui.begin("New Farmer", windowFlags);

            ImGui.text("What's your name, kid?");

            ImGui.pushItemWidth(-1.0f);
            ImGui.inputText("##PlayerName", nameBuffer);
            ImGui.popItemWidth();

            if (ImGui.button("Start your journey", -1.0f, 40.0f)) {
                if (!nameBuffer.get().isBlank()) {
                    this.player = new Player(nameBuffer.get());
                    log.info("Player created: {}", player.getName());
                }
            }

            ImGui.end();
        }

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();

        blockMesh.dispose();
        selectionMesh.dispose();
        spriteMesh.dispose();
        screenQuadMesh.dispose();
        wheat.dispose();
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
    }
}