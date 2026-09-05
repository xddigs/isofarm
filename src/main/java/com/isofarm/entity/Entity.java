package com.isofarm.entity;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.Cause;
import com.isofarm.data.DataClass;
import com.isofarm.data.RenderPass;
import com.isofarm.utils.K;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;

/**
 * Provides entity behavior.
 */
@DataClass
public abstract class Entity {
    private static final float LAVA_DAMAGE_INTERVAL = 1.0f;
    private static final float LAVA_DAMAGE_STEP = 0.1f;

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

    private float speed;
    private float lavaDamageTimer;
    private int lavaDamageTicks;

    /**
     * Creates a new {@code Entity} instance.
     * @param name the name value
     */
    public Entity(String name) {
        this.id = (byte) (Math.floor((Math.random() * Math.random()) * 100));
        this.name = name;

        this.position = new Vector3f();
        this.velocity = new Vector3f();
        this.dimensions = new Vector3f();

        this.standingHeight = 2.0f;
        this.crouchingHeight = 1.4f;

        this.onGround = false;
        this.wasOnGround = onGround;
        this.speed = 1.0f;
    }

    /**
     * Checks whether the alive condition is met.
     * @return {@code true} if alive; otherwise {@code false}
     */
    public boolean isAlive() {
        return hitpoints > 0;
    }

    /**
     * Applies damage to the entity.
     * @param amount the damage amount
     */
    public void damage(float amount) {
        damage(amount, Cause.ENTITY);
    }

    /**
     * Applies damage attributed to a specific cause.
     * @param amount the damage amount
     * @param cause the damage cause
     */
    public void damage(float amount, Cause cause) {
        if (!isAlive() || amount <= 0) return;
        float previousHitpoints = hitpoints;
        hitpoints = Math.max(0.0f, hitpoints - amount);
        if (hitpoints < previousHitpoints) onDamageTaken(amount);
        if (hitpoints == 0.0f) onDeath(cause == null ? Cause.NULL : cause);
    }

    /**
     * Immediately kills the entity with the supplied cause.
     * @param cause the death cause
     */
    public void kill(Cause cause) {
        if (!isAlive()) return;
        float previousHitpoints = hitpoints;
        hitpoints = 0.0f;
        onDamageTaken(previousHitpoints);
        onDeath(cause == null ? Cause.NULL : cause);
    }

    /**
     * Handles an applied damage event.
     * @param amount the applied damage amount
     */
    protected void onDamageTaken(float amount) {}

    /** Called once when damage reduces this entity's hitpoints to zero. */
    protected void onDeath(Cause cause) {}

    /**
     * Returns the maximum hitpoints used to scale environmental damage.
     * @return the maximum hitpoints
     */
    public float getMaxHitpoints() {
        return maxHitpoints;
    }

    /**
     * Applies increasingly severe damage while the entity remains in direct
     * contact with lava. Leaving the lava resets the damage progression.
     * @param world the world value
     * @param delta the elapsed time in seconds
     */
    public final void updateEnvironmentalDamage(World world, float delta) {
        if (!isAlive() || delta <= 0 || !isTouchingLava(world)) {
            lavaDamageTimer = 0;
            lavaDamageTicks = 0;
            return;
        }

        if (lavaDamageTicks == 0 && lavaDamageTimer == 0) {
            lavaDamageTimer = LAVA_DAMAGE_INTERVAL;
        } else {
            lavaDamageTimer += delta;
        }
        while (lavaDamageTimer >= LAVA_DAMAGE_INTERVAL && isAlive()) {
            lavaDamageTimer -= LAVA_DAMAGE_INTERVAL;
            lavaDamageTicks++;
            damage(Math.max(1.0f, getMaxHitpoints() * LAVA_DAMAGE_STEP * lavaDamageTicks),
                    Cause.BURN);
        }
    }

