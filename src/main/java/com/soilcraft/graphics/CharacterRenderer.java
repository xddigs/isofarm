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
        renderPart(shader, cubeMesh, model.getBody(), worldPosition);
        renderPart(shader, cubeMesh, model.getLeftArm(), worldPosition);
        renderPart(shader, cubeMesh, model.getRightArm(), worldPosition);
        renderPart(shader, cubeMesh, model.getLeftLeg(), worldPosition);
        renderPart(shader, cubeMesh, model.getRightLeg(), worldPosition);
    }

    private void renderPart(Shader shader, Mesh mesh, BodyPart part,
                            Vector3f worldPosition) {
        Vector3f position = new Vector3f(worldPosition).add(part.getPosition());
        Vector3f rotation = part.getRotation();
        Vector3f size = part.getSize();
        modelMatrix.identity().translate(position).rotateXYZ(
                rotation.x, rotation.y, rotation.z).scale(size);

        shader.setUniform("uModel", modelMatrix);
        mesh.render();
    }
}