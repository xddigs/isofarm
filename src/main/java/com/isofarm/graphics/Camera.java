package com.isofarm.graphics;

import com.isofarm.data.BlockData;
import com.isofarm.data.Hit;
import com.isofarm.data.Ray;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.joml.Math.lerp;

public class Camera implements CameraView {
    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 80.0f;

    private static final float DEFAULT_YAW = 45.0f;
    private static final float PITCH = 35.2643897f;

    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 2000.0f;

    private final float yaw = DEFAULT_YAW;
    private final Vector3f position;
    private final Matrix4f projectionMatrix;

    private float zoom = 25.0f;
    private float aspectRatio = 1.0f;

    public Camera(float width, float height, int renderDistanceChunks) {
        this.position = new Vector3f();
        this.projectionMatrix = new Matrix4f();
        updateProjection(width, height, renderDistanceChunks);
    }

    public void updateProjection(float width, float height, int renderDistanceChunks) {
        this.aspectRatio = width / Math.max(height, 1.0f);
        float farPlane = (renderDistanceChunks + 2) * 16.0f;
        projectionMatrix.identity().ortho(-zoom * aspectRatio,
                zoom * aspectRatio, -zoom, zoom, NEAR_PLANE, Math.max(farPlane, FAR_PLANE));
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    @Override
    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .identity()
                .rotateX((float) Math.toRadians(PITCH))
                .rotateY((float) Math.toRadians(yaw))
                .translate(-position.x, -position.y, -position.z);
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public float getPitch() {
        return PITCH;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public Vector3f getForwardVector() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(PITCH);
        Vector3f direction = new Vector3f();
        direction.x = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        direction.y = (float) (-Math.sin(pitchRad));
        direction.z = (float) (-Math.cos(yawRad) * Math.cos(pitchRad));
        return direction.normalize();
    }

    public Vector3f getRightVector() {
        Vector3f forward = getForwardVector();
        Vector3f right = new Vector3f();
        right.set(-forward.z, 0.0f, forward.x);
        return right.normalize();
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = lerp(this.zoom, Math.clamp(zoom, MIN_ZOOM, MAX_ZOOM), .01f);
        updateProjection(aspectRatio, 1.0f, Settings.getRenderDistance());
    }

    public Vector3f getUpVector() {
        Vector3f forward = getForwardVector();
        Vector3f right = getRightVector();
        Vector3f up = new Vector3f();
        right.cross(forward, up);
        return up.normalize();
    }

    public Ray getMouseRay(float mouseX, float mouseY, float screenWidth, float screenHeight) {
        float ndcX = (2.0f * mouseX / screenWidth) - 1.0f;
        float ndcY = 1.0f - (2.0f * mouseY / screenHeight);

        Matrix4f invVP = new Matrix4f();
        projectionMatrix.mul(getViewMatrix(), invVP).invert();

        Vector3f rayOrigin = new Vector3f();
        invVP.transformProject(ndcX, ndcY, -1.0f, rayOrigin);
        return new Ray(rayOrigin, getForwardVector());
    }

    public Hit highlight(World world, Vector3f playerPos, float mouseX, float mouseY,
                         float screenWidth, float screenHeight) {
        Ray ray = getMouseRay(mouseX, mouseY, screenWidth, screenHeight);
        return raycast(world, playerPos, ray.origin(), ray.direction());
    }

    private Hit raycast(World world, Vector3f playerPos, Vector3f origin, Vector3f direction) {
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

        float tMaxX = stepX > 0 ? ((x + 1) * tileSize - origin.x) / direction.x : stepX < 0 ? (x * tileSize - origin.x) / direction.x : Float.POSITIVE_INFINITY;
        float tMaxY = stepY > 0 ? ((y + 1) * tileSize - origin.y) / direction.y : stepY < 0 ? (y * tileSize - origin.y) / direction.y : Float.POSITIVE_INFINITY;
        float tMaxZ = stepZ > 0 ? ((z + 1) * tileSize - origin.z) / direction.z : stepZ < 0 ? (z * tileSize - origin.z) / direction.z : Float.POSITIVE_INFINITY;

        int hitNormalX = 0, hitNormalY = 0, hitNormalZ = 0;

        do {
            byte block = world.getBlockTypeAt(x, y, z);
            byte waterLevel = world.getWaterLevelAt(x, y, z);

            boolean hasBlock = block != BlockData.AIR.getId();
            boolean hasWater = waterLevel > 0;

            if (hasBlock || hasWater) {
                float blockCenterX = x + 0.5f;
                float blockCenterY = y + 0.5f;
                float blockCenterZ = z + 0.5f;
                float distToPlayer = playerPos.distance(blockCenterX, blockCenterY, blockCenterZ);

                if (distToPlayer <= Settings.getMaxInteractionDistance()) {
                    return new Hit(x, y, z, hitNormalX, hitNormalY, hitNormalZ);
                } else {
                    return null;
                }
            }

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    tMaxX += tDeltaX;
                    hitNormalX = -stepX;
                    hitNormalY = 0;
                    hitNormalZ = 0;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                    hitNormalX = 0;
                    hitNormalY = 0;
                    hitNormalZ = -stepZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    tMaxY += tDeltaY;
                    hitNormalX = 0;
                    hitNormalY = -stepY;
                    hitNormalZ = 0;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                    hitNormalX = 0;
                    hitNormalY = 0;
                    hitNormalZ = -stepZ;
                }
            }
        } while (!(tMaxX > 2000.0f) || !(tMaxY > 2000.0f) || !(tMaxZ > 2000.0f));
        return null;
    }
}