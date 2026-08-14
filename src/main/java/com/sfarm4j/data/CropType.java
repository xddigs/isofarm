package com.sfarm4j.data;

public enum CropType {
    WHEAT((byte) 0, "Wheat", 4, 5, 4),
    CARROT((byte) 1, "Carrot", 3, 8, 6),
    POTATO((byte) 2, "Potato", 6, 12, 8),
    CABBAGE((byte) 3, "Cabbage", 4, 16, 2);

    private final byte id;
    private final String name;
    private final int yield;
    private final int value;
    private final int seeds;

    CropType(byte id,
             String name,
             int yield, int value, int seeds) {
        this.id = id;
        this.name = name;
        this.yield = yield;
        this.value = value;
        this.seeds = seeds;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getYield() {
        return yield;
    }

    public int getValue() {
        return value;
    }

    public int getSeeds() {
        return seeds;
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
