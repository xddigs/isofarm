package com.tilled.graphics;

import com.tilled.data.Crop;
import com.tilled.data.Hit;
import com.tilled.service.TimeService;
import com.tilled.utils.K;
import com.tilled.wrld.Chunk;
import com.tilled.wrld.GameMaster;
import com.tilled.utils.Settings;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class GameRenderer {
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Matrix4f viewProjMatrix = new Matrix4f();
    private final FrustumIntersection frustum = new FrustumIntersection();

    private float previousCameraYaw;
    private float previousCameraPitch;
    private float blurX;
    private float blurY;

    public void render(GameMaster gameMaster, ResourceManager rm, Map<Chunk, Mesh> chunkMeshes) {
        Camera camera = gameMaster.getCamera();
        float windowWidth = gameMaster.getWindowWidth();
        float windowHeight = gameMaster.getWindowHeight();
        Framebuffer sceneFbo = gameMaster.getSceneFbo();
        Framebuffer maskFbo = gameMaster.getMaskFbo();

        sceneFbo.bind();
        glClearColor(0.15f, 0.15f, 0.20f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glActiveTexture(GL_TEXTURE0);
        Shader defaultShader = rm.getDefaultShader();
        defaultShader.bind();
        defaultShader.setUniform("uIsMaskPass", false);

        defaultShader.setUniform("uProjection", camera.getProjectionMatrix());
        defaultShader.setUniform("uView", camera.getViewMatrix());

        defaultShader.setUniform("uSunColor", TimeService.getSunLightColor());
        defaultShader.setUniform("uLightIntensity", TimeService.getSunIntensity());
        defaultShader.setUniform("uLightDirection", gameMaster.getSunlight().getDirection());

        defaultShader.setUniform("uTotalFrames", 1);
        defaultShader.setUniform("uFrameIndex", 0);
        defaultShader.setUniform("uUseFaceAtlas", false);

        SpriteSheet blocksTexture = rm.getBlocksTexture();
        if (blocksTexture != null) {
            blocksTexture.bind();
            defaultShader.setUniform("uUseTexture", true);
            defaultShader.setUniform("uUseFaceAtlas", true);
            defaultShader.setUniform("uTexture", K.Render.PRIMARY_TEXTURE_UNIT);
            defaultShader.setUniform("uTotalFrames", 1);
            defaultShader.setUniform("uFrameIndex", 0);
            defaultShader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
            defaultShader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
        }

        viewProjMatrix.set(camera.getProjectionMatrix()).mul(camera.getViewMatrix());
        frustum.set(viewProjMatrix);

        updateBlur(camera);

        chunkMeshes.forEach((chunk, mesh) -> {
            if (mesh != null && mesh.getIndicesCount() > 0) {
                float minX = chunk.getChunkX() * Chunk.SIZE_X;
                float minY = 0;
                float minZ = chunk.getChunkZ() * Chunk.SIZE_Z;
                float maxX = minX + Chunk.SIZE_X;
                float maxY = Chunk.SIZE_Y;
                float maxZ = minZ + Chunk.SIZE_Z;
                if (frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ)) {
                    modelMatrix.identity().translate(minX, 0, minZ);
                    defaultShader.setUniform("uModel", modelMatrix);
                    mesh.render();
                }
            }
        });

        gameMaster.getWorld().forEach(block -> {
            if (!(block instanceof Crop crop)) return;

            SpriteSheet sheet = rm.getCropSpritesheets().get(crop.getCropType());
            if (sheet == null) return;

            sheet.bind();
            defaultShader.setUniform("uUseTexture", true);
            defaultShader.setUniform("uTexture", K.Render.PRIMARY_TEXTURE_UNIT);
            defaultShader.setUniform("uUseFaceAtlas", false);
            defaultShader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
            defaultShader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
            defaultShader.setUniform("uTotalFrames", sheet.getTotalFrames());
            defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());

            float renderX = crop.getX() + 0.5f;
            float renderY = crop.getY() + K.World.SHORTER_BLOCK_HEIGHT;
            float renderZ = crop.getZ() + 0.5f;

            modelMatrix.identity().translate(renderX, renderY, renderZ);
            defaultShader.setUniform("uModel", modelMatrix);
            rm.getSpriteMesh().render();
            sheet.unbind();
        });

        gameMaster.getParticles().render(defaultShader, rm.getSpriteMesh());

        if (blocksTexture != null) blocksTexture.unbind();

        Hit hoveredCell = gameMaster.getHoveredCell();
        if (hoveredCell != null) {
            glEnable(GL_DEPTH_TEST);
            glDepthMask(false);
            defaultShader.bind();
            defaultShader.setUniform("uUseTexture", false);
            defaultShader.setUniform("uUseFaceAtlas", false);
            defaultShader.setUniform("uBaseColor", K.Colors.OUTLINE_DEFAULT);
            modelMatrix.identity().translate(hoveredCell.x(), hoveredCell.y(), hoveredCell.z());
            defaultShader.setUniform("uModel", modelMatrix);
            rm.getSelectionMesh().renderLines();
            glDepthMask(true);
        }
        if (hoveredCell != null) {
            maskFbo.bind();

            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            defaultShader.bind();
            defaultShader.setUniform("uIsMaskPass", true);
            defaultShader.setUniform("uUseTexture", true);
            defaultShader.setUniform("uUseFaceAtlas", false);
            defaultShader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
            defaultShader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));

            maskFbo.unbind((int) windowWidth, (int) windowHeight);

            sceneFbo.bind();

            glDisable(GL_DEPTH_TEST);

            Shader outlineShader = rm.getOutlineShader();
            outlineShader.bind();
            outlineShader.setUniform("uScreenSize", new Vector2f(windowWidth, windowHeight));
            outlineShader.setUniform("uOutlineColor", K.Colors.OUTLINE_DEFAULT);
            outlineShader.setUniform("uMaskTexture", K.Render.PRIMARY_TEXTURE_UNIT);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, maskFbo.getTextureId());

            rm.getScreenQuadMesh().render();

            outlineShader.unbind();

            glEnable(GL_DEPTH_TEST);

            defaultShader.bind();
            defaultShader.setUniform("uIsMaskPass", false);
        }

        defaultShader.unbind();
        sceneFbo.unbind((int) windowWidth, (int) windowHeight);

        glDisable(GL_DEPTH_TEST);
        Shader motionBlurShader = rm.getMotionBlurShader();
        motionBlurShader.bind();
        motionBlurShader.setUniform("uScene", 0);
        motionBlurShader.setUniform("uVelocity", new Vector2f(blurX, blurY));
        motionBlurShader.setUniform("uStrength", Settings.doEnableMotions());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sceneFbo.getTextureId());
        rm.getScreenQuadMesh().render();
        motionBlurShader.unbind();
        glEnable(GL_DEPTH_TEST);

        if (gameMaster.getWeatherService().isRaining()) {
            gameMaster.getRainEngine().render(rm.getRainShader(), camera.getViewMatrix(), camera.getProjectionMatrix());
        }

        gameMaster.getGameUIService().render(gameMaster.isHUDShown(), gameMaster);
    }

    private void updateBlur(Camera camera) {
        float yawDelta = camera.getYaw() - previousCameraYaw;
        if (yawDelta > K.Camera.HALF_DEGREES) yawDelta -= K.Camera.FULL_DEGREES;
        else if (yawDelta < -K.Camera.HALF_DEGREES) yawDelta += K.Camera.FULL_DEGREES;

        float pitchDelta = camera.getPitch() - previousCameraPitch;
        previousCameraYaw = camera.getYaw();
        previousCameraPitch = camera.getPitch();
        blurX = yawDelta / K.Camera.FULL_DEGREES;
        blurY = pitchDelta / K.Camera.HALF_DEGREES;
    }

    public void initCamera(Camera camera) {
        previousCameraYaw = camera.getYaw();
        previousCameraPitch = camera.getPitch();
    }
}
