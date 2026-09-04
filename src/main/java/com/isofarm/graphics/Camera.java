package com.isofarm.graphics;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.Ray;
import com.isofarm.item.Bucket;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.joml.Math.lerp;

/**
 * Provides camera behavior.
 */
public class Camera implements CameraView {
    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 80.0f;

    private static final float DEFAULT_YAW = 45.0f;
    private static final float PITCH = 35.2643897f;
    private static final float MAX_DAMAGE_TILT = 8.0f;
    private static final float DAMAGE_TILT_RECOVERY = 7.0f;

    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 2000.0f;

    private final float yaw = DEFAULT_YAW;
    private final Vector3f position;
    private final Matrix4f projectionMatrix;

    private float zoom = 25.0f;
    private float aspectRatio = 1.0f;
    private float damageTilt;
    private boolean tiltRight;

    private BlockPos lastHit;
    private int lastHitNormalX;
    private int lastHitNormalY;
    private int lastHitNormalZ;

    /**
     * Creates a new {@code Camera} instance.
     * @param width the width value
     * @param height the height value
     * @param renderDistanceChunks the render distance chunks value
     */
    public Camera(float width, float height, int renderDistanceChunks) {
        this.position = new Vector3f();
        this.projectionMatrix = new Matrix4f();
        updateProjection(width, height, renderDistanceChunks);
    }

    /**
     * Updates the projection.
     * @param width the width value
     * @param height the height value
     * @param renderDistanceChunks the render distance chunks value
     */
    public void updateProjection(float width, float height, int renderDistanceChunks) {
        this.aspectRatio = width / Math.max(height, 1.0f);
        float farPlane = (renderDistanceChunks + 2) * 16.0f;
        projectionMatrix.identity().ortho(-zoom * aspectRatio,
                zoom * aspectRatio, -zoom, zoom, NEAR_PLANE, Math.max(farPlane, FAR_PLANE));
    }

