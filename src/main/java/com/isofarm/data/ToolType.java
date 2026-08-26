package com.isofarm.data;

public enum ToolType {
    BACKPACK(" Backpack", 0, BlockData.all(), 1, new float[]{}),
    CRAFTING_KIT(" Crafting Kit", 0, BlockData.all(), 1, new float[]{}),
    BUCKET(" Bucket", 0, BlockData.all(), 1, new float[]{}),
    HOE(" Hoe", 5, new BlockData[]{BlockData.GRASS, BlockData.DIRT, BlockData.TILLED_DIRT}, 8, new float[]{0.5f, 0.25f, 0.75f}),
    AXE(" Axe", 8, new BlockData[]{BlockData.OAK_LOG, BlockData.OAK_WOOD}, 10, new float[]{1.0f, 0.5f}),
    PICKAXE(" Pickaxe", 6, new BlockData[]{BlockData.GRASS, BlockData.DIRT, BlockData.OAK_LEAVES, BlockData.STONE, BlockData.COPPER_ORE}, 7, new float[]{0.8f, 0.7f, 1.2f, 0.65f, 0.56f}),
    SHOVEL(" Shovel", 4, new BlockData[]{BlockData.GRASS, BlockData.DIRT}, 5, new float[]{0.3f, 0.25f}),
    SWORD(" Sword", 7, BlockData.all(), 10, new float[]{});

    private final String name;
    private final float baseDamage;
    private final BlockData[] usableOn;
    private final int baseDurability;
    private final float[] efficiency;

    ToolType(String name, float baseDamage, BlockData[] usableOn, int baseDurability, float[] efficiency) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.usableOn = usableOn;
        this.baseDurability = baseDurability;
        this.efficiency = efficiency;
    }

    public String getName() {
        return name;
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
