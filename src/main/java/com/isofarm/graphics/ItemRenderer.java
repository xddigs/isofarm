package com.isofarm.graphics;

import com.isofarm.entity.WorldItem;
import com.isofarm.item.Bucket;
import com.isofarm.item.Item;
import com.isofarm.utils.K;
import com.isofarm.wrld.GameMaster;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class ItemRenderer {
    private static final int THICKNESS_LAYERS = 24;
    private static final float LAYER_DEPTH = 0.0025f;

    private final Matrix4f baseModelMatrix;
    private final Mesh quadMesh;

    public ItemRenderer() {
        this.baseModelMatrix = new Matrix4f();
        this.quadMesh = Mesh.createCenteredQuad();
    }

    private float clamp(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }

    private float easeOutCubic(float t) {
        t = clamp(t);
        return 1.0f - (float) Math.pow(1.0f - t, 3.0f);
    }

    private AnimationTransform getAttackTransform(float t) {
        AnimationTransform transform = new AnimationTransform();
        if (t < 0.22f) {
            float p = easeOutCubic(t / 0.22f);
            transform.x = -0.04f * p;
            transform.y = 0.10f * p;
            transform.z = -0.10f * p;
            transform.rotateX = -22.0f * p;
            transform.rotateY = 8.0f * p;
            transform.rotateZ = 10.0f * p;
        } else {
            float p = easeOutCubic((t - 0.22f) / 0.78f);
            transform.x = -0.04f + 0.08f * p;
            transform.y = 0.10f - 0.25f * p;
            transform.z = -0.10f + 0.28f * p;
            transform.rotateX = -22.0f + 82.0f * p;
            transform.rotateY = 8.0f - 12.0f * p;
            transform.rotateZ = 10.0f - 25.0f * p;
            transform.scaleX = 1.0f + 0.04f * p;
            transform.scaleY = 1.0f - 0.05f * p;
            transform.scaleZ = 1.0f + 0.04f * p;
        }
        return transform;
    }

    private AnimationTransform getBreakTransform(float t) {
        AnimationTransform transform = new AnimationTransform();
        if (t < 0.28f) {
            float p = easeOutCubic(t / 0.28f);
            transform.y = 0.12f * p;
            transform.z = -0.08f * p;
            transform.rotateX = -25.0f * p;
            transform.rotateZ = 6.0f * p;
        } else {
            float p = easeOutCubic((t - 0.28f) / 0.72f);
            transform.y = 0.12f - 0.28f * p;
            transform.z = -0.08f + 0.24f * p;
            transform.rotateX = -25.0f + 88.0f * p;
            transform.rotateZ = 6.0f - 18.0f * p;
            transform.scaleX = 1.0f + 0.05f * p;
            transform.scaleY = 1.0f - 0.07f * p;
            transform.scaleZ = 1.0f + 0.05f * p;
        }
        return transform;
    }

    private AnimationTransform getInteractTransform(float t) {
        AnimationTransform transform = new AnimationTransform();
        if (t < 0.35f) {
            float p = easeOutCubic(t / 0.35f);
            transform.y = 0.06f * p;
            transform.z = -0.04f * p;
            transform.rotateX = -8.0f * p;
            transform.rotateZ = 4.0f * p;
        } else {
            float p = easeOutCubic((t - 0.35f) / 0.65f);
            transform.y = 0.06f - 0.12f * p;
            transform.z = -0.04f + 0.08f * p;
            transform.rotateX = -8.0f + 18.0f * p;
            transform.rotateZ = 4.0f - 8.0f * p;
        }
        return transform;
    }

    public void renderWorldItem(GameMaster gameMaster, WorldItem worldItem,
                                CelestialLighting lighting) {
        if (worldItem == null) return;
        Item item = worldItem.getItem();
        if (item == null) return;
        SpriteSheet spriteSheet = ResourceManager.getItemSpriteSheet(item);
        if (spriteSheet == null) return;
        Shader shader = gameMaster.getResourceManager().getShader("item");
        if (shader == null) return;
        renderWorldItemMesh(gameMaster, worldItem, item, spriteSheet, shader, lighting);
    }

    private void renderWorldItemMesh(GameMaster gameMaster, WorldItem worldItem,
                                     Item item, SpriteSheet spriteSheet, Shader shader,
                                     CelestialLighting lighting) {

        if (item == null || spriteSheet == null || quadMesh == null) {
            return;
        }

        Vector3f position = worldItem.getPosition();
        float scale = 0.4f;
        baseModelMatrix.identity().translate(position.x, position.y, position.z)
                .rotateY((float) Math.toRadians(worldItem.getRotation()))
                .rotateZ((float) Math.toRadians(180.0f)).scale(scale, scale, scale);

        int frameIndex = ResourceManager.getItemFrame(item);

        frameIndex = Math.clamp(frameIndex, 0, spriteSheet.getTotalFrames() - 1);
        Vector4f uvBounds = spriteSheet.getUVBounds(frameIndex);
        int textureUnit = K.Render.PRIMARY_TEXTURE_UNIT;

        shader.bind();
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        spriteSheet.bind();
        shader.setUniform("uTexture", textureUnit);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);
        shader.setUniform("uUVBounds", uvBounds);
        shader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uTopAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uBottomAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uSideAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uProjection", gameMaster.getActiveCamera().getProjectionMatrix());
        shader.setUniform("uView", gameMaster.getActiveCamera().getViewMatrix());
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uIsMaskPass", false);
        shader.setUniform("uEnableShadows", false);
        shader.setUniform("uBaseColor", new Vector3f(1.0f));

        setupCommonShaderUniforms(shader, gameMaster, lighting);
        glDisable(GL_CULL_FACE);
        for (int i = THICKNESS_LAYERS - 1; i >= 0; i--) {
            float zOffset = (i - THICKNESS_LAYERS / 2.0f) * LAYER_DEPTH;
            Matrix4f layerMatrix = new Matrix4f(baseModelMatrix).translate(0.0f, 0.0f, zOffset);
            shader.setUniform("uModel", layerMatrix);
            quadMesh.render();
        }
        glEnable(GL_CULL_FACE);
        spriteSheet.unbind();
        shader.unbind();
    }

    private void setupCommonShaderUniforms(Shader shader, GameMaster gameMaster,
                                           CelestialLighting lighting) {
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uTopAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uBottomAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uSideAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uBaseColor", new Vector3f(1.0f, 1.0f, 1.0f));
        shader.setUniform("uIsMaskPass", false);
        shader.setUniform("uEnableShadows", false);

        Vector3f viewLightDir = new Vector3f(lighting.getDirection());
        gameMaster.getActiveCamera().getViewMatrix().transformDirection(viewLightDir);
        shader.setUniform("uLightDirection", viewLightDir);
        shader.setUniform("uSunColor", lighting.getColor());
        shader.setUniform("uSkyColor", lighting.getColor());
        shader.setUniform("uLightIntensity", lighting.getIntensity());
        shader.setUniform("uAmbientIntensity", lighting.getAmbientIntensity());
    }

    public void dispose() {
        if (quadMesh != null) {
            quadMesh.dispose();
        }
    }

    private static final class AnimationTransform {
        float x, y, z;
        float rotateX, rotateY, rotateZ;
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        float scaleZ = 1.0f;
    }
}