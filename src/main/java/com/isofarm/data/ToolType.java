package com.isofarm.data;

import com.isofarm.item.Item;

public enum ToolType implements Item {
    BACKPACK((byte) 0, " Backpack", 0, BlockData.all(), 1, new float[]{}),
    CRAFTING_KIT((byte) 1, " Crafting Kit", 0, BlockData.all(), 1, new float[]{}),
    BUCKET((byte) 2, " Bucket", 0, BlockData.all(), 1, new float[]{}),
    HOE((byte) 3, " Hoe", 5, new BlockData[]{BlockData.GRASS, BlockData.DIRT, BlockData.TILLED_DIRT}, 8, new float[]{0.5f, 0.25f, 0.75f}),
    AXE((byte) 4, " Axe", 8, new BlockData[]{BlockData.OAK_LOG, BlockData.OAK_WOOD}, 10, new float[]{1.0f, 0.5f}),
    PICKAXE((byte) 5, " Pickaxe", 6, new BlockData[]{BlockData.GRASS, BlockData.DIRT, BlockData.OAK_LEAVES, BlockData.STONE, BlockData.COPPER_ORE}, 7, new float[]{0.8f, 0.7f, 1.2f, 0.65f, 0.56f}),
    SHOVEL((byte) 6, " Shovel", 4, new BlockData[]{BlockData.GRASS, BlockData.DIRT}, 5, new float[]{0.3f, 0.25f}),
    SWORD((byte) 7, " Sword", 7, BlockData.all(), 10, new float[]{});

    private final byte id;
    private final String name;
    private final float baseDamage;
    private final BlockData[] usableOn;
    private final int baseDurability;
    private final float[] efficiency;

    ToolType(byte id, String name, float baseDamage, BlockData[] usableOn, int baseDurability, float[] efficiency) {
        this.id = id;
        this.name = name;
        this.baseDamage = baseDamage;
        this.usableOn = usableOn;
        this.baseDurability = baseDurability;
        this.efficiency = efficiency;
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
        return 0;
    }

    @Override
    public Item copy() {
        return this;
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
