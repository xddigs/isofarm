package com.tilled.entity;

import com.tilled.data.DataClass;
import com.tilled.data.Item;
import com.tilled.graphics.Shader;
import com.tilled.graphics.SpriteSheet;
import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;
import org.joml.Vector3f;

@SuppressWarnings("unused")
@DataClass
public class WorldItem extends Entity {

    private static final float GRAVITY = -20.0f;
    private static final float GROUND_OFFSET = 0.05f;

    private static final float PICKUP_DELAY = 0.56f;

    private static final float AIR_DRAG = 2.5f;
    private static final float GROUND_FRICTION = 7.0f;

    private static final float BOUNCE_FACTOR = 0.20f;
    private static final float MIN_BOUNCE_VELOCITY = 1.2f;

    private static final float ROTATION_SPEED = 180.0f;
    private static final float GROUND_BOB_SPEED = 3.0f;
    private static final float GROUND_BOB_HEIGHT = 0.08f;

    private final Item item;
    private final Vector3f position;
    private final Vector3f velocity;
    private int amount;
    private boolean onGround;
    private World world;
    private float rotation;
    private float bobTime;
    private float pickupTimer;
    private float groundY;

    public WorldItem(Item item, int amount, Vector3f position) {
        super(item.getName());
        this.item = item;
        this.amount = amount;
        this.position = new Vector3f(position);
        this.velocity = new Vector3f();
        this.onGround = false;
        this.rotation = 0.0f;
        this.bobTime = 0.0f;
        this.pickupTimer = PICKUP_DELAY;
        this.groundY = position.y;
    }

    @Override
    public void update(float delta) {
        if (world == null || delta <= 0.0f) return;
        if (pickupTimer > 0.0f) pickupTimer -= delta;

        rotation += ROTATION_SPEED * delta;

        if (rotation >= 360.0f) {
            rotation -= 360.0f;
        }

        if (!onGround) {
            velocity.y += GRAVITY * delta;
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
            position.z += velocity.z * delta;

            float airDamping = Math.max(0.0f, 1.0f - AIR_DRAG * delta);
            velocity.x *= airDamping;
            velocity.z *= airDamping;

            float terrainY = world.getHighestY(position.x, position.z);
            if (position.y <= terrainY + GROUND_OFFSET) {
                position.y = terrainY + GROUND_OFFSET;
                groundY = position.y;
                if (Math.abs(velocity.y) > MIN_BOUNCE_VELOCITY) {
                    velocity.y *= -BOUNCE_FACTOR;
                    onGround = false;

                } else {
                    velocity.y = 0.0f;
                    velocity.x = 0.0f;
                    velocity.z = 0.0f;
                    onGround = true;
                    bobTime = 0.0f;
                }
            }

            return;
        }

        float terrainY = world.getHighestY(position.x, position.z);

        groundY = terrainY + GROUND_OFFSET;
        bobTime += delta * GROUND_BOB_SPEED;

        float bobOffset = (float) Math.sin(bobTime) * GROUND_BOB_HEIGHT;

        position.y = groundY + bobOffset;
        float friction = Math.max(0.0f, 1.0f - GROUND_FRICTION * delta);
        velocity.x *= friction;
        velocity.z *= friction;

        if (Math.abs(velocity.x) < 0.05f) {
            velocity.x = 0.0f;
        }

        if (Math.abs(velocity.z) < 0.05f) {
            velocity.z = 0.0f;
        }
    }

    @Override
    public void render(GameMaster gameMaster) {
        SpriteSheet spriteSheet = gameMaster.getResourceManager().getItemSpriteSheet(item);

        Shader shader = gameMaster.getResourceManager().getShader("item");

        if (spriteSheet == null || shader == null) {
            return;
        }

        gameMaster.getItemRenderer().renderWorldItem(gameMaster,
                this, gameMaster.getCelestialLighting());
    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
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

    public void addVelocity(float x, float y, float z) {
        this.velocity.add(x, y, z);
    }

    public boolean isOnGround() {
        return onGround;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public boolean canBePickedUp() {
        return pickupTimer <= 0.0f;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getBobTime() {
        return bobTime;
    }

    public void setBobTime(float bobTime) {
        this.bobTime = bobTime;
    }
}