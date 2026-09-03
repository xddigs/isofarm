package com.isofarm.data;

import com.isofarm.utils.Local;

public enum CropType {
    WHEAT((byte) 0, "Wheat", "crop.wheat", 4, 5, 4),
    CARROT((byte) 1, "Carrot", "crop.carrot", 3, 8, 6),
    POTATO((byte) 2, "Potato", "crop.potato", 6, 12, 8),
    BEETROOT((byte) 3, "Beetroot", "crop.beetroot", 4, 16, 2);

    private final byte id;
    private final String name;
    private final String displayName;
    private final int yield;
    private final int value;
    private final int seeds;

    CropType(byte id, String name, String displayName,
             int yield, int value, int seeds) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
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

    public String getDisplayName() {
        return Local.lang.t(displayName);
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