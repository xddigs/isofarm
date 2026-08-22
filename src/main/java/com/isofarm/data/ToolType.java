package com.isofarm.data;

public enum ToolType {
    HOE(new BlockData[]{BlockData.GRASS, BlockData.DIRT, BlockData.TILLED_DIRT}, 8, new float[]{0.5f, 0.25f, 0.75f}),
    AXE(new BlockData[]{BlockData.OAK_LOG, BlockData.OAK_WOOD}, 10, new float[]{1.0f, 0.5f}),
    PICKAXE(new BlockData[]{BlockData.STONE, BlockData.VOIDSTONE}, 7, new float[]{0.75f, 10.0f}),
    SHOVEL(new BlockData[]{BlockData.GRASS, BlockData.DIRT}, 5, new float[]{0.25f}),
    SWORD(new BlockData[]{}, 10, new float[]{}),
    ELSE(new BlockData[]{}, 0, new float[]{});

    private final BlockData[] usableOn;
    private final int baseDurability;
    private final float[] efficiency;

    ToolType(BlockData[] usableOn, int baseDurability, float[] efficiency) {
        this.usableOn = usableOn;
        this.baseDurability = baseDurability;
        this.efficiency = efficiency;
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
