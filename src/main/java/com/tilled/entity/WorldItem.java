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
    private static final float PICKUP_DELAY = 0.75f;
    private static final float AIR_DRAG = 2.5f;

    private final Item item;
    private int amount;
    private final Vector3f position;
    private final Vector3f velocity;
    private boolean onGround;
    private World world;

    private float rotation;
    private float bobTime;

    private float pickupTimer;

    public WorldItem(Item item, int amount, Vector3f position) {
        super(item.getName());
        this.item = item;
        this.amount = amount;
        this.position = new Vector3f(position);
        this.velocity = new Vector3f();
        this.onGround = false;
        this.pickupTimer = PICKUP_DELAY;
    }

    @Override
    public void update(float delta) {
        if (world == null || delta <= 0.0f) {
            return;
        }

        if (pickupTimer > 0.0f) {
            pickupTimer -= delta;
        }

        if (!onGround) {
            velocity.y += GRAVITY * delta;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        position.z += velocity.z * delta;

        velocity.x *= Math.max(0.0f, 1.0f - AIR_DRAG * delta);
        velocity.z *= Math.max(0.0f, 1.0f - AIR_DRAG * delta);

        float groundY = world.getHighestY(position.x, position.z);

        if (position.y <= groundY + GROUND_OFFSET) {
            position.y = groundY + GROUND_OFFSET;

            velocity.set(0.0f, 0.0f, 0.0f);

            onGround = true;
        } else {
            onGround = false;
        }
    }

    @Override
    public void render(GameMaster gameMaster) {
        SpriteSheet spriteSheet = gameMaster.getResourceManager().getItemSpriteSheet(item);
        Shader shader = gameMaster.getResourceManager().getShader("item");
        if (spriteSheet == null || shader == null) {
            return;
        }

        gameMaster.getItemRenderer().renderWorldItem(gameMaster, this,
                gameMaster.getCelestialLighting());
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
}