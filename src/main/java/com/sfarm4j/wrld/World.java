package com.sfarm4j.wrld;

import com.sfarm4j.data.Block;
import com.sfarm4j.data.Crop;
import java.util.*;

public class World {
    private final Map<Long, Crop> crops = new HashMap<>();
    private final Map<String, Block> blocks = new HashMap<>();
    private final List<Crop> activeCropsCache = new ArrayList<>();
    private final List<Crop> unmodifiableActiveCrops = Collections.unmodifiableList(activeCropsCache);

    private long getCellKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    public void addCrop(Crop crop) {
        long key = getCellKey(Math.round(crop.getX()), Math.round(crop.getZ()));
        crops.put(key, crop);
        activeCropsCache.add(crop);
    }

    public void removeCrop(Crop crop) {
        long key = getCellKey(Math.round(crop.getX()), Math.round(crop.getZ()));
        if (crops.remove(key) != null) {
            activeCropsCache.remove(crop);
        }
    }

    public boolean addBlock(Block block) {
        String key = block.getX() + "," + block.getY() + "," + block.getZ();
        if (blocks.containsKey(key)) {
            return false;
        }
        blocks.put(key, block);
        return true;
    }

    public Map<String, Block> getBlocks() {
        return blocks;
    }

    public List<Crop> getActiveCrops() {
        return unmodifiableActiveCrops;
    }

    public Crop getCropAt(int x, int z) {
        return crops.get(getCellKey(x, z));
    }

    public Block getBlockAt(int x, int y, int z) {
        return blocks.get(x + "," + y + "," + z);
    }
}