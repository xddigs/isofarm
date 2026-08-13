package com.sfarm4j.wrld;

import com.sfarm4j.data.CellType;
import com.sfarm4j.data.Crop;
import com.sfarm4j.data.CropType;
import com.sfarm4j.graphics.*;
import com.sfarm4j.input.Keyboard;
import com.sfarm4j.input.Mouse;
import com.sfarm4j.service.CellService;
import com.sfarm4j.service.CropService;
import com.sfarm4j.service.TimeService;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
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
    private Mesh blockMesh;
    private Mesh selectionMesh;
    private Mesh spriteMesh;
    private Texture wheatTexture;
    private Camera camera;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Sunlight sunlight;

    private static final int SIZE = 1;
    private Vector2i hoveredCell = null;

    private float windowWidth = 1280.0f;
    private float windowHeight = 720.0f;

    public GameMaster() {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService(cropService);
        this.cellService = new CellService();
        this.sunlight = new Sunlight(new Vector3f(-0.5f, -1.0f, -0.5f));

        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                cellService.setCell(CellType.TILLED, x, z);
            }
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);

        this.defaultShader = new Shader("shaders/default.vert", "shaders/default.frag");

        this.blockMesh = Mesh.createMesh(0.4f);
        this.selectionMesh = Mesh.selection();
        this.spriteMesh = Mesh.quadVertical();
        this.wheatTexture = new Texture("assets/crops/wheat_crop.png");

        this.camera = new Camera(16.0f, 8.0f);
        this.camera.setPosition(0.0f, 0.0f, 0.0f);
        log.info("GameMaster initialized with grid size: {}x{}", SIZE, SIZE);
    }

    public void update(float delta) {
        timeService.update(delta);
        world.update(delta);

        if (Mouse.isButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            float panSensitivity = 0.015f;
            camera.pan(Mouse.getDeltaX(), Mouse.getDeltaY(), panSensitivity);
        }

        Vector2i cell = camera.highlight(Mouse.getX(), Mouse.getY(), windowWidth, windowHeight);
        if (cell != null && cell.x >= 0 && cell.x < SIZE && cell.y >= 0 && cell.y < SIZE) {
            hoveredCell = cell;
        } else {
            hoveredCell = null;
        }

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && hoveredCell != null) {
            CropType selectedType = CropType.WHEAT;

            Crop plantedCrop = cropService.plant(
                    hoveredCell.x,
                    hoveredCell.y,
                    selectedType,
                    timeService.getCurrentSeason(),
                    10
            );

            log.info("Planted crop {} at cell coordinates: ({}, {})",
                    plantedCrop.getType().getName(),
                    hoveredCell.x,
                    hoveredCell.y
            );
        }

        Mouse.update();
        Keyboard.update();
    }

    public void render() {
        glActiveTexture(GL_TEXTURE0);
        defaultShader.bind();

        defaultShader.setUniform("uProjection", camera.getProjectionMatrix());
        defaultShader.setUniform("uView", camera.getViewMatrix());

        defaultShader.setUniform("uUseTexture", false);
        cellService.renderAll(defaultShader, blockMesh, modelMatrix, sunlight);

        defaultShader.setUniform("uUseTexture", true);
        defaultShader.setUniform("uTexture", 0);
        wheatTexture.bind();

        world.getActiveCrops().forEach(crop -> {
            modelMatrix.identity()
                    .translate(crop.getX(), 0.0f, crop.getZ())
                    .rotateY((float) Math.toRadians(-camera.getYaw()))
                    .rotateX((float) Math.toRadians(-camera.getPitch()));

            defaultShader.setUniform("uModel", modelMatrix);
            spriteMesh.render();
        });

        wheatTexture.unbind();

        if (hoveredCell != null) {
            defaultShader.setUniform("uUseTexture", false);
            glDisable(GL_DEPTH_TEST);
            modelMatrix.identity().translate(hoveredCell.x, 0.0f, hoveredCell.y);
            defaultShader.setUniform("uModel", modelMatrix);
            selectionMesh.renderLines();
            glEnable(GL_DEPTH_TEST);
        }

        defaultShader.unbind();
    }

    public void cleanup() {
        blockMesh.cleanup();
        selectionMesh.cleanup();
        spriteMesh.cleanup();
        wheatTexture.cleanup();
        defaultShader.cleanup();
        log.info("GameMaster resources successfully cleaned up");
    }

    public void recenter() {
        float center = (SIZE - 1) / 2.0f;
        this.camera.setPosition(center, 0.0f, center);
    }
}