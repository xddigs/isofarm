package com.tilled.data;

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
        for (Block b : world.getBlocks().values()) {
            if (b.getWaterLevel() < MAX_WATER) {
                b.setWaterLevel(b.getWaterLevel() + MAX_WATER/2);
            }
        }
    }

    public void use(World world) {
        super.use();
        water(world);
    }
}
