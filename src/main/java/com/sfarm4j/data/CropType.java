package com.sfarm4j.data;

public enum CropType {
    WHEAT((byte) 0, "Wheat", 1, 4),
    CARROT((byte) 1, "Carrot", 4, 3),
    POTATO((byte) 2, "Potato", 6, 6),
    CABBAGE((byte) 3, "Cabbage", 8, 4),;

    private final byte id;
    private final String name;
    private final int daysToGrow;
    private final int yield;

    CropType(byte id,
             String name,
             int daysToGrow,
             int yield) {
        this.id = id;
        this.name = name;
        this.daysToGrow = daysToGrow;
        this.yield = yield;
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

    public static CropType fromId(byte id) {
        for (CropType crop : CropType.values()) {
            if (crop.getId() == id) {
                return crop;
            }
        }
        return null;
    }
}
