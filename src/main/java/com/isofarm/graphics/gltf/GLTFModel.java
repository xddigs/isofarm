package com.isofarm.graphics.gltf;

import com.isofarm.graphics.Shader;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

/**
 * Provides gltfmodel behavior.
 */
public class GLTFModel {
    private final List<GLTFMesh> meshes;
    private final List<GLTFNode> nodes;
    private final List<GLTFNode> rootNodes;

    /**
     * Creates a new {@code GLTFModel} instance.
     */
    public GLTFModel() {
        this.meshes = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.rootNodes = new ArrayList<>();
    }

    /**
     * Adds the mesh.
     * @param mesh the mesh value
     */
    public void addMesh(GLTFMesh mesh) {
        meshes.add(mesh);
    }

    /**
     * Adds the node.
     * @param node the node value
     */
    public void addNode(GLTFNode node) {
        nodes.add(node);
    }

    /**
     * Adds the root node.
     * @param node the node value
     */
    public void addRootNode(GLTFNode node) {
        rootNodes.add(node);
    }

    /**
     * Renders render.
     * @param shader the shader value
     * @param modelMatrix the model matrix value
     */
    public void render(Shader shader, Matrix4f modelMatrix) {
        shader.bind();

        for (GLTFNode node : rootNodes) {
            node.render(this, modelMatrix, shader);
        }
    }

    /**
     * Renders the mesh.
     * @param meshIndex the mesh index value
     * @param worldMatrix the world matrix value
     * @param shader the shader value
     */
    public void renderMesh(int meshIndex, Matrix4f worldMatrix, Shader shader) {
        if (meshIndex < 0 || meshIndex >= meshes.size()) {
            return;
        }

        shader.setUniform("uModel", worldMatrix);
        meshes.get(meshIndex).render(shader);
    }

    /**
     * Renders the mesh.
     * @param meshIndex the mesh index value
     * @param worldMatrix the world matrix value
     * @param shader the shader value
     * @param textureId the texture id value
     * @param uvBounds the uv bounds value
     */
    public void renderMesh(int meshIndex, Matrix4f worldMatrix, Shader shader, int textureId, Vector4f uvBounds) {
        if (meshIndex < 0 || meshIndex >= meshes.size()) {
            return;
        }

        shader.setUniform("uModel", worldMatrix);

        meshes.get(meshIndex).render(shader, textureId, uvBounds);
    }

    /**
     * Finds and returns the node.
     * @param name the name value
     * @return the located node
     */
    public GLTFNode findNode(String name) {
        for (GLTFNode node : rootNodes) {
            GLTFNode result = node.find(name);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Returns the texture.
     * @return the texture
     */
    public int getTexture() {
        for (GLTFMesh mesh : meshes) {
            if (mesh.getTextureId() > 0) {
                return mesh.getTextureId();
            }
        }

        return 0;
    }

    /**
     * Returns the nodes.
     * @return the nodes
     */
    public List<GLTFNode> getNodes() {
        return nodes;
    }

    /**
     * Returns the root nodes.
     * @return the root nodes
     */
    public List<GLTFNode> getRootNodes() {
        return rootNodes;
    }

    /**
     * Returns the meshes.
     * @return the meshes
     */
    public List<GLTFMesh> getMeshes() {
        return meshes;
    }

    /**
     * Updates the transforms.
     */
    public void updateTransforms() {
        for (GLTFNode node : rootNodes) {
            node.updateTransform();
        }
    }

    /**
     * Performs the dispose operation.
     */
    public void dispose() {
        for (GLTFMesh mesh : meshes) {
            mesh.dispose();
        }

        meshes.clear();
        nodes.clear();
        rootNodes.clear();
    }

    /**
     * Provides gltfmesh behavior.
     */
    public static class GLTFMesh {
        private final int vao;
        private final int vbo;
        private final int ebo;

        private final int indexCount;
        private final int textureId;

        /**
         * Creates a new {@code GLTFMesh} instance.
         * @param vao the vao value
         * @param vbo the vbo value
         * @param ebo the ebo value
         * @param indexCount the index count value
         * @param textureId the texture id value
         */
        public GLTFMesh(int vao, int vbo, int ebo, int indexCount, int textureId) {
            this.vao = vao;
            this.vbo = vbo;
            this.ebo = ebo;
            this.indexCount = indexCount;
            this.textureId = textureId;
        }

        /**
         * Returns the texture id.
         * @return the texture id
         */
        public int getTextureId() {
            return textureId;
        }

        /**
         * Renders render.
         * @param shader the shader value
         */
        public void render(Shader shader) {
            render(shader, textureId, new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        }

        /**
         * Renders render.
         * @param shader the shader value
         * @param renderTextureId the render texture id value
         * @param uvBounds the uv bounds value
         */
        public void render(Shader shader, int renderTextureId,
                           Vector4f uvBounds) {
            glActiveTexture(GL_TEXTURE0);

            if (renderTextureId > 0) {
                glBindTexture(GL_TEXTURE_2D, renderTextureId);

                shader.setUniform("uTexture", 0);
                shader.setUniform("uUseTexture", true);
                shader.setUniform("uUVBounds", uvBounds);
            } else {
                shader.setUniform("uUseTexture", false);
                shader.setUniform("uUVBounds",
                        new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
            }

            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }

        /**
         * Performs the dispose operation.
         */
        public void dispose() {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
            glDeleteBuffers(ebo);

            if (textureId > 0) {
                glDeleteTextures(textureId);
            }
        }
    }
}