package com.tilled.data;

public enum Season {
    WINTER((byte) 0, "Winter", 0.5),
    SPRING((byte) 1, "Spring", 2.0),
    SUMMER((byte) 2, "Summer", 1.0),
    AUTUMN((byte) 3, "Autumn", 1.5);

    private final byte id;
    private final String name;
    private final double valueMultiplier;

    private Season(byte id, String name, double valueMultiplier) {
        this.id = id;
        this.name = name;
        this.valueMultiplier = valueMultiplier;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getValueMultiplier() {
        return valueMultiplier;
    }

    public static Season fromId(byte id) {
        for (Season season : Season.values()) {
            if (season.getId() == id) {
                return season;
            }
        }
        return SPRING;
    }
}
