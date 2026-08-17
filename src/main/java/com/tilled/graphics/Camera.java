package com.tilled.graphics;

import com.tilled.data.Hit;
import com.tilled.utils.K;
import com.tilled.wrld.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@SuppressWarnings("unused")
public class Camera {
    private static final float MAX_RAY_DISTANCE = 100.0f;
    private static final float MIN_PITCH = -89.0f;
    private static final float MAX_PITCH = 89.0f;
    private static final float FOV = K.Camera.DEFAULT_FOV;
    private static final float ZOOM_FOV = 30.0f;
    private static final float ZOOM_SPEED = 12.0f;
    private final Vector3f position;
    private final Matrix4f projectionMatrix;
    private float pitch = K.Camera.DEFAULT_PITCH;
    private float yaw = K.Camera.DEFAULT_YAW;
    private float currentFov = FOV;
    private float targetFov = FOV;
    private float aspectRatio = 1.0f;

    public Camera(float width, float height) {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.projectionMatrix = new Matrix4f();
        updateProjection(width, height);
    }

    public void updateProjection(float width, float height) {
        this.aspectRatio = width / Math.max(height, 1.0f);
        this.projectionMatrix.identity().perspective(
                (float) Math.toRadians(currentFov),
                aspectRatio,
                0.1f,
                1000.0f);
    }

    public void update(float delta) {
        float smooth = 1.0f - (float) Math.exp(-ZOOM_SPEED * delta);
        currentFov += (targetFov - currentFov) * smooth;
        projectionMatrix.identity().perspective(
                (float) Math.toRadians(currentFov),
                aspectRatio,
                0.1f,
                1000.0f
        );
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .identity()
                .rotateX((float) Math.toRadians(pitch))
                .rotateY((float) Math.toRadians(yaw))
                .translate(-position.x, -position.y, -position.z);
    }

    public Vector3f getForwardVector() {
        Vector3f dir = new Vector3f();
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        dir.x = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        dir.y = (float) (-Math.sin(pitchRad));
        dir.z = (float) (-Math.cos(yawRad) * Math.cos(pitchRad));

        return dir.normalize();
    }

    public void rotateYaw(float offsetAngle) {
        this.yaw += offsetAngle;
        if (this.yaw >= 360.0f) this.yaw -= 360.0f;
        if (this.yaw < 0.0f) this.yaw += 360.0f;
    }

    public void rotatePitch(float offset) {
        this.pitch += offset;
        this.pitch = Math.clamp(pitch, MIN_PITCH, MAX_PITCH);
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

    public float getFov() {
        return currentFov;
    }

    public void setZooming(boolean zooming) {
        targetFov = zooming ? ZOOM_FOV : FOV;
    }

    public Hit highlight(World world) {
        Vector3f origin = new Vector3f(this.position);
        Vector3f direction = getForwardVector();

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

        float tMaxX = (stepX > 0) ? ((x + 1) * tileSize - origin.x) / direction.x :
                (stepX < 0) ? (x * tileSize - origin.x) / direction.x : Float.POSITIVE_INFINITY;

        float tMaxY = (stepY > 0) ? ((y + 1) * tileSize - origin.y) / direction.y :
                (stepY < 0) ? (y * tileSize - origin.y) / direction.y : Float.POSITIVE_INFINITY;

        float tMaxZ = (stepZ > 0) ? ((z + 1) * tileSize - origin.z) / direction.z :
                (stepZ < 0) ? (z * tileSize - origin.z) / direction.z : Float.POSITIVE_INFINITY;

        float distance = 0.0f;
        int hitNormalX = 0, hitNormalY = 0, hitNormalZ = 0;

        while (distance <= MAX_RAY_DISTANCE) {
            byte block = world.getBlockTypeAt(x, y, z);
            if (block != 0) {
                return new Hit(x, y, z, hitNormalX, hitNormalY, hitNormalZ);
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