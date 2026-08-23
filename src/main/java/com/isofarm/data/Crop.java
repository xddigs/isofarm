package com.isofarm.data;

import com.isofarm.item.Block;

@DataClass
public class Crop extends Block {
    private final CropType type;
    private final Block block;
    private final Season season;
    private final int value;
    private final float targetGrowthTime = 8.0f;
    private GrowthStage stage;
    private float currentGrowthTime = 0.0f;
    private boolean wasHarvested;

    public Crop(int x, int y, int z,
                CropType type, Block block, Season season) {
        super(block.getType(), x, y, z);
        this.type = type;
        this.block = block;
        this.season = season;
        this.value = type.getValue();
        this.stage = GrowthStage.SEED;
        this.wasHarvested = false;
    }

    public CropType getCropType() {
        return type;
    }

    public Block getBlock() {
        return block;
    }

    public Season getSeason() {
        return season;
    }

    public int getValue() {
        return value;
    }

    public GrowthStage getStage() {
        return stage;
    }

    public boolean isReadyToHarvest() {
        return stage == GrowthStage.HARVESTABLE;
    }

    public boolean wasHarvested() {
        return wasHarvested;
    }

    public void setHarvested(boolean wasHarvested) {
        this.wasHarvested = wasHarvested;
    }

    public void update(float delta, WeatherType weather) {
        if (isReadyToHarvest()) {
            return;
        }

        float soilBonus = block != null && block.hasWater() ? 2.5f : 0.8f;
        float effectiveDelta = delta * weather.getGrowthMultiplier() * soilBonus;
        currentGrowthTime += effectiveDelta;
        float progress = currentGrowthTime / targetGrowthTime;

        if (progress >= 1.0f) {
            stage = GrowthStage.HARVESTABLE;
        } else if (progress >= 0.75f) {
            stage = GrowthStage.MATURE;
        } else if (progress >= 0.50f) {
            stage = GrowthStage.GROWING;
        } else if (progress >= 0.25f) {
            stage = GrowthStage.BUD;
        } else {
            stage = GrowthStage.SEED;
        }
    }
}