    /**
     * Returns the projection matrix.
     * @return the projection matrix
     */
    @Override
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Returns the view matrix.
     * @return the view matrix
     */
    @Override
    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .identity()
                .rotateZ((float) Math.toRadians(damageTilt))
                .rotateX((float) Math.toRadians(PITCH))
                .rotateY((float) Math.toRadians(yaw))
                .translate(-position.x, -position.y, -position.z);
    }

    /**
     * Returns the position.
     * @return the position
     */
    @Override
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Returns the pitch.
     * @return the pitch
     */
    @Override
    public float getPitch() {
        return PITCH;
    }

    /**
     * Returns the yaw.
     * @return the yaw
     */
    @Override
    public float getYaw() {
        return yaw;
    }

    /**
     * Applies a small screen tilt in response to player damage.
     * Consecutive impacts alternate direction to avoid a permanent visual bias.
     *
     * @param amount the received damage
     */
    public void applyDamageTilt(float amount) {
        if (amount <= 0.0f) return;

        tiltRight = !tiltRight;
        float strength = Math.clamp(0.75f + amount * 0.2f,
                0.75f, MAX_DAMAGE_TILT);
        damageTilt = tiltRight ? strength : -strength;
    }

    /**
     * Smoothly restores the camera to its normal roll.
     *
     * @param delta frame time in seconds
     */
    public void updateDamageTilt(float delta) {
        if (delta <= 0.0f || damageTilt == 0.0f) return;

        damageTilt = lerp(damageTilt, 0.0f,
                Math.clamp(DAMAGE_TILT_RECOVERY * delta, 0.0f, 1.0f));
        if (Math.abs(damageTilt) < 0.01f) damageTilt = 0.0f;
    }

    /**
     * Sets the position.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    /**
     * Returns the forward vector.
     * @return the forward vector
     */
    public Vector3f getForwardVector() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(PITCH);
        Vector3f direction = new Vector3f();
        direction.x = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        direction.y = (float) (-Math.sin(pitchRad));
        direction.z = (float) (-Math.cos(yawRad) * Math.cos(pitchRad));
        return direction.normalize();
    }

    /**
     * Returns the right vector.
     * @return the right vector
     */
    public Vector3f getRightVector() {
        Vector3f forward = getForwardVector();
        Vector3f right = new Vector3f();
        right.set(-forward.z, 0.0f, forward.x);
        return right.normalize();
    }

    /**
     * Returns the zoom.
     * @return the zoom
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Sets the zoom.
     * @param zoom the zoom value
     */
    public void setZoom(float zoom) {
        this.zoom = lerp(this.zoom, Math.clamp(zoom, MIN_ZOOM, MAX_ZOOM), .01f);
        updateProjection(aspectRatio, 1.0f, Settings.getRenderDistance());
    }

    /**
     * Returns the up vector.
     * @return the up vector
     */
    public Vector3f getUpVector() {
        Vector3f forward = getForwardVector();
        Vector3f right = getRightVector();
        Vector3f up = new Vector3f();
        right.cross(forward, up);
        return up.normalize();
    }

    /**
     * Returns the mouse ray.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @param screenWidth the screen width value
     * @param screenHeight the screen height value
     * @return the mouse ray
     */
    public Ray getMouseRay(float mouseX, float mouseY, float screenWidth, float screenHeight) {
        float ndcX = (2.0f * mouseX / screenWidth) - 1.0f;
        float ndcY = 1.0f - (2.0f * mouseY / screenHeight);

        Matrix4f invVP = new Matrix4f();
        projectionMatrix.mul(getViewMatrix(), invVP).invert();

        Vector3f rayOrigin = new Vector3f();
        invVP.transformProject(ndcX, ndcY, -1.0f, rayOrigin);
        return new Ray(rayOrigin, getForwardVector());
    }

    /**
     * Performs the highlight operation.
     * @param world the world value
     * @param playerPos the player pos value
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @param screenWidth the screen width value
     * @param screenHeight the screen height value
     * @param smartFilter the smart filter value
     * @return the highlight result
     */
    public BlockPos highlight(World world, Vector3f playerPos, float mouseX, float mouseY,
                              float screenWidth, float screenHeight, boolean smartFilter) {
        Ray ray = getMouseRay(mouseX, mouseY, screenWidth, screenHeight);
        boolean isBucket = Settings.selectedItem instanceof Bucket;
        lastHit = raycast(world, playerPos, ray.origin(), ray.direction(), smartFilter, isBucket);
        if (lastHit == null) {
            return null;
        }

        return lastHit;
    }

    /**
     * Performs the raycast operation.
     * @param world the world value
     * @param playerPos the player pos value
     * @param origin the origin value
     * @param direction the direction value
     * @param isSmartFilter the is smart filter value
     * @param isBucket the is bucket value
     * @return the raycast result
     */
    private BlockPos raycast(World world, Vector3f playerPos,
                             Vector3f origin, Vector3f direction,
                             boolean isSmartFilter, boolean isBucket) {
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

        int previousX = x;
        int previousY = y;
        int previousZ = z;

        do {
            var interactiveBlock = world.getInteractiveBlockAt(x, y, z);
            if (interactiveBlock != null) {
                float distToPlayer = playerPos.distance(x + 0.5f, y + 0.5f, z + 0.5f);
                if (distToPlayer > Settings.getMaxInteractionDistance()) return null;

                lastHitNormalX = previousX - x;
                lastHitNormalY = previousY - y;
                lastHitNormalZ = previousZ - z;
                return new BlockPos(interactiveBlock.getType(), x, y, z);
            }

            byte block = world.getBlockTypeAt(x, y, z);
            BlockData data = BlockData.fromId(block);
            boolean hasBlock = data != BlockData.AIR;

            if (hasBlock && (data != BlockData.WATER || isBucket)) {
                boolean isTransparentObject = data == BlockData.OAK_LEAVES;

                if (!isSmartFilter || !isTransparentObject) {
                    float blockCenterX = x + 0.5f;
                    float blockCenterY = y + 0.5f;
                    float blockCenterZ = z + 0.5f;

                    float distToPlayer = playerPos.distance(
                            blockCenterX,
                            blockCenterY,
                            blockCenterZ
                    );

                    if (distToPlayer <= Settings.getMaxInteractionDistance()) {
                        lastHitNormalX = previousX - x;
                        lastHitNormalY = previousY - y;
                        lastHitNormalZ = previousZ - z;

                        return new BlockPos(data, x, y, z);
                    } else {
                        return null;
                    }
                }
            }

            previousX = x;
            previousY = y;
            previousZ = z;

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    tMaxX += tDeltaX;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    tMaxY += tDeltaY;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            }
        } while (!(tMaxX > 2000.0f) || !(tMaxY > 2000.0f) || !(tMaxZ > 2000.0f));

        return null;
    }

    /**
     * Returns the last hit normal x.
     * @return the last hit normal x
     */
    public int getLastHitNormalX() {
        return lastHitNormalX;
    }

    /**
     * Returns the last hit normal y.
     * @return the last hit normal y
     */
    public int getLastHitNormalY() {
        return lastHitNormalY;
    }

    /**
     * Returns the last hit normal z.
     * @return the last hit normal z
     */
    public int getLastHitNormalZ() {
        return lastHitNormalZ;
    }
}
