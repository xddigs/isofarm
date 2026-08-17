package com.tilled.graphics;

import com.tilled.data.Hit;
import com.tilled.utils.K;
import com.tilled.wrld.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@SuppressWarnings("unused")
public class Camera {
    private static final float MAX_RAY_DISTANCE = 100.0f;
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
        this.projectionMatrix.identity().ortho(-halfWidth, halfWidth, -halfHeight, halfHeight, K.Camera.ORTHO_NEAR, K.Camera.ORTHO_FAR);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().identity().rotateX((float) Math.toRadians(pitch)).rotateY((float) Math.toRadians(yaw)).translate(-position.x, -position.y, -position.z);
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

    public Hit highlight(float mouseX, float mouseY,
                         float windowWidth, float windowHeight, World world) {

        float ndcX = (2.0f * mouseX) / windowWidth - 1.0f;
        float ndcY = 1.0f - (2.0f * mouseY) / windowHeight;

        Matrix4f invProjView = new Matrix4f(projectionMatrix).mul(getViewMatrix()).invert();
        Vector4f nearPoint = new Vector4f(ndcX, ndcY, -1.0f, 1.0f);
        Vector4f farPoint = new Vector4f(ndcX, ndcY, 1.0f, 1.0f);

        invProjView.transform(nearPoint);
        invProjView.transform(farPoint);

        if (nearPoint.w != 0.0f) {
            nearPoint.div(nearPoint.w);
        }

        if (farPoint.w != 0.0f) {
            farPoint.div(farPoint.w);
        }

        Vector3f origin = new Vector3f(nearPoint.x, nearPoint.y, nearPoint.z);
        Vector3f direction = new Vector3f(farPoint.x - nearPoint.x,
                farPoint.y - nearPoint.y, farPoint.z - nearPoint.z);

        if (direction.lengthSquared() < 0.000001f) {
            return null;
        }

        direction.normalize();

        float tileSize = K.World.TILE_SIZE;

        int x = (int) Math.floor(origin.x / tileSize);
        int y = (int) Math.floor(origin.y / tileSize);
        int z = (int) Math.floor(origin.z / tileSize);

        int stepX = Float.compare(direction.x, 0.0f);
        int stepY = Float.compare(direction.y, 0.0f);
        int stepZ = Float.compare(direction.z, 0.0f);

        float tDeltaX = stepX != 0 ? Math.abs(tileSize / direction.x) : Float.POSITIVE_INFINITY;
        float tDeltaY = stepY != 0 ? Math.abs(tileSize / direction.y) : Float.POSITIVE_INFINITY;
        float tDeltaZ = stepZ != 0 ? Math.abs(tileSize / direction.z) : Float.POSITIVE_INFINITY;

        float tMaxX;

        if (stepX > 0) {
            float nextBoundary = (x + 1) * tileSize;
            tMaxX = (nextBoundary - origin.x) / direction.x;
        } else if (stepX < 0) {
            float nextBoundary = x * tileSize;
            tMaxX = (nextBoundary - origin.x) / direction.x;
        } else {
            tMaxX = Float.POSITIVE_INFINITY;
        }

        float tMaxY;

        if (stepY > 0) {
            float nextBoundary = (y + 1) * tileSize;
            tMaxY = (nextBoundary - origin.y) / direction.y;
        } else if (stepY < 0) {
            float nextBoundary = y * tileSize;
            tMaxY = (nextBoundary - origin.y) / direction.y;
        } else {
            tMaxY = Float.POSITIVE_INFINITY;
        }

        float tMaxZ;

        if (stepZ > 0) {
            float nextBoundary = (z + 1) * tileSize;
            tMaxZ = (nextBoundary - origin.z) / direction.z;
        } else if (stepZ < 0) {
            float nextBoundary = z * tileSize;
            tMaxZ = (nextBoundary - origin.z) / direction.z;
        } else {
            tMaxZ = Float.POSITIVE_INFINITY;
        }

        float distance = 0.0f;

        int hitNormalX = 0;
        int hitNormalY = 0;
        int hitNormalZ = 0;

        while (distance <= MAX_RAY_DISTANCE) {
            byte block = world.getBlockTypeAt(x, y, z);
            if (block != 0) {
                return new Hit(x, y, z, hitNormalX,
                        hitNormalY, hitNormalZ);
            }

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    distance = tMaxX;
                    tMaxX += tDeltaX;
                    hitNormalX = -stepX;
                    hitNormalY = 0;
                    hitNormalZ = 0;
                } else {
                    z += stepZ;
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;

                    hitNormalX = 0;
                    hitNormalY = 0;
                    hitNormalZ = -stepZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    distance = tMaxY;
                    tMaxY += tDeltaY;

                    hitNormalX = 0;
                    hitNormalY = -stepY;
                    hitNormalZ = 0;
                } else {
                    z += stepZ;
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                    hitNormalX = 0;
                    hitNormalY = 0;
                    hitNormalZ = -stepZ;
                }
            }
        }

        return null;
    }
}