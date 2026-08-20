package com.soilcraft.entity;

import com.soilcraft.data.DataClass;
import com.soilcraft.utils.K;
import com.soilcraft.wrld.GameMaster;
import com.soilcraft.wrld.World;
import org.joml.Vector3f;

@DataClass
public abstract class Entity {
    private final byte id;
    private final String name;

    protected Vector3f position;
    protected Vector3f velocity;
    protected Vector3f dimensions;

    private boolean onGround;

    public Entity(String name) {
        this.id = (byte) (Math.floor((Math.random() * Math.random()) * 100));
        this.name = name;

        this.position = new Vector3f();
        this.velocity = new Vector3f();
        this.dimensions = new Vector3f();

        this.onGround = false;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Vector3f getPosition() {
        return position;
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
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void moveAndCollide(World world,
                               Vector3f targetVelocity,
                               float delta) {

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
        if (!isOnGround()) {
            return;
        }

        velocity.y = K.World.JUMP_FORCE;
        setOnGround(false);
    }

    public abstract void update(float delta);

    public abstract void render(GameMaster gameMaster);
}