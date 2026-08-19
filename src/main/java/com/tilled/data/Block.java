package com.tilled.data;

import com.tilled.utils.K;

@DataClass
public class Block extends Item {
    private final int waterLevelMax = K.World.WATER_LEVEL_MAX;
    private BlockData type;
    private int x, y, z;
    private int waterLevel = 15;

    public Block(BlockData type, int x, int y, int z) {
        super(type.getId(), "(Block) " + type.getName(), 1, type.getValue());
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block(BlockData type, int x, int z) {
        this(type, x, 0, z);
    }

    public Block(BlockData type) {
        super(type.getId(), type.getName(), 1, type.getValue());
        this.type = type;
    }

    public Block() {
        this(BlockData.CROP);
    }

    public BlockData getType() {
        return type;
    }

    public void setType(BlockData type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getWaterLevelMax() {
        return waterLevelMax;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void addWater(int amount) {
        waterLevel = Math.min(waterLevelMax, waterLevel + amount);
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    public boolean hasWater() {
        return waterLevel > 0;
    }

    @Override
    public Item copy(int newAmount) {
        if (newAmount <= 0) return null;
        Block block = new Block(type, x, y, z);
        block.setAmount(newAmount);
        block.setWaterLevel(waterLevel);
        return block;
    }
}