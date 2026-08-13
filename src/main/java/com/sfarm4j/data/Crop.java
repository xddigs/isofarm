package com.sfarm4j.data;

@DataClass
public class Crop {
    private float x, z;
    private final CropType type;
    private final Season season;
    private final int value;
    private final int daysToGrow;
    private int days;
    private boolean readyToHarvest;

    public Crop(float x, float z,
                CropType type, Season season, int value) {
        this.x = x;
        this.z = z;
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

    public Season getSeason() {
        return season;
    }

    public int getValue() {
        return value;
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
        if (this.days >= daysToGrow) {
            this.readyToHarvest = true;
        }
    }
}