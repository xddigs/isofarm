package com.isofarm.data;

import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

@DataClass
public class WateringCan extends Tool {
    private static final int MAX_WATER = 100;
    private int water;

    public WateringCan() {
        super((byte) 1, "Watering Can", 100,
                ToolType.ELSE, Tier.COPPER, 128);
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

    public void use(GameMaster gameMaster) {
        setPlayer(gameMaster.getPlayer());
        super.use();
        water(gameMaster.getWorld());
    }

    @Override
    public Item copy() {
        WateringCan wateringCan = new WateringCan();
        wateringCan.water = water;
        wateringCan.setPlayer(getPlayer());
        return wateringCan;
    }
}