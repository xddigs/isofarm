package com.sfarm4j.data;

@DataClass
public class Crop {
    private final CropType type;
    private final Season season;
    private final int value;
    private final int daysToGrow;
    private int days;
    private boolean canBeHarvested;

    public Crop(CropType type, Season season,
                int value, boolean canBeHarvested) {
        this.type = type;
        this.season = season;
        this.value = value;
        this.daysToGrow = type.getDaysToGrow();
        this.canBeHarvested = canBeHarvested;
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

    public boolean isCanBeHarvested() {
        return canBeHarvested;
    }

    public void grow() {
        this.days += 1;
        if (days >= daysToGrow) {
            this.days = 0;
            harvest();
        }
    }

    private void harvest() {
        if (daysToGrow < type.getDaysToGrow()) return;
        this.canBeHarvested = true;
    }
}
