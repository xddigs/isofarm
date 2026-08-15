package com.sfarm4j.graphics;

import com.sfarm4j.utils.K;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

@SuppressWarnings("unused")
public class Camera {
    private final Vector3f position;
    private final Matrix4f projectionMatrix;

    private float pitch = K.Camera.DEFAULT_PITCH;
    private float yaw = K.Camera.DEFAULT_YAW;

    private float baseWidth;
    private float baseHeight;
    private float zoom = 1.0f;
    private float targetZoom = 1.0f;

    public Camera(float width, float height) {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.projectionMatrix = new Matrix4f();
        this.baseWidth = width;
        this.baseHeight = height;

        updateMatrix();
    }

    public void updateProjection(float width, float height) {
        float aspectRatio = width / height;
        this.baseHeight = K.Camera.DEFAULT_HEIGHT;
        this.baseWidth = this.baseHeight * aspectRatio;
        updateMatrix();
    }

    public void update(float delta) {
        if (Math.abs(zoom - targetZoom) > 0.0001f) {
            this.zoom += (targetZoom - zoom) * Math.min(delta * K.Camera.LERP_SPEED, 1.0f);
            updateMatrix();
        }
    }

    public void zoom(float scrollOffset) {
        if (scrollOffset == 0.0f) return;

        if (scrollOffset > 0) {
            this.targetZoom /= (float) Math.pow(K.Camera.ZOOM_FACTOR, scrollOffset);
        } else {
            this.targetZoom *= (float) Math.pow(K.Camera.ZOOM_FACTOR, -scrollOffset);
        }

        this.targetZoom = Math.clamp(this.targetZoom, K.Camera.MIN_ZOOM, K.Camera.MAX_ZOOM);
    }

    private void updateMatrix() {
        float halfWidth = (baseWidth * zoom) / 2.0f;
        float halfHeight = (baseHeight * zoom) / 2.0f;
        this.projectionMatrix.identity().ortho(-halfWidth, halfWidth,
                -halfHeight, halfHeight, K.Camera.ORTHO_NEAR, K.Camera.ORTHO_FAR);
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

    public float getZoom() {
        return zoom;
    }

    public void pan(float deltaX, float deltaY, float sensitivity) {
        float rad = (float) Math.toRadians(yaw);
        float adjustedSensitivity = sensitivity * zoom;
        float dx = (float) (-Math.cos(rad) * deltaX + Math.sin(rad) * deltaY) * adjustedSensitivity;
        float dz = (float) (-Math.sin(rad) * deltaX - Math.cos(rad) * deltaY) * adjustedSensitivity;
        this.position.add(dx, 0.0f, dz);
    }

    public Vector2i highlight(float mouseX, float mouseY,
                              float windowWidth, float windowHeight) {

        float ndcX = (2.0f * mouseX) / windowWidth - 1.0f;
        float ndcY = 1.0f - (2.0f * mouseY) / windowHeight;

        Matrix4f invProjView = new Matrix4f(projectionMatrix)
                .mul(getViewMatrix())
                .invert();

        Vector4f rayStart = new Vector4f(ndcX, ndcY, -1.0f, 1.0f);
        Vector4f rayEnd = new Vector4f(ndcX, ndcY, 1.0f, 1.0f);

        invProjView.transform(rayStart);
        invProjView.transform(rayEnd);

        if (rayStart.w != 0.0f) {
            rayStart.div(rayStart.w);
        }

        if (rayEnd.w != 0.0f) {
            rayEnd.div(rayEnd.w);
        }

        Vector3f origin = new Vector3f(
                rayStart.x,
                rayStart.y,
                rayStart.z
        );

        Vector3f dir = new Vector3f(
                rayEnd.x - rayStart.x,
                rayEnd.y - rayStart.y,
                rayEnd.z - rayStart.z
        ).normalize();

        if (Math.abs(dir.y) < K.World.EPSILON_RAY_Y) return null;
        float tGround = -origin.y / dir.y;
        if (tGround < 0.0f) return null;

        float groundX = origin.x + tGround * dir.x;
        float groundZ = origin.z + tGround * dir.z;

        int cellX = Math.round(groundX / K.World.TILE_SIZE);
        int cellZ = Math.round(groundZ / K.World.TILE_SIZE);
        return new Vector2i(cellX, cellZ);
    }
}