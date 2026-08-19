package com.tilled.graphics;

import com.tilled.data.Block;
import com.tilled.data.Item;
import com.tilled.data.Tool;
import com.tilled.entity.WorldItem;
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

    private float easeOut(float t) {
        return 1.0f - (1.0f - t) * (1.0f - t);
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

    private float getAnimationRotation() {
        if (currentAnimation == ActionAnimation.BREAK) {
            float progress = animationProgress();
            return (float) Math.sin(progress * Math.PI) * 18.0f;
        }

        if (currentAnimation == ActionAnimation.PLACE) {
            float progress = animationProgress();
            return (float) Math.sin(progress * Math.PI) * 5.0f;
        }

        return 0.0f;
    }

    public void renderWorldItem(GameMaster gameMaster, WorldItem worldItem, CelestialLighting lighting) {
        if (worldItem == null) return;
        Item item = worldItem.getItem();
        if (item == null) return;
        SpriteSheet spriteSheet = gameMaster.getResourceManager().getItemSpriteSheet(item);
        if (spriteSheet == null) return;
        Shader shader = gameMaster.getResourceManager().getShader("item");
        if (shader == null) return;
        Vector3f position = worldItem.getPosition();
        renderWorldItemMesh(gameMaster, item, spriteSheet, shader, lighting, position);
    }

    private void renderWorldItemMesh(GameMaster gameMaster, Item item, SpriteSheet spriteSheet, Shader shader, CelestialLighting lighting, Vector3f position) {

        if (item == null || spriteSheet == null || quadMesh == null) {
            return;
        }

        float scale = 0.45f;
        baseModelMatrix.identity().translate(position.x, position.y, position.z).rotateY((float)
                Math.toRadians(45.0f)).scale(scale, -scale, scale);

        int frameIndex = item instanceof Block ? item.getId() - 1 : item.getId();
        shader.bind();
        glBindTexture(GL_TEXTURE_2D, spriteSheet.getTextureId());
        shader.setUniform("uProjection", gameMaster.getCamera().getProjectionMatrix());
        shader.setUniform("uView", gameMaster.getCamera().getViewMatrix());
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
            Matrix4f layerMatrix = new Matrix4f(baseModelMatrix).translate(0.0f, 0.0f, -i * LAYER_DEPTH);
            shader.setUniform("uModel", layerMatrix);
            quadMesh.render();
        }

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void render(GameMaster gameMaster, Item item,
                       SpriteSheet spriteSheet, Shader shader,
                       CelestialLighting lighting, float delta) {
        if (item == null || spriteSheet == null || quadMesh == null) return;
        glClear(GL_DEPTH_BUFFER_BIT);

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

        Vector3f animationOffset = getAnimationOffset();
        float animationRotation = getAnimationRotation();

        baseModelMatrix.identity()
                .translate(
                        OFFSET_X + animationOffset.x,
                        OFFSET_Y + animationOffset.y,
                        OFFSET_Z + animationOffset.z
                )
                .rotateX((float) Math.toRadians(rotateX))
                .rotateY((float) Math.toRadians(
                        rotateY + animationRotation))
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