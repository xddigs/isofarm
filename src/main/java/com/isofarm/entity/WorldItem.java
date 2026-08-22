package com.isofarm.entity;

import com.isofarm.data.DataClass;
import com.isofarm.data.Hit;
import com.isofarm.data.Item;
import com.isofarm.graphics.Shader;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@DataClass
public class WorldItem extends Entity {
    private static final Logger log = LoggerFactory.getLogger(WorldItem.class);
    private static final float GRAVITY = -20.0f;
    private static final float GROUND_OFFSET = 0.02f;
    private static final float ITEM_HEIGHT = 0.45f;

    private static final float PICKUP_DELAY = 0.2f;

    private static final float AIR_DRAG = 2.5f;
    private static final float GROUND_FRICTION = 8.0f;

    private static final float BOUNCE_FACTOR = 0.20f;
    private static final float MIN_BOUNCE_VELOCITY = 1.2f;

    private static final float ROTATION_SPEED = 90.0f;
    private static final float GROUND_BOB_SPEED = 3.0f;
    private static final float GROUND_BOB_HEIGHT = 0.08f;

    private final Item item;
    private int amount;
    private World world;
    private float rotation;
    private float bobTime;
    private float pickupTimer;
    private float groundY;
    private boolean isAttracting = false;

    public WorldItem(Item item, int amount, Vector3f position) {
        super(item.getName());
        this.item = item;
        this.amount = amount;

        setPosition(new Vector3f(position));
        setVelocity(new Vector3f());
        setOnGround(false);

        this.rotation = 0.0f;
        this.bobTime = 0.0f;
        this.pickupTimer = PICKUP_DELAY;
        this.groundY = position.y;
    }

    @Override
    public void update(Hit hit, float delta) {
        if (world == null || delta <= 0.0f) return;
        if (pickupTimer > 0.0f) pickupTimer -= delta;

        rotation = (rotation + ROTATION_SPEED * delta) % 360.0f;
        int currentX = (int) Math.floor(position.x);
        int currentZ = (int) Math.floor(position.z);

        int targetY = world.getHighestY(currentX, currentZ).y();
        float surfaceY = targetY + 1.0f;
        float targetGroundY = surfaceY + GROUND_OFFSET + ITEM_HEIGHT * 0.5f;

        if (!isOnGround()) {
            velocity.y += GRAVITY * delta;
            float airDamping = Math.max(0.0f, 1.0f - AIR_DRAG * delta);
            velocity.x *= airDamping;
            velocity.z *= airDamping;

            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
            position.z += velocity.z * delta;

            if (position.y <= targetGroundY) {
                position.y = targetGroundY;
                groundY = targetGroundY;

                if (Math.abs(velocity.y) > MIN_BOUNCE_VELOCITY) {
                    velocity.y = -velocity.y * BOUNCE_FACTOR;
                } else {
                    velocity.set(0, 0, 0);
                    setOnGround(true);
                    bobTime = 0.0f;
                }
            }
        } else {
            position.y = targetGroundY + (float) Math.sin(bobTime) * GROUND_BOB_HEIGHT;
            bobTime += delta * GROUND_BOB_SPEED;

            float friction = Math.max(0.0f, 1.0f - GROUND_FRICTION * delta);
            velocity.x *= friction;
            velocity.z *= friction;
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

    public void addVelocity(float x, float y, float z) {
        this.velocity.add(x, y, z);
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

    public boolean isAttracting() {
        return isAttracting;
    }

    public void setAttracting(boolean attracting) {
        this.isAttracting = attracting;
    }
}