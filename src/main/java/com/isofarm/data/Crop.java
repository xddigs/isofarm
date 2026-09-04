package com.isofarm.data;

import com.isofarm.item.Block;

/**
 * Provides crop behavior.
 */
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

    /**
     * Creates a new {@code Crop} instance.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param type the type value
     * @param block the block value
     * @param season the season value
     */
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

    /**
     * Returns the crop type.
     * @return the crop type
     */
    public CropType getCropType() {
        return type;
    }

    /**
     * Returns the block.
     * @return the block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Returns the season.
     * @return the season
     */
    public Season getSeason() {
        return season;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the stage.
     * @return the stage
     */
    public GrowthStage getStage() {
        return stage;
    }

    /**
     * Checks whether the ready to harvest condition is met.
     * @return {@code true} if ready to harvest; otherwise {@code false}
     */
    public boolean isReadyToHarvest() {
        return stage == GrowthStage.HARVESTABLE;
    }

    /**
     * Performs the was harvested operation.
     * @return the was harvested result
     */
    public boolean wasHarvested() {
        return wasHarvested;
    }

    /**
     * Sets the harvested.
     * @param wasHarvested the was harvested value
     */
    public void setHarvested(boolean wasHarvested) {
        this.wasHarvested = wasHarvested;
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     * @param weather the weather value
     */
    public void update(float delta, WeatherType weather) {
        if (isReadyToHarvest() && !type.equals(CropType.SUGAR_CANE_CROP)) {
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