    /**
     * Checks whether the entity bounds touch the occupied portion of a lava cell.
     * @param world the world value
     * @return {@code true} when the entity touches lava; otherwise {@code false}
     */
    private boolean isTouchingLava(World world) {
        float epsilon = 0.001f;
        float minX = position.x - dimensions.x / 2.0f + epsilon;
        float maxX = position.x + dimensions.x / 2.0f - epsilon;
        float minY = position.y;
        float maxY = position.y + dimensions.y;
        float minZ = position.z - dimensions.z / 2.0f + epsilon;
        float maxZ = position.z + dimensions.z / 2.0f - epsilon;

        for (int x = (int) Math.floor(minX); x <= (int) Math.floor(maxX); x++) {
            for (int y = (int) Math.floor(minY); y <= (int) Math.floor(maxY); y++) {
                for (int z = (int) Math.floor(minZ); z <= (int) Math.floor(maxZ); z++) {
                    if (world.getBlockTypeAt(x, y, z) != BlockData.LAVA.getId()) continue;
                    float lavaTop = y + world.getFluidLevelAt(x, y, z) / 8.0f;
                    if (maxY > y && minY <= lavaTop + 0.05f) return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name the name value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the position.
     * @return the position
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Returns the position string.
     * @return the position string
     */
    public String getPositionString() {
        return String.format("X:%.2f // Y:%.2f // Z:%.2f",
                position.x, position.y, position.z);
    }

    /**
     * Sets the position.
     * @param position the position value
     */
    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    /**
     * Sets the position.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    /**
     * Returns the velocity.
     * @return the velocity
     */
    public Vector3f getVelocity() {
        return velocity;
    }

    /**
     * Sets the velocity.
     * @param velocity the velocity value
     */
    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
    }

    /**
     * Sets the velocity.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public void setVelocity(float x, float y, float z) {
        this.velocity.set(x, y, z);
    }

    /**
     * Returns the dimensions.
     * @return the dimensions
     */
    public Vector3f getDimensions() {
        return dimensions;
    }

    /**
     * Sets the dimensions.
     * @param dimensions the dimensions value
     */
    public void setDimensions(Vector3f dimensions) {
        this.dimensions.set(dimensions);
    }

    /**
     * Sets the dimensions.
     * @param width the width value
     * @param height the height value
     * @param depth the depth value
     */
    public void setDimensions(float width, float height, float depth) {
        this.dimensions.set(width, height, depth);

        if (standingHeight <= 0.0f) {
            standingHeight = height;
        }
    }

    /**
     * Returns the standing height.
     * @return the standing height
     */
    public float getStandingHeight() {
        return standingHeight;
    }

    /**
     * Sets the standing height.
     * @param standingHeight the standing height value
     */
    public void setStandingHeight(float standingHeight) {
        this.standingHeight = standingHeight;
    }

    /**
     * Returns the crouching height.
     * @return the crouching height
     */
    public float getCrouchingHeight() {
        return crouchingHeight;
    }

    /**
     * Sets the crouching height.
     * @param crouchingHeight the crouching height value
     */
    public void setCrouchingHeight(float crouchingHeight) {
        this.crouchingHeight = crouchingHeight;
    }

    /**
     * Checks whether the on ground condition is met.
     * @return {@code true} if on ground; otherwise {@code false}
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * Performs the was on ground operation.
     * @return the was on ground result
     */
    public boolean wasOnGround() {
        return wasOnGround;
    }

    /**
     * Sets the on ground.
     * @param onGround the on ground value
     */
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    /**
     * Sets the was on ground.
     * @param wasOnGround the was on ground value
     */
    public void setWasOnGround(boolean wasOnGround) {
        this.wasOnGround = wasOnGround;
    }

    /**
     * Checks whether the in fluid condition is met.
     * @param world the world value
     * @return {@code true} if in fluid; otherwise {@code false}
     */
    public boolean isInFluid(World world) {
        float epsilon = 0.001f;
        float minX = position.x - dimensions.x / 2.0f + epsilon;
        float maxX = position.x + dimensions.x / 2.0f - epsilon;

        float minY = position.y - 0.05f;
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
                    byte blockId = world.getBlockTypeAt(x, y, z);
                    BlockData data = BlockData.fromId(blockId);
                    if (data != null && data.isFluid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the fluid submersion.
     * @param world the world value
     * @return the fluid submersion
     */
    private float getFluidSubmersion(World world) {
        float epsilon = 0.001f;

        float minX = position.x - dimensions.x / 2.0f + epsilon;
        float maxX = position.x + dimensions.x / 2.0f - epsilon;

        float minY = position.y - 0.1f;
        float maxY = position.y + dimensions.y;

        float minZ = position.z - dimensions.z / 2.0f + epsilon;
        float maxZ = position.z + dimensions.z / 2.0f - epsilon;

        int blockMinX = (int) Math.floor(minX);
        int blockMaxX = (int) Math.floor(maxX);

        int blockMinY = (int) Math.floor(minY);
        int blockMaxY = (int) Math.floor(maxY);

        int blockMinZ = (int) Math.floor(minZ);
        int blockMaxZ = (int) Math.floor(maxZ);

        float submergedHeight = 0.0f;

        for (int x = blockMinX; x <= blockMaxX; x++) {
            for (int y = blockMinY; y <= blockMaxY; y++) {
                for (int z = blockMinZ; z <= blockMaxZ; z++) {
                    byte blockId = world.getBlockTypeAt(x, y, z);
                    BlockData data = BlockData.fromId(blockId);
                    if (data == null || !data.isFluid()) {
                        continue;
                    }

                    float waterLevel = world.getFluidLevelAt(x, y, z) / 8.0f;
                    float waterTop = y + waterLevel;
                    float overlapMin = Math.max(position.y, y);
                    float overlapMax = Math.min(maxY, waterTop);
                    if (overlapMax > overlapMin || (position.y >= y && position.y <= waterTop + 0.1f)) {
                        submergedHeight += Math.max(0.1f, overlapMax - overlapMin);
                    }
                }
            }
        }

        return submergedHeight;
    }

    /**
     * Performs the collide operation.
     * @param world the world value
     * @param targetVelocity the target velocity value
     * @param delta the delta value
     */
    public void collide(World world, Vector3f targetVelocity, float delta) {
        float smooth = 1.0f - (float) Math.exp(-12.0f * delta);

        velocity.x += (targetVelocity.x - velocity.x) * smooth;
        velocity.z += (targetVelocity.z - velocity.z) * smooth;
        adjustVelocity(delta);

        float submergedHeight = getFluidSubmersion(world);
        boolean inFluid = submergedHeight > 0.0f;

        if (inFluid) {
            velocity.y += K.World.GRAVITY * 0.15f * delta;

            if (velocity.y < -2.0f) {
                velocity.y = -2.0f;
            }
        } else {
            velocity.y += K.World.GRAVITY * delta;
        }

        position.x += velocity.x * delta;
        if (checkCollision(world)) {
            position.x -= velocity.x * delta;
            velocity.x = 0.0f;
        }

        setOnGround(false);
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

    /**
     * Performs the adjust velocity operation.
     * @param delta the delta value
     */
    protected void adjustVelocity(float delta) {}

    /**
     * Performs the check collision operation.
     * @param world the world value
     * @return the check collision result
     */
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

    /**
     * Performs the intersects block operation.
     * @param blockX the block x value
     * @param blockY the block y value
     * @param blockZ the block z value
     * @return the intersects block result
     */
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

    /**
     * Performs the jump operation.
     */
    public void jump() {
        if (!isOnGround()) return;
        velocity.y = K.World.JUMP_FORCE;
        setOnGround(false);
    }

    /**
     * Returns the speed.
     * @return the speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets the speed.
     * @param speed the speed value
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Updates the current state.
     * @param blockPos the block pos value
     * @param delta the delta value
     */
    public abstract void update(BlockPos blockPos, float delta);

    /**
     * Renders render.
     * @param gameMaster the game master value
     */
    public void render(GameMaster gameMaster) {
        render(gameMaster, RenderPass.NORMAL);
    }

    /**
     * Renders render.
     * @param gameMaster the game master value
     * @param pass the pass value
     */
    public abstract void render(GameMaster gameMaster, RenderPass pass);

    /**
     * Performs the drop loot operation.
     */
    protected void dropLoot() {}
}
