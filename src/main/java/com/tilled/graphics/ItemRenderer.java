package com.tilled.graphics;

import com.tilled.data.Block;
import com.tilled.data.Item;
import com.tilled.data.Tool;
import com.tilled.wrld.GameMaster;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class ItemRenderer {
    private static final float OFFSET_X = 0.60f;
    private static final float OFFSET_Y = -0.35f;
    private static final float OFFSET_Z = -0.55f;
    private static final float ITEM_SCALE = 0.60f;

    private static final int THICKNESS_LAYERS = 24;
    private static final float LAYER_DEPTH = 0.0025f;

    private final Matrix4f baseModelMatrix;
    private final Mesh quadMesh;
    private float bobbingTimer = 0.0f;

    public ItemRenderer() {
        this.baseModelMatrix = new Matrix4f();
        this.quadMesh = Mesh.createCenteredQuad();
    }

    public void render(GameMaster gameMaster, Item item,
                       SpriteSheet spriteSheet, Shader shader,
                       CelestialLighting lighting,
                       boolean isMoving, float delta) {
        if (item == null || spriteSheet == null || quadMesh == null) return;
        glClear(GL_DEPTH_BUFFER_BIT);

        if (isMoving) {
            bobbingTimer += delta * 8.0f;
        } else {
            bobbingTimer += (0.0f - bobbingTimer) * delta * 5.0f;
        }

        float bobbingX = (float) Math.cos(bobbingTimer * 0.5f) * 0.015f;
        float bobbingY = (float) Math.abs(Math.sin(bobbingTimer)) * 0.025f;

        float rotateX, rotateY, rotateZ;
        boolean isTool = item instanceof Tool;
        if (isTool) {
            rotateX = 10.0f;
            rotateY = -90.0f;
            rotateZ = 48.0f;
        } else {
            rotateX = 0.0f;
            rotateY = -15.0f;
            rotateZ = 0.0f;
        }

        baseModelMatrix.identity()
                .translate(OFFSET_X + bobbingX,
                        OFFSET_Y + bobbingY,
                        OFFSET_Z)
                .rotateX((float) Math.toRadians(rotateX))
                .rotateY((float) Math.toRadians(rotateY))
                .rotateZ((float) Math.toRadians(rotateZ))
                .scale(ITEM_SCALE, -ITEM_SCALE, ITEM_SCALE);

        int frameIndex = item instanceof Block ? item.getId() - 1 : item.getId();
        shader.bind();
        glBindTexture(GL_TEXTURE_2D, spriteSheet.getTextureId());
        shader.setUniform("uProjection", gameMaster.getCamera().getProjectionMatrix());
        shader.setUniform("uView", new Matrix4f().identity());
        shader.setUniform("uFrameIndex", frameIndex);
        shader.setUniform("uTotalFrames", spriteSheet.getTotalFrames());
        Vector3f viewLightDir = new Vector3f(lighting.getDirection());
        gameMaster.getCamera().getViewMatrix().transformDirection(viewLightDir);
        shader.setUniform("uLightDirection", viewLightDir);
        shader.setUniform("uSunColor", lighting.getColor());
        shader.setUniform("uSkyColor", lighting.getColor());
        shader.setUniform("uLightIntensity", lighting.getIntensity());
        shader.setUniform("uAmbientIntensity", lighting.getAmbientIntensity());
        shader.setUniform("uEnableShadows", false);

        for (int i = THICKNESS_LAYERS - 1; i >= 0; i--) {
            Matrix4f layerMatrix = new Matrix4f(baseModelMatrix)
                    .translate(0.0f, 0.0f, -i * LAYER_DEPTH);

            shader.setUniform("uModel", layerMatrix);
            quadMesh.render();
        }

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void dispose() {
        if (quadMesh != null) {
            quadMesh.dispose();
        }
    }
}