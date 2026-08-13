package com.sfarm4j.data;

@DataClass
public class Crop extends Item {
    private final float x, z;
    private GrowthStage stage;
    private final CropType type;
    private final Cell cell;
    private final Season season;
    private final int value;
    private final int daysToGrow;
    private int daysGrown;
    private boolean readyToHarvest;

    public Crop(float x, float z,
                CropType type, Cell cell, Season season, int value) {
        super(type.getName(), 1, value);
        this.x = x;
        this.z = z;
        this.cell = cell;
        this.stage = GrowthStage.SEED;
        this.type = type;
        this.season = season;
        this.value = value;
        this.daysToGrow = type.getDaysToGrow();
        this.daysGrown = 0;
        this.readyToHarvest = false;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public CropType getType() {
        return type;
    }

    public Cell getCell() {
        return cell;
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

    public int getDaysToGrow() {
        return daysToGrow;
    }

    public int getDaysGrown() {
        return daysGrown;
    }

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    public void grow() {
        if (readyToHarvest) {
            return;
        }

        daysGrown++;

        if (daysGrown >= daysToGrow) {
            this.readyToHarvest = true;
            this.stage = GrowthStage.HARVESTABLE;
        } else {
            float progress = (float) daysGrown / daysToGrow;
            if (progress >= 0.90f) {
                this.stage = GrowthStage.MATURE;
            } else if (progress >= 0.75f) {
                this.stage = GrowthStage.GROWING;
            } else if (progress >= 0.50f) {
                this.stage = GrowthStage.BUD;
            } else if (progress >= 0.25f) {
                this.stage = GrowthStage.SEED;
            }
        }
    }
}