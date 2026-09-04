package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import com.isofarm.data.DataClass;

/**
 * Provides chunk behavior.
 */
@DataClass
public class Chunk {
    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 256;
    public static final int SIZE_Z = 16;

    private final int chunkX;
    private final int chunkZ;
    private final byte[] blocks;
    private final byte[] waterLevels;

    private int[] plantIndices;
    private boolean plantCacheDirty = true;

    /**
     * Creates a new {@code Chunk} instance.
     * @param chunkX the chunk x value
     * @param chunkZ the chunk z value
     */
    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        int size = SIZE_X * SIZE_Y * SIZE_Z;

        this.blocks = new byte[size];
        this.waterLevels = new byte[size];
    }

    /**
     * Returns the block.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the block
     */
    public byte getBlock(int x, int y, int z) {
        if (!isOutOfBounds(x, y, z)) {
            return blocks[getIndex(x, y, z)];
        }

        return 0;
    }

    /**
     * Sets the block.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param blockId the block id value
     */
    public void setBlock(int x, int y, int z, byte blockId) {
        if (isOutOfBounds(x, y, z)) {
            return;
        }

        int index = getIndex(x, y, z);
        byte oldBlockId = blocks[index];
        blocks[index] = blockId;
        boolean oldWasPlant = isPlant(oldBlockId);
        boolean newIsPlant = isPlant(blockId);
        if (oldWasPlant || newIsPlant) {
            plantCacheDirty = true;
        }
    }

    /**
     * Checks whether the plant condition is met.
     * @param blockId the block id value
     * @return {@code true} if plant; otherwise {@code false}
     */
    private boolean isPlant(byte blockId) {
        BlockData data = BlockData.fromId(blockId);
        return data != null && data.isPlant();
    }

    /**
     * Returns the water level.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the water level
     */
    public byte getWaterLevel(int x, int y, int z) {
        if (!isOutOfBounds(x, y, z)) {
            return waterLevels[getIndex(x, y, z)];
        }

        return 0;
    }

    /**
     * Returns the fluid level stored at a local chunk position.
     * @param x the local x value
     * @param y the local y value
     * @param z the local z value
     * @return the stored fluid level
     */
    public byte getFluidLevel(int x, int y, int z) {
        return getWaterLevel(x, y, z);
    }

    /**
     * Sets the water level.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param waterLevel the water level value
     */
    public void setWaterLevel(int x, int y, int z, byte waterLevel) {
        if (!isOutOfBounds(x, y, z)) {
            waterLevels[getIndex(x, y, z)] = waterLevel;
        }
    }

    /**
     * Sets the fluid level at a local chunk position.
     * @param x the local x value
     * @param y the local y value
     * @param z the local z value
     * @param fluidLevel the fluid level value
     */
    public void setFluidLevel(int x, int y, int z, byte fluidLevel) {
        setWaterLevel(x, y, z, fluidLevel);
    }

    /**
     * Returns the blocks.
     * @return the blocks
     */
    public byte[] getBlocks() {
        return blocks;
    }

    /**
     * Returns the water levels.
     * @return the water levels
     */
    public byte[] getWaterLevels() {
        return waterLevels;
    }

    /**
     * Returns the chunk x.
     * @return the chunk x
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Returns the chunk z.
     * @return the chunk z
     */
    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * Returns the plant indices.
     * @return the plant indices
     */
    public int[] getPlantIndices() {
        if (!plantCacheDirty && plantIndices != null) {
            return plantIndices;
        }

        int[] temp = new int[64];
        int count = 0;

        for (int index = 0; index < blocks.length; index++) {
            byte blockId = blocks[index];

            if (blockId == BlockData.AIR.getId()) {
                continue;
            }

            BlockData data = BlockData.fromId(blockId);
            if (data == null || !data.isPlant()) {
                continue;
            }

            if (count >= temp.length) {
                int[] expanded = new int[temp.length * 2];
                System.arraycopy(temp, 0, expanded, 0, temp.length);
                temp = expanded;
            }

            temp[count++] = index;
        }

        plantIndices = new int[count];
        System.arraycopy(temp, 0, plantIndices, 0, count);
        plantCacheDirty = false;
        return plantIndices;
    }

    /**
     * Performs the index to x operation.
     * @param index the index value
     * @return the index to x result
     */
    public static int indexToX(int index) {
        return index % SIZE_X;
    }

    /**
     * Performs the index to z operation.
     * @param index the index value
     * @return the index to z result
     */
    public static int indexToZ(int index) {
        return (index / SIZE_X) % SIZE_Z;
    }

    /**
     * Performs the index to y operation.
     * @param index the index value
     * @return the index to y result
     */
    public static int indexToY(int index) {
        return index / (SIZE_X * SIZE_Z);
    }

    /**
     * Returns the index.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the index
     */
    private int getIndex(int x, int y, int z) {
        return x + SIZE_X * (z + SIZE_Z * y);
    }

    /**
     * Checks whether the out of bounds condition is met.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return {@code true} if out of bounds; otherwise {@code false}
     */
    private boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= SIZE_X ||
                y < 0 || y >= SIZE_Y ||
                z < 0 || z >= SIZE_Z;
    }
}
