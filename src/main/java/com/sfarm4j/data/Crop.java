package com.sfarm4j.data;

@DataClass
public class Crop {
    private final float x, z;
    private GrowthStage stage;
    private final CropType type;
    private final Cell cell;
    private final Season season;
    private final int value;
    private final int daysToGrow;
    private int days;
    private boolean readyToHarvest;

    public Crop(float x, float z,
                CropType type, Cell cell, Season season, int value) {
        this.x = x;
        this.z = z;
        this.cell = cell;
        this.stage = GrowthStage.SEED;
        this.type = type;
        this.season = season;
        this.value = value;
        this.daysToGrow = type.getDaysToGrow();
        this.days = 0;
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

    public int getDays() {
        return days;
    }

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    public void grow() {
        if (readyToHarvest) {
            return;
        }

        this.days++;
        if (this.days % 2 == 0) {
            this.stage = stage.next(1);
        }

        if (this.days >= daysToGrow) {
            this.readyToHarvest = true;
            this.stage = GrowthStage.HARVESTABLE;
        }
    }
}