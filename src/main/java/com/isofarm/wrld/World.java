package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import com.isofarm.data.Crop;
import com.isofarm.data.Hit;
import com.isofarm.item.Block;
import com.isofarm.pathfinding.GridPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class World {
    private final Map<Long, Block> blocks;
    private final Map<Long, Chunk> chunks;

    public World() {
        this.blocks = new HashMap<>();
        this.chunks = new HashMap<>();
    }

    public long get2DKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    public Chunk getOrCreateChunk(int chunkX, int chunkZ) {
        long key = get2DKey(chunkX, chunkZ);

        return chunks.computeIfAbsent(key, k -> new Chunk(chunkX, chunkZ));
    }

    public boolean isChunkLoadedAt(int x, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);
        return chunks.containsKey(get2DKey(chunkX, chunkZ));
    }

    public Map<Long, Chunk> getChunks() {
        return chunks;
    }

    public Block getBlockAt(int x, int y, int z) {
        long key = getBlockKey(x, y, z);
        Block registeredBlock = blocks.get(key);

        if (registeredBlock != null) {
            return registeredBlock;
        }

        byte blockId = getBlockTypeAt(x, y, z);
        if (blockId == 0) {
            return null;
        }

        BlockData data = getBlockData(blockId);
        if (data == null) {
            return null;
        }

        return new Block(data, x, y, z);
    }

    public Block getBlockAt(Hit pos) {
        return getBlockAt(pos.x(), pos.y(), pos.z());
    }

    public void addBlock(Block block) {
        if (block == null) return;
        blocks.put(getBlockKey(block.getX(), block.getY(), block.getZ()), block);
    }

    public void removeBlock(Block block) {
        if (block == null) return;
        long key = getBlockKey(block.getX(), block.getY(), block.getZ());
        if (blocks.get(key) == block) {
            blocks.remove(key);
        }
    }

    public void addCrop(Crop crop) {
        addBlock(crop);
    }

    public void removeCrop(Crop crop) {
        removeBlock(crop);
    }

    public Crop getCropAt(int x, int y, int z) {
        Block block = blocks.get(getBlockKey(x, y, z));
        if (block instanceof Crop crop) {
            return crop;
        }
        return null;
    }

    public Map<Long, Block> getBlocks() {
        return blocks;
    }

    public void forEach(Consumer<Block> consumer) {
        for (Block block : blocks.values()) {
            consumer.accept(block);
        }
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
        if (y < 0 || y >= Chunk.SIZE_Y) return 0;
        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);

        Chunk chunk = chunks.get(get2DKey(chunkX, chunkZ));
        if (chunk == null) return 0;
        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);

        return chunk.getBlock(localX, y, localZ);
    }

    public byte getBlockTypeAt(Hit pos) {
        return getBlockTypeAt(pos.x(), pos.y(), pos.z());
    }

    public void setBlockTypeAt(int x, int y, int z, byte blockId) {
        if (y < 0 || y >= Chunk.SIZE_Y) return;
        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);
        Chunk chunk = getOrCreateChunk(chunkX, chunkZ);

        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);
        chunk.setBlock(localX, y, localZ, blockId);
    }

    public void setBlockTypeAt(Hit pos, byte blockId) {
        setBlockTypeAt(pos.x(), pos.y(), pos.z(), blockId);
    }

    public byte getWaterLevelAt(int x, int y, int z) {
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

        return chunk.getWaterLevel(localX, y, localZ);
    }

    public byte getWaterLevelAt(Hit pos) {
        return getWaterLevelAt(pos.x(), pos.y(), pos.z());
    }

    public void setWaterLevelAt(int x, int y, int z, byte waterLevel) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return;
        }

        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);

        Chunk chunk = getOrCreateChunk(chunkX, chunkZ);

        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);

        chunk.setWaterLevel(localX, y, localZ, waterLevel);
    }

    public void setWaterLevelAt(Hit pos, byte waterLevel) {
        setWaterLevelAt(pos.x(), pos.y(), pos.z(), waterLevel);
    }

    public long getBlockKey(int x, int y, int z) {
        long keyXZ = get2DKey(x, z);
        return keyXZ ^ ((long) y * 0x9E3779B97F4A7C15L);
    }

    private BlockData getBlockData(byte blockId) {
        for (BlockData data : BlockData.values()) {
            if (data.getId() == blockId) {
                return data;
            }
        }

        return null;
    }

    public boolean isBlockSolid(int x, int y, int z) {
        byte blockId = getBlockTypeAt(x, y, z);
        BlockData block = BlockData.fromId(blockId);
        return block != null && block.isSolid();
    }

    public GridPos getHighestY(float spawnX, float spawnZ) {
        int blockX = (int) Math.floor(spawnX);
        int blockZ = (int) Math.floor(spawnZ);

        for (int y = Chunk.SIZE_Y - 1; y >= 0; y--) {
            byte blockId = getBlockTypeAt(blockX, y, blockZ);
            if (blockId != 0) {
                return new GridPos(blockX, y, blockZ);
            }
        }
        return new GridPos(blockX, 0, blockZ);
    }

    public void forEachPlant(Consumer<PlantInstance> consumer) {
        for (Chunk chunk : chunks.values()) {
            int[] plantIndices = chunk.getPlantIndices();

            for (int index : plantIndices) {
                int localX = Chunk.indexToX(index);
                int localY = Chunk.indexToY(index);
                int localZ = Chunk.indexToZ(index);
                int worldX = chunk.getChunkX() * Chunk.SIZE_X + localX;
                int worldZ = chunk.getChunkZ() * Chunk.SIZE_Z + localZ;

                byte blockId = chunk.getBlock(localX, localY, localZ);
                BlockData data = BlockData.fromId(blockId);
                if (data == null || !data.isPlant()) {
                    continue;
                }

                consumer.accept(new PlantInstance(chunk, worldX, localY, worldZ, data));
            }
        }
    }

    public record PlantInstance(Chunk chunk, int x, int y, int z, BlockData data) {}
}