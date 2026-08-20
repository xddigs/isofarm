package com.soilcraft.graphics;

import com.soilcraft.wrld.GameMaster;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CharacterRenderer {
    private static final Vector3f WHITE = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Matrix4f modelMatrix = new Matrix4f();

    public void render(GameMaster gameMaster, CharacterModel model, Vector3f worldPosition) {
        if (model == null || worldPosition == null) return;
        ResourceManager rm = gameMaster.getResourceManager();
        Shader shader = rm.getDefaultShader();
        Mesh cubeMesh = rm.getCharacterCubeMesh();

        shader.bind();
        shader.setUniform("uUseTexture", false);
        shader.setUniform("uUseFaceAtlas", false);
        shader.setUniform("uBaseColor", WHITE);

        float modelRotY = model.getRotationY();
        renderPart(shader, cubeMesh, model.getBody(), worldPosition, modelRotY);
        renderPart(shader, cubeMesh, model.getLeftArm(), worldPosition, modelRotY);
        renderPart(shader, cubeMesh, model.getRightArm(), worldPosition, modelRotY);
        renderPart(shader, cubeMesh, model.getLeftLeg(), worldPosition, modelRotY);
        renderPart(shader, cubeMesh, model.getRightLeg(), worldPosition, modelRotY);
    }

    private void renderPart(Shader shader, Mesh mesh,
                            BodyPart part, Vector3f worldPosition, float modelRotY) {
        Vector3f localPosition = part.getPosition();
        Vector3f localRotation = part.getRotation();
        Vector3f size = part.getSize();

        modelMatrix.identity()
                .translate(worldPosition)
                .rotateY(modelRotY)
                .translate(localPosition)
                .rotateXYZ(localRotation.x, localRotation.y, localRotation.z)
                .scale(size);

        shader.setUniform("uModel", modelMatrix);
        mesh.render();
    }
}