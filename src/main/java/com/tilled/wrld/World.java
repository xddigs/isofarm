package com.tilled.wrld;

import com.tilled.data.Block;
import com.tilled.data.Crop;
import java.util.*;

public class World {
    private final Map<Long, Crop> crops = new HashMap<>();
    private final Map<Long, Chunk> chunks = new HashMap<>();
    private final Map<Long, Block> activeBlocks = new HashMap<>();

    private long get2DKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    private long get3DKey(int x, int y, int z) {
        return (((long) x & 0x1FFFFFL) << 43) |
                (((long) z & 0x1FFFFFL) << 22) |
                ((long) y & 0x3FFFFFFL);
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

    public byte getBlockTypeAt(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) return 0;

        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        Chunk chunk = chunks.get(get2DKey(chunkX, chunkZ));
        if (chunk == null) return 0;
        int localX = x & 15;
        int localZ = z & 15;
        return chunk.getBlock(localX, y, localZ);
    }

    public void setBlockTypeAt(int x, int y, int z, byte blockId) {
        if (y < 0 || y >= Chunk.SIZE_Y) return;

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        Chunk chunk = getOrCreateChunk(chunkX, chunkZ);

        int localX = x & 15;
        int localZ = z & 15;

        chunk.setBlock(localX, y, localZ, blockId);
    }

    public boolean addBlock(Block block) {
        long key = get3DKey(block.getX(), block.getY(), block.getZ());
        if (activeBlocks.containsKey(key)) {
            return false;
        }

        setBlockTypeAt(block.getX(), block.getY(), block.getZ(), block.getType().getId());
        activeBlocks.put(key, block);
        return true;
    }

    public boolean removeBlock(Block block) {
        long key = get3DKey(block.getX(), block.getY(), block.getZ());
        setBlockTypeAt(block.getX(), block.getY(), block.getZ(), (byte) 0);
        return activeBlocks.remove(key) != null;
    }

    public Block getBlockAt(int x, int y, int z) {
        return activeBlocks.get(get3DKey(x, y, z));
    }

    public Map<Long, Chunk> getChunks() {
        return chunks;
    }

    public Collection<Block> getActiveBlocks() {
        return activeBlocks.values();
    }
}