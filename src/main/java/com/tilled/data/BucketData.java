package com.tilled.data;

@DataClass
public enum BucketData {
    EMPTY( (byte) 0, "Empty"),
    WATER( (byte) 1, "Water");

    private final byte id;
    private final String name;

    BucketData(byte id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isWater() {
        return this == WATER;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
