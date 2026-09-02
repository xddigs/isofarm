package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import com.isofarm.data.DataClass;

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

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        int size = SIZE_X * SIZE_Y * SIZE_Z;

        this.blocks = new byte[size];
        this.waterLevels = new byte[size];
    }

    public byte getBlock(int x, int y, int z) {
        if (!isOutOfBounds(x, y, z)) {
            return blocks[getIndex(x, y, z)];
        }

        return 0;
    }

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

    private boolean isPlant(byte blockId) {
        BlockData data = BlockData.fromId(blockId);
        return data != null && data.isPlant();
    }

    public byte getWaterLevel(int x, int y, int z) {
        if (!isOutOfBounds(x, y, z)) {
            return waterLevels[getIndex(x, y, z)];
        }

        return 0;
    }

    public void setWaterLevel(int x, int y, int z, byte waterLevel) {
        if (!isOutOfBounds(x, y, z)) {
            waterLevels[getIndex(x, y, z)] = waterLevel;
        }
    }

    public byte[] getBlocks() {
        return blocks;
    }

    public byte[] getWaterLevels() {
        return waterLevels;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

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

    public static int indexToX(int index) {
        return index % SIZE_X;
    }

    public static int indexToZ(int index) {
        return (index / SIZE_X) % SIZE_Z;
    }

    public static int indexToY(int index) {
        return index / (SIZE_X * SIZE_Z);
    }

    private int getIndex(int x, int y, int z) {
        return x + SIZE_X * (z + SIZE_Z * y);
    }

    private boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= SIZE_X ||
                y < 0 || y >= SIZE_Y ||
                z < 0 || z >= SIZE_Z;
    }
}