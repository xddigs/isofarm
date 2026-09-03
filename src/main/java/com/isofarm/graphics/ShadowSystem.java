package com.isofarm.graphics;

import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.GameMaster;
import com.isofarm.entity.Player;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class ShadowSystem {
    public static final ShadowSystem sys = new ShadowSystem();
    private static final float SHADOW_DISTANCE = 100.0f;
    private static final float SHADOW_SIZE = 70.0f;
    private static final float SHADOW_NEAR = 1.0f;
    private static final float SHADOW_FAR = 220.0f;

    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f lightSpace = new Matrix4f();

    private final Vector3f lightPosition = new Vector3f();
    private final Vector3f target = new Vector3f();
    private final Vector3f lightDirection = new Vector3f();

    private final Matrix4f modelMatrix = new Matrix4f();

    public void render(GameMaster gameMaster, ResourceManager rm,
                       Map<Chunk, ChunkMeshBuilder.ChunkRenderMesh> chunkMeshes) {
        ShadowMap shadowMap = gameMaster.getShadowMap();
        updateLightMatrix(gameMaster);
        shadowMap.bind();

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);

        Shader shadowShader = rm.getShadowMapShader();
        shadowShader.bind();
        shadowShader.setUniform("uLightSpaceMatrix", lightSpace);

        for (Map.Entry<Chunk, ChunkMeshBuilder.ChunkRenderMesh> entry : chunkMeshes.entrySet()) {
            Chunk chunk = entry.getKey();
            ChunkMeshBuilder.ChunkRenderMesh chunkMesh = entry.getValue();
            if (chunkMesh == null) {
                continue;
            }

            if (chunkMesh.solidMesh() == null || chunkMesh.solidMesh().getIndicesCount() <= 0) {
                continue;
            }

            float worldX = chunk.getChunkX() * Chunk.SIZE_X;
            float worldZ = chunk.getChunkZ() * Chunk.SIZE_Z;
            modelMatrix.identity().translate(worldX, 0.0f, worldZ);
            shadowShader.setUniform("uModel", modelMatrix);
            chunkMesh.solidMesh().render();
        }

        shadowShader.unbind();
        glCullFace(GL_BACK);
        shadowMap.unbind((int) gameMaster.getWindowWidth(),
                (int) gameMaster.getWindowHeight());
    }

    private void updateLightMatrix(GameMaster gameMaster) {
        lightDirection.set(gameMaster.getCelestialLighting().getDirection()).normalize();

        Player player = gameMaster.getPlayer();
        if (player != null) {
            target.set(player.getPosition());
        } else {
            target.set(0.0f, 0.0f, 0.0f);
        }

        lightPosition.set(target).sub(new Vector3f(lightDirection).mul(SHADOW_DISTANCE));
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

        if (Math.abs(lightDirection.y) > 0.98f) {
            up.set(0.0f, 0.0f, 1.0f);
        }

        projection.identity().ortho(-SHADOW_SIZE, SHADOW_SIZE, -SHADOW_SIZE, SHADOW_SIZE, SHADOW_NEAR, SHADOW_FAR);
        view.identity().lookAt(lightPosition, target, up);
        lightSpace.set(projection).mul(view);
    }

    public Matrix4f getLightSpaceMatrix() {
        return lightSpace;
    }
}