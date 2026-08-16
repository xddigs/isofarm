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

    public void water(World world, int amount) {
        water -= amount;
        for (Block b : world.getBlocks().values()) {
            if (b.getWaterLevel() < MAX_WATER) {
                b.setWaterLevel(b.getWaterLevel() + amount);
            }
        }
    }

    public void use(World world) {
        super.use();
        water(world, 1);
    }
}
