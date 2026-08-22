package com.isofarm.graphics;

import com.isofarm.data.Crop;
import com.isofarm.data.Hit;
import com.isofarm.entity.Player;
import com.isofarm.input.GameInteraction;
import com.isofarm.service.TimeService;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.GameMaster;
import org.joml.*;

import java.lang.Math;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class GameRenderer {
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Matrix4f viewProjMatrix = new Matrix4f();
    private final FrustumIntersection frustum = new FrustumIntersection();
    private final Matrix4f lightProjection = new Matrix4f();
    private final Matrix4f lightView = new Matrix4f();
    private final Matrix4f lightSpaceMatrix = new Matrix4f();
    private final Vector3f lightPosition = new Vector3f();
    private final Vector3f lightTarget = new Vector3f();
    private final Vector3f upVector = new Vector3f(0.0f, 1.0f, 0.0f);
    private float previousCameraYaw;
    private float previousCameraPitch;
    private float blurX;
    private float blurY;

    public void render(GameMaster gameMaster, ResourceManager rm, Map<Chunk, Mesh> chunkMeshes) {
        renderShadowPass(gameMaster, rm, chunkMeshes);
        CameraView camera = gameMaster.getActiveCamera();
        float windowWidth = gameMaster.getWindowWidth();
        float windowHeight = gameMaster.getWindowHeight();
        Framebuffer sceneFbo = gameMaster.getSceneFbo();
        Framebuffer maskFbo = gameMaster.getMaskFbo();

        sceneFbo.bind();
        glViewport(0, 0, (int) windowWidth, (int) windowHeight);

        Vector3f skyColor = TimeService.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glActiveTexture(GL_TEXTURE0);
        Shader defaultShader = rm.getDefaultShader();
        defaultShader.bind();
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, gameMaster.getShadowMap().getDepthTexture());
        defaultShader.setUniform("uEnableShadows", true);
        defaultShader.setUniform("uShadowMap", 1);
        defaultShader.setUniform("uIsMaskPass", false);

        defaultShader.setUniform("uProjection", camera.getProjectionMatrix());
        defaultShader.setUniform("uView", camera.getViewMatrix());

        CelestialLighting lighting = gameMaster.getCelestialLighting();
        defaultShader.setUniform("uSunColor", lighting.getColor());
        defaultShader.setUniform("uLightIntensity", lighting.getIntensity());
        defaultShader.setUniform("uLightDirection", lighting.getDirection());
        defaultShader.setUniform("uAmbientIntensity", lighting.getAmbientIntensity());
        defaultShader.setUniform("uSkyColor", TimeService.getSkyColor());
        defaultShader.setUniform("uLightSpaceMatrix", lightSpaceMatrix);

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

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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

        Player player = gameMaster.getPlayer();
        Hit hoveredCell = HoveredCell.get(gameMaster);

        if (hoveredCell != null && player != null) {
            player.lookAt(hoveredCell.x() + 0.5f, hoveredCell.z() + 0.5f,
                    gameMaster.getActiveCamera().getYaw());
        }

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

        renderDestroyOverlay(gameMaster.getGameInteraction(), defaultShader,
                rm.getDestroyOverlayMesh(), rm.getDestroyTexture(), camera);

        gameMaster.getEntities().forEach(entity -> {
            if (entity instanceof Player && !gameMaster.isOrthographicCamera()) {
                return;
            }
            entity.render(gameMaster);
        });

        glDisable(GL_DEPTH_TEST);
        gameMaster.getParticles().render(defaultShader, rm.getSpriteMesh(),
                gameMaster.getActiveCamera());
        glEnable(GL_DEPTH_TEST);

        if (gameMaster.getWeatherService().isRaining()) {
            Vector3f rainTargetPos;
            if (gameMaster.isOrthographicCamera() && player != null) {
                rainTargetPos = new Vector3f(player.getPosition().x(),
                        player.getPosition().y() + 10.0f,
                        player.getPosition().z());
            } else {
                rainTargetPos = camera.getPosition();
            }

            gameMaster.getRainEngine().render(rm.getRainShader(),
                    camera.getViewMatrix(), camera.getProjectionMatrix(),
                    rainTargetPos, gameMaster.getWorld());
        }

        if (blocksTexture != null) blocksTexture.unbind();
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

        if (gameMaster.isInventoryOpen()) {
            glDisable(GL_DEPTH_TEST);
            Shader blurShader = rm.getBlurShader();
            Vector2f resolution = new Vector2f(windowWidth, windowHeight);

            Framebuffer blurFbo = gameMaster.getBlurFbo();
            blurFbo.bind();
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            blurShader.bind();
            blurShader.setUniform("uResolution", resolution);
            blurShader.setUniform("uDirection", new Vector2f(1.0f, 0.0f));
            blurShader.setUniform("uBlurRadius", 5.0f);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, sceneFbo.getTextureId());
            blurShader.setUniform("screenTexture", 0);
            rm.getScreenQuadMesh().render();
            blurShader.unbind();
            blurFbo.unbind((int) windowWidth,(int) windowHeight);

            glClear(GL_COLOR_BUFFER_BIT);
            blurShader.bind();
            blurShader.setUniform("uResolution", resolution);
            blurShader.setUniform("uDirection", new Vector2f(0.0f, 1.0f));
            blurShader.setUniform("uBlurRadius", 3.0f);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, blurFbo.getTextureId());
            blurShader.setUniform("screenTexture", 0);

            rm.getScreenQuadMesh().render();
            blurShader.unbind();
            glEnable(GL_DEPTH_TEST);

        } else {
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
        }

        if (player.isNoClip()) {
            glDisable(GL_CULL_FACE);
            // glDisable(GL_DEPTH_TEST);
        } else {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            glEnable(GL_DEPTH_TEST);
        }
    }

    private void renderShadowPass(GameMaster gameMaster, ResourceManager rm,
                                  Map<Chunk, Mesh> chunkMeshes) {
        ShadowMap shadowMap = gameMaster.getShadowMap();
        Shader shadowShader = rm.getShadowMapShader();

        updateLightSpaceMatrix(gameMaster);
        shadowMap.bind();
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        shadowShader.bind();
        shadowShader.setUniform("uLightSpaceMatrix", lightSpaceMatrix);
        chunkMeshes.forEach((chunk, mesh) -> {
            if (mesh == null || mesh.getIndicesCount() <= 0) {
                return;
            }
            float minX = chunk.getChunkX() * Chunk.SIZE_X;
            float minZ = chunk.getChunkZ() * Chunk.SIZE_Z;
            modelMatrix.identity().translate(minX, 0.0f, minZ);
            shadowShader.setUniform("uModel", modelMatrix);
            mesh.render();
        });

        gameMaster.getWorld().forEach(block -> {
            if (!(block instanceof Crop crop)) {
                return;
            }

            float renderX = crop.getX() + 0.5f;
            float renderY = crop.getY() + K.World.SHORTER_BLOCK_HEIGHT;
            float renderZ = crop.getZ() + 0.5f;

            modelMatrix.identity()
                    .translate(renderX, renderY, renderZ);

            shadowShader.setUniform("uModel", modelMatrix);
            rm.getSpriteMesh().render();
        });

        shadowShader.unbind();
        shadowMap.unbind((int) gameMaster.getWindowWidth(), (int) gameMaster.getWindowHeight());
    }

    private void updateLightSpaceMatrix(GameMaster gameMaster) {
        CameraView camera = gameMaster.getActiveCamera();
        Vector3f cameraPosition = new Vector3f(camera.getPosition());
        Vector3f lightDirection = new Vector3f(gameMaster.getCelestialLighting().getDirection()).normalize();

        lightTarget.set(cameraPosition);
        lightPosition.set(cameraPosition).sub(new Vector3f(lightDirection).mul(80.0f));

        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        if (Math.abs(lightDirection.y) > 0.98f) {
            up.set(0.0f, 0.0f, 1.0f);
        }

        lightProjection.identity().ortho(-60.0f, 60.0f, -60.0f, 60.0f, 1.0f, 180.0f);
        lightView.identity().lookAt(lightPosition, lightTarget, up);

        lightSpaceMatrix.set(lightProjection).mul(lightView);
        Vector4f shadowCoord = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f).mul(lightSpaceMatrix);
        shadowCoord.mul(Settings.getShadowMapSize() / 2.0f);

        float roundedX = Math.round(shadowCoord.x);
        float roundedY = Math.round(shadowCoord.y);
        float dx = (roundedX - shadowCoord.x) * (2.0f / Settings.getShadowMapSize());
        float dy = (roundedY - shadowCoord.y) * (2.0f / Settings.getShadowMapSize());

        Matrix4f texelFix = new Matrix4f().translate(dx, dy, 0.0f);
        lightProjection.mul(texelFix);
        lightSpaceMatrix.set(lightProjection).mul(lightView);
    }

    public void renderDestroyOverlay(GameInteraction interaction,
                                     Shader shader,
                                     Mesh blockMesh,
                                     SpriteSheet destroyTexture,
                                     CameraView camera) {

        if (!interaction.isBreakingBlock() || destroyTexture == null) {
            return;
        }

        Vector3i pos = interaction.getBreakingBlockPos();
        float progress = Math.clamp(interaction.getBreakProgress(), 0.0f, 1.0f);
        int totalFrames = destroyTexture.getTotalFrames();
        if (totalFrames <= 0) return;

        int frameIndex = Math.clamp((int) (progress * totalFrames), 0, totalFrames - 1);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(false);
        glDisable(GL_CULL_FACE);

        shader.bind();
        destroyTexture.bind();
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);
        shader.setUniform("uTexture", K.Render.PRIMARY_TEXTURE_UNIT);
        shader.setUniform("uTotalFrames", totalFrames);
        shader.setUniform("uFrameIndex", frameIndex);

        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uProjection", camera.getProjectionMatrix());
        shader.setUniform("uView", camera.getViewMatrix());

        modelMatrix.identity().translate(pos.x, pos.y, pos.z).scale(1.001f);
        shader.setUniform("uModel", modelMatrix);
        blockMesh.render();
        destroyTexture.unbind();
        shader.unbind();

        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
    }

    private void updateBlur(CameraView camera) {
        float yawDelta = camera.getYaw() - previousCameraYaw;
        if (yawDelta > K.Camera.HALF_DEGREES) yawDelta -= K.Camera.FULL_DEGREES;
        else if (yawDelta < -K.Camera.HALF_DEGREES) yawDelta += K.Camera.FULL_DEGREES;

        float pitchDelta = camera.getPitch() - previousCameraPitch;
        previousCameraYaw = camera.getYaw();
        previousCameraPitch = camera.getPitch();
        blurX = yawDelta / K.Camera.FULL_DEGREES;
        blurY = pitchDelta / K.Camera.HALF_DEGREES;
    }

    public void initCamera(CameraView camera) {
        previousCameraYaw = camera.getYaw();
        previousCameraPitch = camera.getPitch();
    }
}
