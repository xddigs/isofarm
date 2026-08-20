package com.soilcraft.data;

public enum ToolType {
    HOE(new BlockData[]{BlockData.GRASS, BlockData.DIRT}, 8),
    AXE(new BlockData[]{BlockData.OAK_LOG, BlockData.OAK_WOOD}, 10),
    PICKAXE(new BlockData[]{BlockData.STONE, BlockData.VOIDSTONE}, 7),
    SHOVEL(new BlockData[]{BlockData.GRASS, BlockData.DIRT}, 5),
    SWORD(new BlockData[]{}, 10),
    ELSE(new BlockData[]{}, 0);

    private final BlockData[] usableOn;
    private final int baseDurability;

    ToolType(BlockData[] usableOn, int baseDurability) {
        this.usableOn = usableOn;
        this.baseDurability = baseDurability;
    }

    public BlockData[] getUsableOn() {
        return usableOn;
    }

    public int getBaseDurability() {
        return baseDurability;
    }
}
