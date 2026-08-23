package com.isofarm.data;

public enum ToolType {
    HOE(5, new BlockData[]{BlockData.GRASS, BlockData.DIRT, BlockData.TILLED_DIRT}, 8, new float[]{0.5f, 0.25f, 0.75f}),
    AXE(8, new BlockData[]{BlockData.OAK_LOG, BlockData.OAK_WOOD}, 10, new float[]{1.0f, 0.5f}),
    PICKAXE(6, new BlockData[]{BlockData.STONE, BlockData.VOIDSTONE}, 7, new float[]{0.75f, 10.0f}),
    SHOVEL(4, new BlockData[]{BlockData.GRASS, BlockData.DIRT}, 5, new float[]{0.25f}),
    SWORD(7, new BlockData[]{}, 10, new float[]{}),
    ELSE(0, new BlockData[]{}, 0, new float[]{});

    private final float baseDamage;
    private final BlockData[] usableOn;
    private final int baseDurability;
    private final float[] efficiency;

    ToolType(float baseDamage, BlockData[] usableOn, int baseDurability, float[] efficiency) {
        this.baseDamage = baseDamage;
        this.usableOn = usableOn;
        this.baseDurability = baseDurability;
        this.efficiency = efficiency;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public BlockData[] getUsableOn() {
        return usableOn;
    }

    public int getBaseDurability() {
        return baseDurability;
    }

    public float[] getEfficiency() {
        return efficiency;
    }
}
