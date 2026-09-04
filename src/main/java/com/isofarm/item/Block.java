package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.DataClass;
import com.isofarm.utils.K;

/**
 * Provides block behavior.
 */
@DataClass
public class Block implements Craftable {
    private final int waterLevelMax = K.World.WATER_LEVEL_MAX;
    private final byte id;
    private final String name;
    private final int value;
    private BlockData type;
    private int x, y, z;
    private int waterLevel = 15;
    private boolean isInteractive;

    /**
     * Creates a new {@code Block} instance.
     * @param type the type value
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public Block(BlockData type, int x, int y, int z) {
        this.id = type.getId();
        this.name = type.getDisplayName();
        this.value = type.getValue();
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isInteractive = false;
    }

    /**
     * Creates a new {@code Block} instance.
     * @param type the type value
     * @param pos the pos value
     */
    public Block(BlockData type, BlockPos pos) {
        this(type, pos.x(), 0, pos.y());
    }

    /**
     * Creates a new {@code Block} instance.
     * @param type the type value
     */
    public Block(BlockData type) {
        this.id = type.getId();
        this.name = type.getDisplayName();
        this.value = type.getValue();
        this.type = type;
    }

    /**
     * Creates a new {@code Block} instance.
     */
    public Block() {
        this(BlockData.DIRT);
    }

    /**
     * Returns the id.
     * @return the id
     */
    @Override
    public byte getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return type.getDisplayName();
    }

    /**
     * Returns the value.
     * @return the value
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * Returns the type.
     * @return the type
     */
    public BlockData getType() {
        return type;
    }

    /**
     * Sets the type.
     * @param type the type value
     */
    public void setType(BlockData type) {
        this.type = type;
    }

    /**
     * Returns the x.
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y.
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the z.
     * @return the z
     */
    public int getZ() {
        return z;
    }

    /**
     * Returns the water level max.
     * @return the water level max
     */
    public int getWaterLevelMax() {
        return waterLevelMax;
    }

    /**
     * Returns the water level.
     * @return the water level
     */
    public int getWaterLevel() {
        return waterLevel;
    }

    /**
     * Adds the water.
     * @param amount the amount value
     */
    public void addWater(int amount) {
        waterLevel = Math.min(waterLevelMax, waterLevel + amount);
    }

    /**
     * Sets the water level.
     * @param waterLevel the water level value
     */
    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    /**
     * Checks whether the water condition is met.
     * @return {@code true} if water; otherwise {@code false}
     */
    public boolean hasWater() {
        return waterLevel > 0;
    }

    /**
     * Checks whether the interactive condition is met.
     * @return {@code true} if interactive; otherwise {@code false}
     */
    public boolean isInteractive() {
        return isInteractive;
    }

    /**
     * Sets the isInteractive value
     * @param interactive the interactive value
     */
    public void setInteractive(boolean interactive) {
        isInteractive = interactive;
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Block(type, x, y, z);
    }
}