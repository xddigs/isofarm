package com.isofarm.graphics.gltf;

import com.isofarm.graphics.Shader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class GLTFNode {
    private final String name;
    private final int meshIndex;

    private final Vector3f translation;
    private final Quaternionf rotation;
    private final Vector3f scale;

    private final Matrix4f localMatrix;
    private final Matrix4f worldMatrix;

    private final List<GLTFNode> children;

    public GLTFNode(String name, int meshIndex, Vector3f translation,
                    Quaternionf rotation, Vector3f scale) {
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

    private void updateLocalMatrix() {
        localMatrix.identity().translate(translation).rotate(rotation).scale(scale);
    }

    public void updateTransform() {
        updateLocalMatrix();

        for (GLTFNode child : children) {
            child.updateTransform();
        }
    }

    public void render(GLTFModel model, Matrix4f parentMatrix, Shader shader) {
        parentMatrix.mul(localMatrix, worldMatrix);

        if (meshIndex >= 0) {
            model.renderMesh(meshIndex, worldMatrix, shader);
        }

        for (GLTFNode child : children) {
            child.render(model, worldMatrix, shader);
        }
    }

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

    public void addChild(GLTFNode child) {
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public int getMeshIndex() {
        return meshIndex;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f value) {
        translation.set(value);
        updateLocalMatrix();
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public void setRotation(Quaternionf value) {
        rotation.set(value);
        updateLocalMatrix();
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f value) {
        scale.set(value);
        updateLocalMatrix();
    }

    public Matrix4f getLocalMatrix() {
        return localMatrix;
    }

    public Matrix4f getWorldMatrix() {
        return worldMatrix;
    }

    public List<GLTFNode> getChildren() {
        return children;
    }
}