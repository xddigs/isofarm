package com.sfarm4j.data;

@DataClass
public enum BlockData {
    DIRT((byte) 0, "Dirt", 100),
    GRASS((byte) 1, "Grass", 120),
    STONE((byte) 2, "Stone", 150),
    DISPENSER((byte) 3, "Dispenser", 500);

    private final byte id;
    private final String name;
    private final int value;

    BlockData(byte id, String name, int value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
