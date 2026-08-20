package com.tilled.entity;

import com.tilled.data.*;
import com.tilled.service.ToastService;
import com.tilled.utils.K;
import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("FieldMayBeFinal")
@DataClass
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final String name;
    private final ToastService toastService;
    private Vector3f position;
    private Vector3f velocity;
    private Vector3f dimensions;
    private boolean onGround;

    public Player(String name, World world, ToastService toastService) {
        super(name, toastService);
        this.name = name;
        this.toastService = toastService;

        float spawnX = 0.5f;
        float spawnZ = 0.5f;
        float highestY = world.getHighestY(spawnX, spawnZ);

        this.position = new Vector3f(spawnX, highestY, spawnZ);
        this.velocity = new Vector3f();
        this.dimensions = new Vector3f(0.6f, 2.0f, 0.6f);
        setUpInventory();
    }

    private void setUpInventory() {
        add(new Seed(), 4);
        add(new Seed(CropType.CARROT), 4);
        add(new Hoe(), 1);
        add(new Pickaxe(), 1);
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void render(GameMaster gameMaster) {

    }

    public String getName() {
        return name;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void sell(Item item, int amount) {
        if (item == null || amount <= 0) return;

        int current = getInventory().getAmount(item);
        if (current <= 0) {
            log.warn("No {} in inventory to sell", item.getName());
            return;
        }

        int toSell = Math.min(current, amount);
        getInventory().remove(item, toSell);
        int earnings = toSell * item.getValue();
        toastService.sell("You successfully sold " + item.getName() + " for " + earnings + " coins");
        earn(earnings);
    }

    public void add(Item item, int amount) {
        getInventory().add(item, amount);
        log.info("Added x{} of {} to inventory", amount, item.getName());
    }

    public void add(Item item) {
        getInventory().add(item, 1);
        log.info("Added x1 of {} to inventory", item.getName());
    }

    public void remove(Item item, int amount) {
        getInventory().remove(item, amount);
        log.info("Removed x{} of {} to inventory", amount, item.getName());
    }

    public void remove(Item item) {
        getInventory().remove(item, 1);
        log.info("Removed x1 of {} from inventory", item.getName());
    }

    public void clear() {
        getInventory().clear();
        log.info("Cleared inventory");
    }

    public boolean isEmpty() {
        return getInventory().isEmpty();
    }

    public int size() {
        return getInventory().size();
    }

    public Item get(int index) {
        return getInventory().get(index);
    }

    public Item get(Item item) {
        return getInventory().get(item);
    }

    public int getAmount(Item item) {
        return getInventory().getAmount(item);
    }

    public void earn(int amount) {
        log.info("Earned ${}", amount);
        getPurse().add(amount);
    }

    public void spend(int amount) {
        if (amount <= 0) return;
        log.info("Spent ${}", amount);
        getPurse().remove(amount);
    }

    public boolean hasSeeds() {
        return getInventory().hasItemOfType(Seed.class);
    }

    public boolean isMoving() {
        return (velocity.x * velocity.x + velocity.z * velocity.z) > 0.01f;
    }

    public void jump() {
        if (!onGround) {
            return;
        }

        velocity.y = K.World.JUMP_FORCE;
        onGround = false;
    }

    public void moveAndCollide(World world, Vector3f targetVelocity, float delta, boolean isOrthographic) {
        float smooth = 1.0f - (float) Math.exp(-12.0f * delta);

        velocity.x += (targetVelocity.x - velocity.x) * smooth;
        velocity.z += (targetVelocity.z - velocity.z) * smooth;

        if (isOrthographic) {
            velocity.y = 0.0f;
            position.x += velocity.x * delta;
            position.z += velocity.z * delta;
            return;
        }

        velocity.y += K.World.GRAVITY * delta;
        onGround = false;

        position.x += velocity.x * delta;
        if (checkCollision(world)) {
            position.x -= velocity.x * delta;
            velocity.x = 0;
        }

        position.y += velocity.y * delta;
        if (checkCollision(world)) {
            position.y -= velocity.y * delta;
            if (velocity.y < 0) {
                onGround = true;
            }
            velocity.y = 0;
        }

        position.z += velocity.z * delta;
        if (checkCollision(world)) {
            position.z -= velocity.z * delta;
            velocity.z = 0;
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

        float playerMinX = position.x - dimensions.x / 2.0f + epsilon;
        float playerMaxX = position.x + dimensions.x / 2.0f - epsilon;

        float playerMinY = position.y + epsilon;
        float playerMaxY = position.y + dimensions.y - epsilon;

        float playerMinZ = position.z - dimensions.z / 2.0f + epsilon;
        float playerMaxZ = position.z + dimensions.z / 2.0f - epsilon;

        float blockMaxX = blockX + 1.0f;
        float blockMaxY = blockY + 1.0f;
        float blockMaxZ = blockZ + 1.0f;

        return playerMinX < blockMaxX &&
                playerMaxX > (float) blockX &&
                playerMinY < blockMaxY &&
                playerMaxY > (float) blockY &&
                playerMinZ < blockMaxZ &&
                playerMaxZ > (float) blockZ;
    }

    public Vector3f getEyePosition() {
        float eyeHeight = 1.6f;
        return new Vector3f(position.x, position.y + eyeHeight, position.z);
    }

    public float getForward() {
        return (float) Math.atan2(velocity.z, velocity.x);
    }
}