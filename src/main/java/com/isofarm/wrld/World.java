package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.Crop;
import com.isofarm.data.Singleton;
import com.isofarm.item.Block;
import com.isofarm.pathfinding.GridPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Provides world behavior.
 */
@Singleton
public class World {
    public static final World wrld = new World();
    private final Map<Long, Block> blocks = new HashMap<>();
    private final Map<Long, Chunk> chunks = new HashMap<>();

    /**
     * Creates a new private {@code World} instance.
     */
    private World() {}

    /**
     * Returns get2 dkey.
     * @param x the x value
     * @param z the z value
     * @return the get2 dkey result
     */
    public long get2DKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    /**
     * Returns the or create chunk.
     * @param chunkX the chunk x value
     * @param chunkZ the chunk z value
     * @return the or create chunk
     */
    public Chunk getOrCreateChunk(int chunkX, int chunkZ) {
        long key = get2DKey(chunkX, chunkZ);

        return chunks.computeIfAbsent(key, k -> new Chunk(chunkX, chunkZ));
    }

    /**
     * Checks whether the chunk loaded at condition is met.
     * @param x the x value
     * @param z the z value
     * @return {@code true} if chunk loaded at; otherwise {@code false}
     */
    public boolean isChunkLoadedAt(int x, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);
        return chunks.containsKey(get2DKey(chunkX, chunkZ));
    }

    /**
     * Returns the chunks.
     * @return the chunks
     */
    public Map<Long, Chunk> getChunks() {
        return chunks;
    }

    /**
     * Returns the block at.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the block at
     */
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

        BlockData data = BlockData.fromId(blockId);
        if (data == null) {
            return null;
        }

        return new Block(data, x, y, z);
    }

    /**
     * Returns the block at.
     * @param pos the pos value
     * @return the block at
     */
    public Block getBlockAt(BlockPos pos) {
        return getBlockAt(pos.x(), pos.y(), pos.z());
    }

    /**
     * Adds the block.
     * @param block the block value
     */
    public void addBlock(Block block) {
        if (block == null) return;
        blocks.put(getBlockKey(block.getX(), block.getY(), block.getZ()), block);
    }

    /**
     * Removes the block.
     * @param block the block value
     */
    public void removeBlock(Block block) {
        if (block == null) return;
        long key = getBlockKey(block.getX(), block.getY(), block.getZ());
        if (blocks.get(key) == block) {
            blocks.remove(key);
        }
    }

    /**
     * Adds the crop.
     * @param crop the crop value
     */
    public void addCrop(Crop crop) {
        addBlock(crop);
    }

    /**
     * Removes the crop.
     * @param crop the crop value
     */
    public void removeCrop(Crop crop) {
        removeBlock(crop);
    }

    /**
     * Returns the crop at.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the crop at
     */
    public Crop getCropAt(int x, int y, int z) {
        Block block = blocks.get(getBlockKey(x, y, z));
        if (block instanceof Crop crop) {
            return crop;
        }
        return null;
    }

    /**
     * Returns the blocks.
     * @return the blocks
     */
    public Map<Long, Block> getBlocks() {
        return blocks;
    }

    /**
     * Performs the for each operation.
     * @param consumer the consumer value
     */
    public void forEach(Consumer<Block> consumer) {
        for (Block block : blocks.values()) {
            consumer.accept(block);
        }
    }

    /**
     * Returns the chunk block type at.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the chunk block type at
     */
    public int getChunkBlockTypeAt(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);

        Chunk chunk = getOrCreateChunk(chunkX, chunkZ);

        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);

        return chunk.getBlock(localX, y, localZ);
    }

    /**
     * Returns the block type at.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the block type at
     */
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

    /**
     * Returns the block type at.
     * @param pos the pos value
     * @return the block type at
     */
    public byte getBlockTypeAt(BlockPos pos) {
        return getBlockTypeAt(pos.x(), pos.y(), pos.z());
    }

    /**
     * Removes the block at.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public void removeBlockAt(int x, int y, int z) {
        blocks.remove(getBlockKey(x, y, z));
    }

    /**
     * Sets the block type at.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param blockId the block id value
     */
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

        if (blockId == BlockData.AIR.getId()) {
            removeBlockAt(x, y, z);
        }
    }

    /**
     * Sets the block type at.
     * @param pos the pos value
     * @param blockId the block id value
     */
    public void setBlockTypeAt(BlockPos pos, byte blockId) {
        setBlockTypeAt(pos.x(), pos.y(), pos.z(), blockId);
    }

    /**
     * Returns the water level at.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the water level at
     */
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

    /**
     * Returns the water level at.
     * @param pos the pos value
     * @return the water level at
     */
    public byte getWaterLevelAt(BlockPos pos) {
        return getWaterLevelAt(pos.x(), pos.y(), pos.z());
    }

    /**
     * Sets the water level at.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param waterLevel the water level value
     */
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

    /**
     * Sets the water level at.
     * @param pos the pos value
     * @param waterLevel the water level value
     */
    public void setWaterLevelAt(BlockPos pos, byte waterLevel) {
        setWaterLevelAt(pos.x(), pos.y(), pos.z(), waterLevel);
    }

    /**
     * Returns the block key.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the block key
     */
    public long getBlockKey(int x, int y, int z) {
        return (((long) x & 0x3FFFFFFL) << 38) |
                (((long) z & 0x3FFFFFFL) << 12) |
                ((long) y & 0xFFFL);
    }

    /**
     * Returns the block data.
     * @param blockId the block id value
     * @return the block data
     */
    private BlockData getBlockData(byte blockId) {
        for (BlockData data : BlockData.values()) {
            if (data.getId() == blockId) {
                return data;
            }
        }

        return null;
    }

    /**
     * Checks whether the block solid condition is met.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return {@code true} if block solid; otherwise {@code false}
     */
    public boolean isBlockSolid(int x, int y, int z) {
        byte blockId = getBlockTypeAt(x, y, z);
        BlockData block = BlockData.fromId(blockId);
        if (block == null) return false;
        if (block.isFluid()) return false;
        return block.isSolid();
    }

    /**
     * Returns the highest y.
     * @param spawnX the spawn x value
     * @param spawnZ the spawn z value
     * @return the highest y
     */
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

    /**
     * Performs the for each plant operation.
     * @param consumer the consumer value
     */
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

    /**
     * Stores plant instance data.
     */
    public record PlantInstance(Chunk chunk, int x, int y, int z, BlockData data) {}
}