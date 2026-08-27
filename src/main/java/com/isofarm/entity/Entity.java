package com.isofarm.entity;

import com.isofarm.data.BlockData;
import com.isofarm.data.DataClass;
import com.isofarm.data.Hit;
import com.isofarm.utils.K;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;

@DataClass
public abstract class Entity {
    private final byte id;
    private String name;

    protected Vector3f position;
    protected Vector3f velocity;
    protected Vector3f dimensions;

    protected float hitpoints;
    protected float maxHitpoints;

    private float standingHeight;
    private float crouchingHeight;
    private boolean onGround;
    private boolean wasOnGround;
    private boolean isCrounching;

    private float currentEyeHeight = 1.6f;
    private float speed;

    public Entity(String name) {
        this.id = (byte) (Math.floor((Math.random() * Math.random()) * 100));
        this.name = name;

        this.position = new Vector3f();
        this.velocity = new Vector3f();
        this.dimensions = new Vector3f();

        this.isCrounching = false;
        this.standingHeight = 2.0f;
        this.crouchingHeight = 1.4f;

        this.onGround = false;
        this.wasOnGround = onGround;
        this.speed = 1.0f;
    }

    public boolean isAlive() {
        return hitpoints > 0;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector3f getPosition() {
        return position;
    }

    public String getPositionString() {
        return String.format("X:%.2f // Y:%.2f // Z:%.2f",
                position.x, position.y, position.z);
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
    }

    public void setVelocity(float x, float y, float z) {
        this.velocity.set(x, y, z);
    }

    public Vector3f getDimensions() {
        return dimensions;
    }

    public void setDimensions(Vector3f dimensions) {
        this.dimensions.set(dimensions);
    }

    public void setDimensions(float width, float height, float depth) {
        this.dimensions.set(width, height, depth);

        if (standingHeight <= 0.0f) {
            standingHeight = height;
        }
    }

    public float getStandingHeight() {
        return standingHeight;
    }

    public void setStandingHeight(float standingHeight) {
        this.standingHeight = standingHeight;
    }

    public float getCrouchingHeight() {
        return crouchingHeight;
    }

    public void setCrouchingHeight(float crouchingHeight) {
        this.crouchingHeight = crouchingHeight;
    }

    public boolean isCrounching() {
        return isCrounching;
    }

    public void setCrounching(boolean isCrounching) {
        this.isCrounching = isCrounching;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean wasOnGround() {
        return wasOnGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void setWasOnGround(boolean wasOnGround) {
        this.wasOnGround = wasOnGround;
    }

    public void moveAndCollide(World world, Vector3f targetVelocity, float delta) {
        float smooth = 1.0f - (float) Math.exp(-12.0f * delta);

        velocity.x += (targetVelocity.x - velocity.x) * smooth;
        velocity.z += (targetVelocity.z - velocity.z) * smooth;
        velocity.y += K.World.GRAVITY * delta;
        setOnGround(false);
        position.x += velocity.x * delta;
        if (checkCollision(world)) {
            position.x -= velocity.x * delta;
            velocity.x = 0.0f;
        }

        position.y += velocity.y * delta;
        if (checkCollision(world)) {
            position.y -= velocity.y * delta;

            if (velocity.y < 0.0f) {
                setOnGround(true);
            }

            velocity.y = 0.0f;
        }

        position.z += velocity.z * delta;
        if (checkCollision(world)) {
            position.z -= velocity.z * delta;
            velocity.z = 0.0f;
        }
    }

    public boolean checkCollision(World world) {
        float epsilon = 0.001f;

        float minX = position.x - dimensions.x / 2.0f + epsilon;
        float maxX = position.x + dimensions.x / 2.0f - epsilon;

        float minY = position.y + epsilon;
        float maxY = position.y + dimensions.y - epsilon;

        float minZ = position.z - dimensions.z / 2.0f + epsilon;
        float maxZ = position.z + dimensions.z / 2.0f - epsilon;

        int blockMinX = (int) Math.floor(minX);
        int blockMaxX = (int) Math.floor(maxX);

        int blockMinY = (int) Math.floor(minY);
        int blockMaxY = (int) Math.floor(maxY);

        int blockMinZ = (int) Math.floor(minZ);
        int blockMaxZ = (int) Math.floor(maxZ);

        for (int x = blockMinX; x <= blockMaxX; x++) {
            for (int y = blockMinY; y <= blockMaxY; y++) {
                for (int z = blockMinZ; z <= blockMaxZ; z++) {
                    if (world.isBlockSolid(x, y, z)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean intersectsBlock(int blockX, int blockY, int blockZ) {
        float epsilon = 0.001f;

        float minX = position.x - dimensions.x / 2.0f + epsilon;
        float maxX = position.x + dimensions.x / 2.0f - epsilon;

        float minY = position.y + epsilon;
        float maxY = position.y + dimensions.y - epsilon;

        float minZ = position.z - dimensions.z / 2.0f + epsilon;
        float maxZ = position.z + dimensions.z / 2.0f - epsilon;

        float blockMaxX = blockX + 1.0f;
        float blockMaxY = blockY + 1.0f;
        float blockMaxZ = blockZ + 1.0f;

        return minX < blockMaxX &&
                maxX > blockX &&
                minY < blockMaxY &&
                maxY > blockY &&
                minZ < blockMaxZ &&
                maxZ > blockZ;
    }

    public void jump() {
        if (!isOnGround()) return;
        velocity.y = K.World.JUMP_FORCE;
        setOnGround(false);
    }

    public void crunch() {
        isCrounching = true;
    }

    public void uncrouch(World world) {
        if (!isCrounching) return;

        if (canStandUp(world)) {
            isCrounching = false;
        }
    }

    public void lerp(Vector3f target, float delta) {
        float speed = 12.0f;
        float amount = 1.0f - (float) Math.exp(-speed * delta);
        dimensions.lerp(target, amount);
    }

    public boolean canStandUp(World world) {
        float epsilon = 0.001f;
        float minX = position.x - dimensions.x / 2.0f + epsilon;
        float maxX = position.x + dimensions.x / 2.0f - epsilon;
        float minY = position.y + epsilon;
        float maxY = position.y + standingHeight - epsilon;

        float minZ = position.z - dimensions.z / 2.0f + epsilon;
        float maxZ = position.z + dimensions.z / 2.0f - epsilon;

        int blockMinX = (int) Math.floor(minX);
        int blockMaxX = (int) Math.floor(maxX);
        int blockMinY = (int) Math.floor(minY);
        int blockMaxY = (int) Math.floor(maxY);
        int blockMinZ = (int) Math.floor(minZ);
        int blockMaxZ = (int) Math.floor(maxZ);

        for (int x = blockMinX; x <= blockMaxX; x++) {
            for (int y = blockMinY; y <= blockMaxY; y++) {
                for (int z = blockMinZ; z <= blockMaxZ; z++) {
                    if (world.isBlockSolid(x, y, z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void updateCrouching(float delta) {
        float targetHeight = isCrounching ? crouchingHeight : standingHeight;
        if (Math.abs(dimensions.y - targetHeight) < 0.001f) {
            dimensions.y = targetHeight;
        } else {
            float smoothFactor = 1.0f - (float) Math.exp(-14.0f * delta);
            dimensions.y += (targetHeight - dimensions.y) * smoothFactor;
        }

        float targetEyeHeight = isCrounching ? (crouchingHeight * 0.85f) : (standingHeight * 0.85f);
        float eyeSmooth = 1.0f - (float) Math.exp(-14.0f * delta);
        currentEyeHeight += (targetEyeHeight - currentEyeHeight) * eyeSmooth;
    }

    public float getCurrentEyeHeight() {
        return currentEyeHeight;
    }

    public boolean isUnderFluid(World world) {
        float epsilon = 0.001f;
        float minX = position.x - dimensions.x / 2.0f + epsilon;
        float maxX = position.x + dimensions.x / 2.0f - epsilon;
        float minY = position.y + epsilon;
        float maxY = position.y + dimensions.y - epsilon;
        float minZ = position.z - dimensions.z / 2.0f + epsilon;
        float maxZ = position.z + dimensions.z / 2.0f - epsilon;

        int blockMinX = (int) Math.floor(minX);
        int blockMaxX = (int) Math.floor(maxX);
        int blockMinY = (int) Math.floor(minY);
        int blockMaxY = (int) Math.floor(maxY);
        int blockMinZ = (int) Math.floor(minZ);
        int blockMaxZ = (int) Math.floor(maxZ);

        for (int x = blockMinX; x <= blockMaxX; x++) {
            for (int y = blockMinY; y <= blockMaxY; y++) {
                for (int z = blockMinZ; z <= blockMaxZ; z++) {
                    byte blockId = world.getBlockTypeAt(x, (int) (y + getDimensions().y()), z);
                    if (BlockData.fromId(blockId).isFluid()) {
                        return true;
                    }

                    if (world.getWaterLevelAt(x, y, z) > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public abstract void update(Hit hit, float delta);

    public abstract void render(GameMaster gameMaster);

    protected void dropLoot() {}
}