package com.sfarm4j.data;

public enum CropType {
    WHEAT((byte) 0, "Wheat", "Your main source of income! Great base crop", 4, 5, 4),
    CARROT((byte) 1, "Carrot", "Such a delicious vegetable, and a good crop at that too!", 3, 8, 6),
    POTATO((byte) 2, "Potato", "Boring! Although, very good as side fries. Great crop!", 6, 12, 8),
    BEETROOT((byte) 3, "BEETROOT", "Ugh I love this one, it's such a treat! Income wise is good!", 4, 16, 2);

    private final byte id;
    private final String name;
    private final String description;
    private final int yield;
    private final int value;
    private final int seeds;

    CropType(byte id,
             String name, String description,
             int yield, int value, int seeds) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
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
