package com.sfarm4j.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {
    private final Vector3f position;
    private final Matrix4f projectionMatrix;

    private float pitch = 35.264f;
    private float yaw = -45.0f;

    public Camera(float width, float height) {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.projectionMatrix = new Matrix4f();

        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        this.projectionMatrix.ortho(-halfWidth, halfWidth,
                -halfHeight, halfHeight, -100.0f, 100.0f);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .identity()
                .rotateX((float) Math.toRadians(pitch))
                .rotateY((float) Math.toRadians(yaw))
                .translate(-position.x, -position.y, -position.z);
    }

    public void rotateYaw(float offsetAngle) {
        this.yaw += offsetAngle;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
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

    public void pan(float deltaX, float deltaY, float sensitivity) {
        float rad = (float) Math.toRadians(yaw);
        float dx = (float) (-Math.cos(rad) * deltaX + Math.sin(rad) * deltaY) * sensitivity;
        float dz = (float) (-Math.sin(rad) * deltaX - Math.cos(rad) * deltaY) * sensitivity;
        this.position.add(dx, 0.0f, dz);
    }

    public Vector2i highlight(float mouseX, float mouseY, float windowWidth, float windowHeight) {
        float ndcX = (2.0f * mouseX) / windowWidth - 1.0f;
        float ndcY = 1.0f - (2.0f * mouseY) / windowHeight;

        Matrix4f invProjView = new Matrix4f(projectionMatrix)
                .mul(getViewMatrix())
                .invert();

        Vector4f rayStart = new Vector4f(ndcX, ndcY, -1.0f, 1.0f);
        Vector4f rayEnd   = new Vector4f(ndcX, ndcY,  1.0f, 1.0f);

        invProjView.transform(rayStart);
        invProjView.transform(rayEnd);

        if (rayStart.w != 0.0f) rayStart.div(rayStart.w);
        if (rayEnd.w != 0.0f)   rayEnd.div(rayEnd.w);

        Vector3f origin = new Vector3f(rayStart.x, rayStart.y, rayStart.z);
        Vector3f dir = new Vector3f(rayEnd.x - rayStart.x, rayEnd.y - rayStart.y,
                rayEnd.z - rayStart.z).normalize();

        if (Math.abs(dir.y) < 0.00001f) {
            return null;
        }

        float t = -origin.y / dir.y;
        float worldX = origin.x + t * dir.x;
        float worldZ = origin.z + t * dir.z;

        return new Vector2i(Math.round(worldX), Math.round(worldZ));
    }
}