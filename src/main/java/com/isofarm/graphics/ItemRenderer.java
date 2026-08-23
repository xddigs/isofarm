package com.isofarm.graphics;

import com.isofarm.item.Block;
import com.isofarm.item.Item;
import com.isofarm.item.Tool;
import com.isofarm.entity.WorldItem;
import com.isofarm.wrld.GameMaster;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class ItemRenderer {
    private static final float OFFSET_X = 1.0f;
    private static final float OFFSET_Y = -0.60f;
    private static final float OFFSET_Z = -1.0f;
    private static final float ITEM_SCALE = 1.0f;

    private static final int THICKNESS_LAYERS = 24;
    private static final float LAYER_DEPTH = 0.0025f;

    private final Matrix4f baseModelMatrix;
    private final Mesh quadMesh;

    private enum ActionAnimation {
        NONE,
        BREAK,
        PLACE
    }

    private ActionAnimation currentAnimation = ActionAnimation.NONE;
    private float animationTime = 0.0f;

    private static final float ANIMATION_DURATION = 0.14f;

    public ItemRenderer() {
        this.baseModelMatrix = new Matrix4f();
        this.quadMesh = Mesh.createCenteredQuad();
    }

    public void playBreakAnimation() {
        currentAnimation = ActionAnimation.BREAK;
        animationTime = 0.0f;
    }

    public void playPlaceAnimation() {
        currentAnimation = ActionAnimation.PLACE;
        animationTime = 0.0f;
    }

    private float animationProgress() {
        return Math.min(animationTime / ANIMATION_DURATION, 1.0f);
    }

    public void update(float delta) {
        if (currentAnimation == ActionAnimation.NONE) {
            return;
        }

        animationTime += delta;

        if (animationTime >= ANIMATION_DURATION) {
            animationTime = ANIMATION_DURATION;
            currentAnimation = ActionAnimation.NONE;
        }
    }

    private Vector3f getAnimationOffset() {
        float progress = animationProgress();
        if (currentAnimation == ActionAnimation.BREAK) {
            float swing = (float) Math.sin(progress * Math.PI);
            return new Vector3f(0.0f, -0.10f * swing, 0.12f * swing);
        }

        if (currentAnimation == ActionAnimation.PLACE) {
            float swing = (float) Math.sin(progress * Math.PI);
            return new Vector3f(0.0f, -0.12f * swing, 0.0f);
        }

        return new Vector3f();
    }

    public void renderWorldItem(GameMaster gameMaster, WorldItem worldItem,
                                CelestialLighting lighting) {
        if (worldItem == null) return;
        Item item = worldItem.getItem();
        if (item == null) return;
        SpriteSheet spriteSheet = gameMaster.getResourceManager().getItemSpriteSheet(item);
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

        baseModelMatrix.identity()
                .translate(position.x, position.y, position.z)
                .rotateY((float) Math.toRadians(worldItem.getRotation()))
                .rotateZ((float) Math.toRadians(180.0f))
                .scale(scale, scale, scale);

        int frameIndex = item instanceof Block ? item.getId() - 1 : item.getId();

        shader.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, spriteSheet.getTextureId());

        shader.setUniform("uProjection", gameMaster.getActiveCamera().getProjectionMatrix());
        shader.setUniform("uView", gameMaster.getActiveCamera().getViewMatrix());
        shader.setUniform("uFrameIndex", frameIndex);
        shader.setUniform("uTotalFrames", spriteSheet.getTotalFrames());

        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uAtlasScale", new org.joml.Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new org.joml.Vector2f(0.0f, 0.0f));
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

        glDisable(GL_CULL_FACE);

        for (int i = THICKNESS_LAYERS - 1; i >= 0; i--) {
            float zOffset = (i - THICKNESS_LAYERS / 2.0f) * LAYER_DEPTH;
            Matrix4f layerMatrix = new Matrix4f(baseModelMatrix).translate(0.0f, 0.0f, zOffset);
            shader.setUniform("uModel", layerMatrix);
            quadMesh.render();
        }

        glEnable(GL_CULL_FACE);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void render(GameMaster gameMaster,
                       Item item,
                       SpriteSheet spriteSheet,
                       Shader shader,
                       CelestialLighting lighting,
                       float delta) {
        if (item == null || spriteSheet == null || quadMesh == null) return;
        if (gameMaster.isOrthographicCamera()) return;
        boolean isTool = item instanceof Tool;
        float rotateX, rotateY, rotateZ;
        if (isTool) {
            rotateX = 10.0f;
            rotateY = -90.0f;
            rotateZ = 48.0f;
        } else {
            rotateX = 0.0f;
            rotateY = -15.0f;
            rotateZ = 0.0f;
        }

        Vector3f animationOffset = getAnimationOffset();

        baseModelMatrix.identity().translate(OFFSET_X + animationOffset.x,
                OFFSET_Y + animationOffset.y, OFFSET_Z + animationOffset.z)
                .rotateX((float) Math.toRadians(rotateX))
                .rotateY((float) Math.toRadians(rotateY))
                .rotateZ((float) Math.toRadians(rotateZ))
                .scale(ITEM_SCALE, -ITEM_SCALE, ITEM_SCALE);

        int frameIndex = item instanceof Block ? item.getId() - 1 : item.getId();
        shader.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, spriteSheet.getTextureId());
        shader.setUniform("uProjection", gameMaster.getActiveCamera().getProjectionMatrix());
        shader.setUniform("uProjection", gameMaster.getActiveCamera().getProjectionMatrix());
        shader.setUniform("uView", new Matrix4f().identity());
        shader.setUniform("uFrameIndex", frameIndex);
        shader.setUniform("uTotalFrames", spriteSheet.getTotalFrames());

        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uAtlasScale", new org.joml.Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new org.joml.Vector2f(0.0f, 0.0f));
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

        glDisable(GL_CULL_FACE);
        for (int i = THICKNESS_LAYERS - 1; i >= 0; i--) {
            Matrix4f layerMatrix = new Matrix4f(baseModelMatrix).translate(0.0f, 0.0f, -i * LAYER_DEPTH);
            shader.setUniform("uModel", layerMatrix);
            quadMesh.render();
        }

        glEnable(GL_CULL_FACE);
        shader.unbind();
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void dispose() {
        if (quadMesh != null) {
            quadMesh.dispose();
        }
    }
}