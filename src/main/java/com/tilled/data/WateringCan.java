package com.tilled.data;

import com.tilled.wrld.Chunk;
import com.tilled.wrld.World;

@DataClass
public class WateringCan extends Tool {
    private static final int MAX_WATER = 100;
    private int water;

    public WateringCan() {
        super((byte) 1, "Watering Can", 100, 128);
        this.water = MAX_WATER;
    }

    public int getWater() {
        return water;
    }

    private void water(World world) {
        water -= 1;

        for (Chunk chunk : world.getChunks().values()) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    for (int x = 0; x < Chunk.SIZE_X; x++) {
                        byte blockId = chunk.getBlock(x, y, z);
                        if (blockId == 0) {
                            continue;
                        }
                        int currentWater = chunk.getWaterLevel(x, y, z);
                        if (currentWater < MAX_WATER) {
                            int newWater = Math.min(currentWater + MAX_WATER / 2, MAX_WATER);
                            chunk.setWaterLevel(x, y, z, (byte) newWater);
                        }
                    }
                }
            }
        }
    }

    public void use(World world) {
        super.use();
        water(world);
    }

    @Override
    public Item copy(int newAmount) {
        WateringCan wateringCan = new WateringCan();
        wateringCan.addAmount(newAmount);
        wateringCan.water = water;
        return wateringCan;
    }
}