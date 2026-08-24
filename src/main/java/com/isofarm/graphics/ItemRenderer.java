package com.isofarm.graphics;

import com.isofarm.entity.WorldItem;
import com.isofarm.input.CameraController;
import com.isofarm.item.Block;
import com.isofarm.item.Item;
import com.isofarm.item.Tool;
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

    private static final float OFFSET_X = 1.1f;
    private static final float OFFSET_Y = -0.60f;
    private static final float OFFSET_Z = -1.0f;
    private static final float SWAY_LAG = 50.0f;

    private static final float ITEM_SCALE = 1.2f;
    private static final int THICKNESS_LAYERS = 24;
    private static final float LAYER_DEPTH = 0.0025f;

    private static final float ANIMATION_DURATION = 0.38f;

    private final Matrix4f baseModelMatrix;
    private final Mesh quadMesh;

    private enum ActionAnimation {
        NONE,
        ATTACK,
        BREAK,
        INTERACT
    }

    private ActionAnimation currentAnimation = ActionAnimation.NONE;
    private float animationTime = 0.0f;

    private float swayX;
    private float swayY;

    private float previousYaw;
    private float previousPitch;

    public ItemRenderer() {
        this.baseModelMatrix = new Matrix4f();
        this.quadMesh = Mesh.createCenteredQuad();
    }

    public void playAttackAnimation() {
        startAnimation(ActionAnimation.ATTACK);
    }

    public void playBreakAnimation() {
        if (currentAnimation == ActionAnimation.BREAK) return;
        startAnimation(ActionAnimation.BREAK);
    }

    public void playInteractAnimation() {
        startAnimation(ActionAnimation.INTERACT);
    }

    public void playPlaceAnimation() {
        playInteractAnimation();
    }

    public void stopAnimation() {
        currentAnimation = ActionAnimation.NONE;
        animationTime = 0.0f;
    }

    private void startAnimation(ActionAnimation animation) {
        currentAnimation = animation;
        animationTime = 0.0f;
    }

    public void update(GameMaster gameMaster, float delta) {
        CameraController cameraController = gameMaster.getCameraController();
        updateCameraSway(cameraController);

        if (currentAnimation == ActionAnimation.NONE) {
            return;
        }

        animationTime += delta;
        if (animationTime >= ANIMATION_DURATION) {
            if (currentAnimation == ActionAnimation.BREAK) {
                animationTime -= ANIMATION_DURATION;
            } else {
                animationTime = ANIMATION_DURATION;
                currentAnimation = ActionAnimation.NONE;
            }
        }
    }

    public boolean isAnimationPlaying() {
        return currentAnimation != ActionAnimation.NONE && animationTime < ANIMATION_DURATION;
    }

    public boolean isAttackAnimationPlaying() {
        return currentAnimation == ActionAnimation.ATTACK && animationTime < ANIMATION_DURATION;
    }

    public boolean isBreakAnimationPlaying() {
        return currentAnimation == ActionAnimation.BREAK && animationTime < ANIMATION_DURATION;
    }

    public boolean isInteractAnimationPlaying() {
        return currentAnimation == ActionAnimation.INTERACT && animationTime < ANIMATION_DURATION;
    }

    private float animationProgress() {
        if (ANIMATION_DURATION <= 0.0f) return 1.0f;
        return clamp01(animationTime / ANIMATION_DURATION);
    }

    private float clamp01(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }

    private float easeOutCubic(float t) {
        t = clamp01(t);
        return 1.0f - (float) Math.pow(1.0f - t, 3.0f);
    }

    private AnimationTransform getAnimationTransform() {
        float t = animationProgress();
        return switch (currentAnimation) {
            case ATTACK -> getAttackTransform(t);
            case BREAK -> getBreakTransform(t);
            case INTERACT -> getInteractTransform(t);
            case NONE -> new AnimationTransform();
        };
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

    private void updateCameraSway(CameraController camera) {
        float yaw = camera.getCamera().getYaw();
        float pitch = camera.getCamera().getPitch();
        float yawDelta = yaw - previousYaw;
        float pitchDelta = pitch - previousPitch;

        if (yawDelta > 180.0f) yawDelta -= 360.0f;
        if (yawDelta < -180.0f) yawDelta += 360.0f;

        swayX = Math.clamp(swayX - yawDelta * 0.0025f, -0.08f, 0.08f);
        swayY = Math.clamp(swayY + pitchDelta * 0.0025f, -0.08f, 0.08f);

        swayX *= 0.85f;
        swayY *= 0.85f;

        previousYaw = yaw;
        previousPitch = pitch;
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

        int frameIndex = item instanceof Block ? item.getId() - 1 : item.getId();
        frameIndex = Math.clamp(frameIndex, 0, spriteSheet.getTotalFrames() - 1);
        Vector4f uvBounds = spriteSheet.getUVBounds(frameIndex);
        int textureUnit = com.isofarm.utils.K.Render.PRIMARY_TEXTURE_UNIT;

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

    public void render(GameMaster gameMaster, Item item, SpriteSheet spriteSheet,
                       Shader shader, CelestialLighting lighting) {

        if (item == null || spriteSheet == null || quadMesh == null) {
            return;
        }

        if (gameMaster.isOrthographicCamera()) {
            return;
        }

        boolean isTool = item instanceof Tool;
        float rotateX = isTool ? 10.0f : 0.0f;
        float rotateY = isTool ? -90.0f : -15.0f;
        float rotateZ = isTool ? 48.0f : 0.0f;

        AnimationTransform animation = getAnimationTransform();
        float x = OFFSET_X + animation.x + swayX;
        float y = OFFSET_Y + animation.y + swayY;
        float z = OFFSET_Z + animation.z;

        baseModelMatrix.identity().translate(x, y, z)
                .rotateX((float) Math.toRadians(rotateX + animation.rotateX + swayY * SWAY_LAG))
                .rotateY((float) Math.toRadians(rotateY + animation.rotateY + swayX * SWAY_LAG))
                .rotateZ((float) Math.toRadians(rotateZ + animation.rotateZ))
                .scale(ITEM_SCALE * animation.scaleX, -ITEM_SCALE * animation.scaleY, ITEM_SCALE * animation.scaleZ);

        int frameIndex = item instanceof Block ? item.getId() - 1 : item.getId();
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
        shader.setUniform("uView", new Matrix4f().identity());
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uIsMaskPass", false);
        shader.setUniform("uEnableShadows", false);
        shader.setUniform("uBaseColor", new Vector3f(1.0f));
        setupCommonShaderUniforms(shader, gameMaster, lighting);
        glDisable(GL_CULL_FACE);
        for (int i = THICKNESS_LAYERS - 1; i >= 0; i--) {
            Matrix4f layerMatrix = new Matrix4f(baseModelMatrix).translate(0.0f, 0.0f, -i * LAYER_DEPTH);
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