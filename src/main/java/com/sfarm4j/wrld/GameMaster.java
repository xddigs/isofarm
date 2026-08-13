package com.sfarm4j.wrld;

import com.sfarm4j.data.CellType;
import com.sfarm4j.data.CropType;
import com.sfarm4j.graphics.*;
import com.sfarm4j.service.CellService;
import com.sfarm4j.service.CropService;
import com.sfarm4j.service.TimeService;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

@SuppressWarnings("all")
public class GameMaster {
    private final World world;
    private final CropService cropService;
    private final TimeService timeService;
    private final CellService cellService;

    private Shader defaultShader;
    private Mesh quadMesh;
    private Mesh spriteMesh;
    private Texture carrotTexture;
    private Camera camera;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Sunlight sunlight;

    public GameMaster() {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService(cropService);
        this.cellService = new CellService();
        this.sunlight = new Sunlight(new Vector3f(-0.5f, -1.0f, -0.5f));

        cellService.setCell(CellType.TILLED, 0, 0);
        cellService.setCell(CellType.DIRT, 1, 0);
        cellService.setCell(CellType.TILLED, 0, 1);
        cellService.setCell(CellType.DIRT, 1, 1);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);

        this.defaultShader = new Shader("shaders/default.vert",
                "shaders/default.frag");

        this.quadMesh = Mesh.createQuad();
        this.spriteMesh = Mesh.createVerticalQuad();
        this.carrotTexture = new Texture("assets/carrot.png");

        this.camera = new Camera(16.0f, 9.0f);
        this.camera.setPosition(0.5f, -1.0f, 0.5f);
        cropService.plant(CropType.CARROT, timeService.getCurrentSeason(), 10);
    }

    public void update(float delta) {
        timeService.update(delta);
    }

    public void render() {
        glActiveTexture(GL_TEXTURE0);
        defaultShader.bind();

        defaultShader.setUniform("uProjection", camera.getProjectionMatrix());
        defaultShader.setUniform("uView", camera.getViewMatrix());

        defaultShader.setUniform("uUseTexture", false);
        cellService.renderAll(defaultShader, quadMesh, modelMatrix, sunlight);

        defaultShader.setUniform("uUseTexture", true);
        defaultShader.setUniform("uTexture", 0);
        carrotTexture.bind();

        modelMatrix.identity()
                .translate(new Vector3f(0.0f, 0.0f, 0.0f))
                .rotateY((float) Math.toRadians(45))
                .rotateX((float) Math.toRadians(-30))
                .scale(1.0f);

        defaultShader.setUniform("uModel", modelMatrix);
        spriteMesh.render();

        defaultShader.setUniform("uModel", modelMatrix);
        carrotTexture.unbind();
        defaultShader.unbind();
    }

    public void cleanup() {
        quadMesh.cleanup();
        carrotTexture.cleanup();
        defaultShader.cleanup();
        spriteMesh.cleanup();
    }
}