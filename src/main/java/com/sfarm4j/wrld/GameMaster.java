package com.sfarm4j.wrld;

import com.sfarm4j.data.CropType;
import com.sfarm4j.graphics.Camera;
import com.sfarm4j.graphics.Mesh;
import com.sfarm4j.graphics.Shader;
import com.sfarm4j.graphics.Texture;
import com.sfarm4j.service.CropService;
import com.sfarm4j.service.TimeService;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("all")
public class GameMaster {
    private final World world;
    private final CropService cropService;
    private final TimeService timeService;

    private Shader defaultShader;
    private Mesh quadMesh;
    private Texture carrotTexture;
    private Camera camera;
    private final Matrix4f modelMatrix = new Matrix4f();

    public GameMaster() {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService(cropService);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        glEnable(GL_DEPTH_TEST);

        this.defaultShader = new Shader("shaders/default.vert",
                "shaders/default.frag");

        this.quadMesh = Mesh.createQuad();
        this.carrotTexture = new Texture("assets/carrot.png");

        this.camera = new Camera(16.0f, 9.0f);
        cropService.plant(CropType.CARROT, timeService.getCurrentSeason(), 10);
    }

    public void update(float delta) {
        timeService.update(delta);
    }

    public void render() {
        defaultShader.bind();

        defaultShader.setUniform("uProjection", camera.getProjectionMatrix());
        defaultShader.setUniform("uView", camera.getViewMatrix());
        defaultShader.setUniform("uTexture", 0);
        carrotTexture.bind();

        modelMatrix.identity()
                .translate(new Vector3f(0.0f, 0.0f, 0.0f))
                .scale(1.5f);

        defaultShader.setUniform("uModel", modelMatrix);
        quadMesh.render();
        carrotTexture.unbind();
        defaultShader.unbind();
    }

    public void cleanup() {
        quadMesh.cleanup();
        carrotTexture.cleanup();
        defaultShader.cleanup();
    }
}