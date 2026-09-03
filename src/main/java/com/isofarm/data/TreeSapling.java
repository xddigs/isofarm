package com.isofarm.data;

public class TreeSapling {
    private final int x, y, z;
    private final BlockData treeType;
    private int currentTicks;
    private int targetTicks;

    public TreeSapling(int x, int y, int z, BlockData treeType, int targetTicks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.treeType = treeType;
        this.currentTicks = 0;
        this.targetTicks = targetTicks;
    }

    public boolean tick() {
        currentTicks++;
        return currentTicks >= targetTicks;
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

    public BlockData getTreeType() {
        return treeType;
    }
}