package com.sfarm4j.data;

@DataClass
public class Cell {
    private final float x, z;
    private CellType type;
    private boolean hasCrop;
    private boolean isUnlocked;

    public Cell(float x, float z) {
        this.x = x;
        this.z = z;
    }

    public Cell(CellType type, float x, float z) {
        this(x, z);
        this.type = type;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public CellType getType() {
        return type;
    }

    public boolean hasCrop() {
        return hasCrop;
    }

    public void setCrop(boolean hasCrop) {
        this.hasCrop = hasCrop;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
}
