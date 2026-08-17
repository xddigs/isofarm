package com.tilled.wrld;

import com.tilled.data.DataClass;

@DataClass
public class Chunk {
    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 128;
    public static final int SIZE_Z = 16;

    private final int chunkX;
    private final int chunkZ;
    private final byte[] blocks;
    private final byte[] waterLevels;

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
        if (!isOutOfBounds(x, y, z)) {
            blocks[getIndex(x, y, z)] = blockId;
        }
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

    private int getIndex(int x, int y, int z) {
        return x + SIZE_X * (z + SIZE_Z * y);
    }

    private boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= SIZE_X ||
                y < 0 || y >= SIZE_Y ||
                z < 0 || z >= SIZE_Z;
    }
}