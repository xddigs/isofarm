package com.tilled.wrld;

import com.tilled.data.Block;
import com.tilled.data.BlockData;
import com.tilled.data.Crop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World {
    private final Map<Long, Crop> crops = new HashMap<>();
    private final Map<Long, Chunk> chunks = new HashMap<>();

    private long get2DKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    public void addCrop(Crop crop) {
        long key = get2DKey((int) crop.getX(), (int) crop.getZ());

        crops.put(key, crop);
    }

    public void removeCrop(Crop crop) {
        long key = get2DKey((int) crop.getX(), (int) crop.getZ());

        if (crops.get(key) == crop) {
            crops.remove(key);
        }
    }

    public Crop getCropAt(int x, int z) {
        return crops.get(get2DKey(x, z));
    }

    public List<Crop> getActiveCrops() {
        return List.copyOf(crops.values());
    }

    public Chunk getOrCreateChunk(int chunkX, int chunkZ) {
        long key = get2DKey(chunkX, chunkZ);
        return chunks.computeIfAbsent(key, k -> new Chunk(chunkX, chunkZ));
    }

    public int getChunkBlockTypeAt(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);

        Chunk chunk = getOrCreateChunk(chunkX, chunkZ);

        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);

        return chunk.getBlock(localX, y, localZ);
    }

    public byte getBlockTypeAt(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return 0;
        }

        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);

        Chunk chunk = chunks.get(get2DKey(chunkX, chunkZ));

        if (chunk == null) {
            return 0;
        }

        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);

        return chunk.getBlock(localX, y, localZ);
    }

    public Block getBlockAt(int x, int y, int z) {
        byte blockId = getBlockTypeAt(x, y, z);

        if (blockId == 0) {
            return null;
        }

        for (BlockData data : BlockData.values()) {
            if (data.getId() == blockId) {
                return new Block(data, x, y, z);
            }
        }
        return null;
    }

    public void setBlockTypeAt(int x, int y, int z, byte blockId) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return;
        }

        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);
        Chunk chunk = getOrCreateChunk(chunkX, chunkZ);
        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);
        chunk.setBlock(localX, y, localZ, blockId);
    }

    public byte getWaterLevelAt(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return 0;
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        Chunk chunk = chunks.get(get2DKey(chunkX, chunkZ));

        if (chunk == null) {
            return 0;
        }

        int localX = x & 15;
        int localZ = z & 15;

        return chunk.getWaterLevel(localX, y, localZ);
    }

    public void setWaterLevelAt(int x, int y, int z, byte waterLevel) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return;
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        Chunk chunk = getOrCreateChunk(chunkX, chunkZ);
        int localX = x & 15;
        int localZ = z & 15;
        chunk.setWaterLevel(localX, y, localZ, waterLevel);
    }

    public Map<Long, Chunk> getChunks() {
        return chunks;
    }
}