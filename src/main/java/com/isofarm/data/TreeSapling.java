package com.isofarm.data;

/**
 * Provides tree sapling behavior.
 */
public class TreeSapling {
    private final int x, y, z;
    private final BlockData treeType;
    private int currentTicks;
    private int targetTicks;

    /**
     * Creates a new {@code TreeSapling} instance.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param treeType the tree type value
     * @param targetTicks the target ticks value
     */
    public TreeSapling(int x, int y, int z, BlockData treeType, int targetTicks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.treeType = treeType;
        this.currentTicks = 0;
        this.targetTicks = targetTicks;
    }

    /**
     * Performs the tick operation.
     * @return the tick result
     */
    public boolean tick() {
        currentTicks++;
        return currentTicks >= targetTicks;
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
     * Returns the tree type.
     * @return the tree type
     */
    public BlockData getTreeType() {
        return treeType;
    }
}