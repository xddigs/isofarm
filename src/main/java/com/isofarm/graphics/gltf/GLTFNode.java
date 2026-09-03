package com.isofarm.graphics.gltf;

import com.isofarm.graphics.Shader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides gltfnode behavior.
 */
public class GLTFNode {

    private final String name;
    private final int meshIndex;

    private final Vector3f translation;
    private final Quaternionf rotation;
    private final Vector3f scale;

    private final Matrix4f localMatrix;
    private final Matrix4f worldMatrix;

    private final List<GLTFNode> children;

    private boolean isVisible = true;

    private int textureOverride = 0;
    private Vector4f uvOverride = null;

    /**
     * Creates a new {@code GLTFNode} instance.
     * @param name the name value
     * @param meshIndex the mesh index value
     * @param translation the translation value
     * @param rotation the rotation value
     * @param scale the scale value
     */
    public GLTFNode(String name, int meshIndex, Vector3f translation, Quaternionf rotation, Vector3f scale) {
        this.name = name;
        this.meshIndex = meshIndex;

        this.translation = new Vector3f(translation);
        this.rotation = new Quaternionf(rotation);
        this.scale = new Vector3f(scale);

        this.localMatrix = new Matrix4f();
        this.worldMatrix = new Matrix4f();

        this.children = new ArrayList<>();

        updateLocalMatrix();
    }

    /**
     * Updates the local matrix.
     */
    private void updateLocalMatrix() {
        localMatrix.identity()
                .translate(translation)
                .rotate(rotation)
                .scale(scale);
    }

    /**
     * Updates the transform.
     */
    public void updateTransform() {
        updateLocalMatrix();

        for (GLTFNode child : children) {
            child.updateTransform();
        }
    }

    /**
     * Renders render.
     * @param model the model value
     * @param parentMatrix the parent matrix value
     * @param shader the shader value
     */
    public void render(GLTFModel model, Matrix4f parentMatrix, Shader shader) {
        worldMatrix.set(parentMatrix).mul(localMatrix);
        if (!isVisible) {
            return;
        }

        if (meshIndex >= 0) {
            if (textureOverride != 0 && uvOverride != null) {
                model.renderMesh(meshIndex, worldMatrix, shader, textureOverride, uvOverride);
            } else {
                model.renderMesh(meshIndex, worldMatrix, shader);
            }
        }

        for (GLTFNode child : children) {
            child.render(model, worldMatrix, shader);
        }
    }

    /**
     * Returns find.
     * @param nodeName the node name value
     * @return the find result
     */
    public GLTFNode find(String nodeName) {

        if (name != null && name.equals(nodeName)) {
            return this;
        }

        for (GLTFNode child : children) {

            GLTFNode result = child.find(nodeName);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Adds the child.
     * @param child the child value
     */
    public void addChild(GLTFNode child) {
        children.add(child);
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the mesh index.
     * @return the mesh index
     */
    public int getMeshIndex() {
        return meshIndex;
    }

    /**
     * Returns the translation.
     * @return the translation
     */
    public Vector3f getTranslation() {
        return translation;
    }

    /**
     * Sets the translation.
     * @param value the value value
     */
    public void setTranslation(Vector3f value) {
        translation.set(value);
        updateLocalMatrix();
    }

    /**
     * Returns the rotation.
     * @return the rotation
     */
    public Quaternionf getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation.
     * @param value the value value
     */
    public void setRotation(Quaternionf value) {
        rotation.set(value);
        updateLocalMatrix();
    }

    /**
     * Returns the scale.
     * @return the scale
     */
    public Vector3f getScale() {
        return scale;
    }

    /**
     * Sets the scale.
     * @param value the value value
     */
    public void setScale(Vector3f value) {
        scale.set(value);
        updateLocalMatrix();
    }

    /**
     * Returns the local matrix.
     * @return the local matrix
     */
    public Matrix4f getLocalMatrix() {
        return localMatrix;
    }

    /**
     * Returns the world matrix.
     * @return the world matrix
     */
    public Matrix4f getWorldMatrix() {
        return worldMatrix;
    }

    /**
     * Returns the children.
     * @return the children
     */
    public List<GLTFNode> getChildren() {
        return children;
    }

    /**
     * Checks whether the visible condition is met.
     * @return {@code true} if visible; otherwise {@code false}
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Sets the visible.
     * @param visible the visible value
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    /**
     * Sets the texture override.
     * @param textureId the texture id value
     * @param uvBounds the uv bounds value
     */
    public void setTextureOverride(int textureId, Vector4f uvBounds) {
        this.textureOverride = textureId;
        this.uvOverride = uvBounds == null ? null : new Vector4f(uvBounds);
    }

    /**
     * Clears the texture override.
     */
    public void clearTextureOverride() {
        textureOverride = 0;
        uvOverride = null;
    }

    /**
     * Returns the texture override.
     * @return the texture override
     */
    public int getTextureOverride() {
        return textureOverride;
    }

    /**
     * Returns the uv override.
     * @return the uv override
     */
    public Vector4f getUvOverride() {
        return uvOverride;
    }
}