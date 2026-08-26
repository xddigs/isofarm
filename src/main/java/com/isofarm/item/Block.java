package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.DataClass;
import com.isofarm.utils.K;

@DataClass
public class Block implements Craftable {
    private final int waterLevelMax = K.World.WATER_LEVEL_MAX;
    private final byte id;
    private final String name;
    private final int value;
    private BlockData type;
    private int x, y, z;
    private int waterLevel = 15;

    public Block(BlockData type, int x, int y, int z) {
        this.id = type.getId();
        this.name = type.getName();
        this.value = type.getValue();
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block(BlockData type, int x, int z) {
        this(type, x, 0, z);
    }

    public Block(BlockData type) {
        this.id = type.getId();
        this.name = type.getName();
        this.value = type.getValue();
        this.type = type;
    }

    public Block() {
        this(BlockData.DIRT);
    }

    @Override
    public byte getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
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
    public Item copy() {
        return new Block(type, x, y, z);
    }

    public boolean hasSpecificDrop() {
        return type.getDrop() != null;
    }
}