package com.tilled.data;

import com.tilled.service.ToastService;
import com.tilled.utils.K;
import com.tilled.wrld.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("FieldMayBeFinal")
@DataClass
public class Player {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final String name;
    private final Inventory inventory;
    private final Purse purse;
    private final ToastService toastService;
    private Vector3f position;
    private Vector3f velocity;
    private Vector3f dimensions;
    private int experience;
    private int level;
    private boolean onGround;

    public Player(String name, World world, ToastService toastService) {
        this.name = name;
        this.toastService = toastService;
        this.inventory = new Inventory();
        this.purse = new Purse(inventory, new Coin());

        float spawnX = 0.5f;
        float spawnZ = 0.5f;
        float highestY = world.getHighestY(spawnX, spawnZ);

        this.position = new Vector3f(spawnX, highestY, spawnZ);
        this.velocity = new Vector3f();
        this.dimensions = new Vector3f(0.6f, 1.5f, 0.6f);
        setUpInventory();
    }

    private void setUpInventory() {
        add(new Seed(), 4);
        add(new Hoe(), 1);
        add(new Block(BlockData.STONE), 999);
    }

    public String getName() {
        return name;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Purse getPurse() {
        return purse;
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

    private boolean detectGround(World world) {
        position.y -= 0.05f;
        boolean collision = checkCollision(world);
        position.y += 0.05f;
        return collision;
    }

    public void update() {
    }

    public void gain(int amount) {
        experience += amount;
        if (isLevelUpAvailable()) {
            level++;
            experience = 0;
            log.info("Level up! New level: {}", level);
            toastService.success("Level up! New level: " + level);
        }
    }

    private boolean isLevelUpAvailable() {
        return experience >= (10 * level);
    }

    public void sell(Item item, int amount) {
        if (item == null || amount <= 0) return;

        int current = inventory.getAmount(item);
        if (current <= 0) {
            log.warn("No {} in inventory to sell", item.getName());
            return;
        }

        int toSell = Math.min(current, amount);
        inventory.remove(item, toSell);
        int earnings = toSell * item.getValue();
        toastService.sell("You successfully sold " + item.getName() + " for " + earnings + " coins");
        earn(earnings);
    }

    public void add(Item item, int amount) {
        inventory.add(item, amount);
        log.info("Added x{} of {} to inventory", amount, item.getName());
    }

    public void add(Item item) {
        inventory.add(item, 1);
        log.info("Added x1 of {} to inventory", item.getName());
    }

    public void remove(Item item, int amount) {
        inventory.remove(item, amount);
        log.info("Removed x{} of {} to inventory", amount, item.getName());
    }

    public void remove(Item item) {
        inventory.remove(item, 1);
        log.info("Removed x1 of {} from inventory", item.getName());
    }

    public void clear() {
        inventory.clear();
        log.info("Cleared inventory");
    }

    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    public int size() {
        return inventory.size();
    }

    public Item get(int index) {
        return inventory.get(index);
    }

    public Item get(Item item) {
        return inventory.get(item);
    }

    public int getAmount(Item item) {
        return inventory.getAmount(item);
    }

    public void earn(int amount) {
        log.info("Earned ${}", amount);
        purse.add(amount);
    }

    public void spend(int amount) {
        if (amount <= 0) return;
        log.info("Spent ${}", amount);
        purse.remove(amount);
    }

    public int purse() {
        return purse.getBalance();
    }

    public boolean hasSeeds() {
        return inventory.hasItemOfType(Seed.class);
    }

    public int getSeedCount() {
        return inventory.getTotalAmountOfType(Seed.class);
    }

    public void jump() {
        if (!onGround) {
            return;
        }

        velocity.y = K.World.JUMP_FORCE;
        onGround = false;
    }

    public void moveAndCollide(World world, Vector3f targetVelocity, float delta) {
        float smooth = 1.0f - (float) Math.exp(-12.0f * delta);

        velocity.x += (targetVelocity.x - velocity.x) * smooth;
        velocity.z += (targetVelocity.z - velocity.z) * smooth;

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

    public boolean isOnGround(Block block) {
        return position.y <= block.getY();
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

    public Vector3f getEyePosition() {
        float eyeHeight = 1.6f;
        return new Vector3f(position.x, position.y + eyeHeight, position.z);
    }
}