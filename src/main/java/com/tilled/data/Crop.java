package com.tilled.data;

@DataClass
public class Crop extends Item {
    private final float x, y, z;
    private final CropType type;
    private final Block block;
    private final Season season;
    private final int value;
    private GrowthStage stage;

    private float currentGrowthTime = 0.0f;
    private final float targetGrowthTime = 8.0f;
    private boolean wasHarvested;

    public Crop(float x, float y, float z,
                CropType type, Block block, Season season) {
        super(type.getId(), type.getName(), 1, type.getValue());
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.stage = GrowthStage.SEED;
        this.type = type;
        this.season = season;
        this.value = type.getValue();
        this.wasHarvested = false;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public CropType getType() {
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
        return this.stage == GrowthStage.HARVESTABLE;
    }

    public boolean wasHarvested() {
        return wasHarvested;
    }

    public void setHarvested(boolean wasHarvested) {
        this.wasHarvested = wasHarvested;
    }

    public void update(float delta, WeatherType weather) {
        if (isReadyToHarvest()) return;

        float soilBonus = (block != null && block.hasWater()) ? 1.5f : 0.5f;
        float effectiveDelta = delta * weather.getGrowthMultiplier() *
                soilBonus * block.getWaterLevel() / 10.0f;

        currentGrowthTime += effectiveDelta;
        float progress = currentGrowthTime / targetGrowthTime;

        if (progress >= 1.0f) {
            this.stage = GrowthStage.HARVESTABLE;
        } else if (progress >= 0.75f) {
            this.stage = GrowthStage.MATURE;
        } else if (progress >= 0.50f) {
            this.stage = GrowthStage.GROWING;
        } else if (progress >= 0.25f) {
            this.stage = GrowthStage.BUD;
        } else {
            this.stage = GrowthStage.SEED;
        }
    }
}