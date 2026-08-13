package com.sfarm4j.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final Vector3f position;
    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix;

    public Camera(float width, float height) {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();

        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        this.projectionMatrix.ortho(-halfWidth, halfWidth,
                -halfHeight, halfHeight, -100.0f, 100.0f);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .identity()
                .rotateX((float) Math.toRadians(30))
                .rotateY((float) Math.toRadians(-45))
                .translate(-position.x, -position.y, -position.z);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void move(float offsetX, float offsetY, float offsetZ) {
        this.position.add(offsetX, offsetY, offsetZ);
    }
}