package com.sfarm4j.wrld;

import com.sfarm4j.data.Block;
import com.sfarm4j.data.Crop;
import java.util.*;

public class World {
    private final Map<Long, Crop> crops = new HashMap<>();
    private final Map<String, Block> blocks = new HashMap<>();

    private long getCellKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    public void addCrop(Crop crop) {
        long key = getCellKey((int) crop.getX(), (int) crop.getZ());
        crops.put(key, crop);
    }

    public void removeCrop(Crop crop) {
        long key = getCellKey((int) crop.getX(), (int) crop.getZ());

        if (crops.get(key) == crop) {
            crops.remove(key);
        }
    }

    public Crop getCropAt(int x, int z) {
        return crops.get(getCellKey(x, z));
    }

    public boolean addBlock(Block block) {
        String key = block.getX() + "," + block.getY() + "," + block.getZ();
        if (blocks.containsKey(key)) {
            return false;
        }
        blocks.put(key, block);
        return true;
    }

    public boolean removeBlock(Block block) {
        String key = block.getX() + "," + block.getY() + "," + block.getZ();
        return blocks.remove(key) != null;
    }

    public Block getBlockAt(int x, int y, int z) {
        return blocks.get(x + "," + y + "," + z);
    }

    public Map<String, Block> getBlocks() {
        return blocks;
    }

    public List<Crop> getActiveCrops() {
        return List.copyOf(crops.values());
    }
}