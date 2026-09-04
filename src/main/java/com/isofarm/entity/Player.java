package com.isofarm.entity;

import com.isofarm.data.BlockPos;
import com.isofarm.data.Direction;
import com.isofarm.data.PlayerState;
import com.isofarm.data.RenderPass;
import com.isofarm.data.Singleton;
import com.isofarm.entity.plyr.PlayerAnimator;
import com.isofarm.entity.plyr.PlayerGameplay;
import com.isofarm.entity.plyr.PlayerManager;
import com.isofarm.item.Item;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector3f;

import java.util.List;

/**
 * Represents the local player and orchestrates its focused components.
 */
@Singleton
public class Player extends Character {
    public static final Player plyr;
    static {
        plyr = new Player();
        plyr.initialize();
    }

    private final PlayerAnimator animator;
    private final PlayerGameplay gameplay;
    private final PlayerManager manager;

    /**
     * Creates and initializes a player.
     */
    private Player() {
        super(null);
        gameplay = new PlayerGameplay();
        manager = new PlayerManager();
        animator = new PlayerAnimator();
    }

    /**
     * Initializes singleton components after the shared instance is assigned.
     */
    private void initialize() {
        gameplay.initialize();
        manager.initialize();
        animator.initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(BlockPos blockPos, float delta) {
        if (!gameplay.updateLifeCycle(delta)) return;
        manager.update(delta);
        animator.update(delta);
        gameplay.update(delta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GameMaster game, RenderPass pass) {
        animator.render(game, pass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDamageTaken(float amount) {
        gameplay.onDamageTaken(amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void dropLoot() {
        gameplay.dropLoot();
    }

    /**
     * Starts the attack animation.
     */
    public void interact() {
        animator.interact();
    }

    /**
     * @return whether an attack animation is active
     */
    public boolean isAttacking() {
        return animator.isAttacking();
    }

    /**
     * @param state state to enter
     */
    public void changeState(PlayerState state) {
        manager.changeState(state);
    }

    /**
     * @param velocity velocity @param delta frame time
     */
    public void autoJump(Vector3f velocity, float delta) {
        manager.autoJump(velocity, delta);
    }

    /**
     * @return active state
     */
    public PlayerState getCurrentState() {
        return manager.getCurrentState();
    }

    /**
     * @param state state to store
     */
    public void setCurrentState(PlayerState state) {
        manager.setCurrentState(state);
    }

    /**
     * Respawns the player.
     */
    public void respawn() {
        gameplay.respawn();
    }

    /**
     * Resets attributes.
     */
    public void resetAttributes() {
        gameplay.resetAttributes();
    }

    /**
     * @return damage event sequence
     */
    public int getDamageSequence() {
        return gameplay.getDamageSequence();
    }

    /**
     * @param delta frame time
     */
    public void move(float delta) {
        manager.move(delta);
    }

    /**
     * @param delta frame time @param yaw camera yaw @param flying flight flag
     */
    public void wasd(float delta, float yaw, boolean flying) {
        manager.wasd(delta, yaw, flying);
    }

    /**
     * @param delta frame time @param yaw camera yaw @param flying flight flag
     */
    public void fly(float delta, float yaw, boolean flying) {
        manager.fly(delta, yaw, flying);
    }

    /**
     * @return whether support exists below the proposed position
     */
    public boolean hasGroundBelow(float x, float z) {
        return manager.hasGroundBelow(x, z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void adjustVelocity(float delta) {
        manager.adjustVelocity(delta);
    }

    /**
     * @param item item @param amount quantity
     */
    public void sell(Item item, int amount) {
        gameplay.sell(item, amount);
    }

    /**
     * @param item item @param amount quantity
     */
    public void add(Item item, int amount) {
        gameplay.add(item, amount);
    }

    /**
     * @param item item
     */
    public void add(Item item) {
        gameplay.add(item);
    }

    /**
     * @param item item @param amount quantity
     */
    public void addToBackpack(Item item, int amount) {
        gameplay.addToBackpack(item, amount);
    }

    /**
     * @param item item
     */
    public void addToBackpack(Item item) {
        gameplay.addToBackpack(item);
    }

    /**
     * @param item item @param amount quantity
     */
    public void removeFromBackpack(Item item, int amount) {
        gameplay.removeFromBackpack(item, amount);
    }

    /**
     * @param item item
     */
    public void removeFromBackpack(Item item) {
        gameplay.removeFromBackpack(item);
    }

    /**
     * Sorts inventory storage.
     */
    public void sort() {
        gameplay.sort();
    }

    /**
     * @param item item @param amount quantity
     */
    public void remove(Item item, int amount) {
        gameplay.remove(item, amount);
    }

    /**
     * @param item item
     */
    public void remove(Item item) {
        gameplay.remove(item);
    }

    /**
     * Clears droppable inventory items.
     */
    public void clear() {
        gameplay.clear();
    }

    /**
     * @return whether inventory is empty
     */
    public boolean isEmpty() {
        return gameplay.isEmpty();
    }

    /**
     * @return inventory size
     */
    public int size() {
        return gameplay.size();
    }

    /**
     * @param index index @return indexed item
     */
    public Item get(int index) {
        return gameplay.get(index);
    }

    /**
     * @param item key @return matching item
     */
    public Item get(Item item) {
        return gameplay.get(item);
    }

    /**
     * @param item item @return quantity
     */
    public int getAmount(Item item) {
        return gameplay.getAmount(item);
    }

    /**
     * @param amount currency amount
     */
    public void earn(int amount) {
        gameplay.earn(amount);
    }

    /**
     * @param amount currency amount
     */
    public void spend(int amount) {
        gameplay.spend(amount);
    }

    /**
     * @return whether storage has space
     */
    public boolean hasSpace() {
        return gameplay.hasSpace();
    }

    /**
     * @return whether seeds are available
     */
    public boolean hasSeeds() {
        return gameplay.hasSeeds();
    }

    /**
     * @return interpolated eye height
     */
    public float getCurrentEyeHeight() {
        return manager.getCurrentEyeHeight();
    }

    /**
     * @return forward angle
     */
    public float getForward() {
        return manager.getForward();
    }

    /**
     * @return facing direction
     */
    public Direction getDirection() {
        return animator.getDirection();
    }

    /**
     * @return whether a path remains
     */
    public boolean isFollowingPath() {
        return manager.isFollowingPath();
    }

    /**
     * @return active path
     */
    public List<GridPos> getPath() {
        return manager.getPath();
    }

    /**
     * @param path path to follow
     */
    public void setPath(List<GridPos> path) {
        manager.setPath(path);
    }

    /**
     * @return path index
     */
    public int getPathIndex() {
        return manager.getPathIndex();
    }

    /**
     * @param index path index
     */
    public void setPathIndex(int index) {
        manager.setPathIndex(index);
    }

    /**
     * Clears the path.
     */
    public void clearPath() {
        manager.clearPath();
    }

    /**
     * @return respawn timer
     */
    public float getRespawnTimer() {
        return gameplay.getRespawnTimer();
    }

    /**
     * @param timer respawn timer
     */
    public void setRespawnTimer(float timer) {
        gameplay.setRespawnTimer(timer);
    }

    /**
     * @return target eye height
     */
    public float getTargetEyeHeight() {
        return manager.getTargetEyeHeight();
    }

    /**
     * @param height target eye height
     */
    public void setTargetEyeHeight(float height) {
        manager.setTargetEyeHeight(height);
    }

    /**
     * @return whether falling
     */
    public boolean isFalling() {
        return manager.isFalling();
    }

    /**
     * @param falling falling flag
     */
    public void setFalling(boolean falling) {
        manager.setFalling(falling);
    }

    /**
     * @return regeneration multiplier
     */
    public float getDifficultyRegen() {
        return gameplay.getDifficultyRegen();
    }
}
