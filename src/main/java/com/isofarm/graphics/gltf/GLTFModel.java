package com.isofarm.graphics.gltf;

import com.isofarm.graphics.Shader;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class GLTFModel {

    private final List<GLTFMesh> meshes;
    private final List<GLTFNode> nodes;
    private final List<GLTFNode> rootNodes;

    public GLTFModel() {
        this.meshes = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.rootNodes = new ArrayList<>();
    }

    void addMesh(GLTFMesh mesh) {
        meshes.add(mesh);
    }

    void addNode(GLTFNode node) {
        nodes.add(node);
    }

    void addRootNode(GLTFNode node) {
        rootNodes.add(node);
    }

    public void render(Shader shader, Matrix4f modelMatrix) {
        shader.bind();

        for (GLTFNode node : rootNodes) {
            node.render(this, modelMatrix, shader);
        }
    }

    void renderMesh(int meshIndex, Matrix4f worldMatrix, Shader shader) {
        if (meshIndex < 0 || meshIndex >= meshes.size()) {
            return;
        }

        shader.setUniform("uModel", worldMatrix);

        meshes.get(meshIndex).render(shader);
    }

    public GLTFNode findNode(String name) {
        for (GLTFNode node : rootNodes) {
            GLTFNode result = node.find(name);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public List<GLTFNode> getNodes() {
        return nodes;
    }

    public List<GLTFNode> getRootNodes() {
        return rootNodes;
    }

    public List<GLTFMesh> getMeshes() {
        return meshes;
    }

    public void updateTransforms() {
        for (GLTFNode node : rootNodes) {
            node.updateTransform();
        }
    }

    public void dispose() {
        for (GLTFMesh mesh : meshes) {
            mesh.dispose();
        }

        meshes.clear();
        nodes.clear();
        rootNodes.clear();
    }

    public static class GLTFMesh {
        private final int vao;
        private final int vbo;
        private final int ebo;

        private final int indexCount;
        private final int textureId;

        GLTFMesh(int vao, int vbo, int ebo, int indexCount, int textureId) {
            this.vao = vao;
            this.vbo = vbo;
            this.ebo = ebo;
            this.indexCount = indexCount;
            this.textureId = textureId;
        }

        void render(Shader shader) {
            if (textureId > 0) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, textureId);

                shader.setUniform("uTexture", 0);
                shader.setUniform("uUseTexture", true);
            }

            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        void dispose() {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
            glDeleteBuffers(ebo);

            if (textureId > 0) {
                glDeleteTextures(textureId);
            }
        }
    }
}