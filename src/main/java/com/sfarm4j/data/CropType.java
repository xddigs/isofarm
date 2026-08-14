package com.sfarm4j.data;

public enum CropType {
    WHEAT((byte) 0, "Wheat", 1, 4, 5),
    CARROT((byte) 1, "Carrot", 4, 3, 8),
    POTATO((byte) 2, "Potato", 6, 6, 12),
    CABBAGE((byte) 3, "Cabbage", 8, 4, 16),;

    private final byte id;
    private final String name;
    private final int daysToGrow;
    private final int yield;
    private final int value;

    CropType(byte id,
             String name,
             int daysToGrow,
             int yield, int value) {
        this.id = id;
        this.name = name;
        this.daysToGrow = daysToGrow;
        this.yield = yield;
        this.value = value;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDaysToGrow() {
        return daysToGrow;
    }

    public int getYield() {
        return yield;
    }

    public int getValue() {
        return value;
    }

    public static CropType fromId(byte id) {
        for (CropType crop : CropType.values()) {
            if (crop.getId() == id) {
                return crop;
            }
        }
        return null;
    }
}
