package com.sfarm4j.data;

@DataClass
public enum BlockData {
    DIRT((byte) 0, "Dirt", 0),
    GRASS((byte) 1, "Grass", 1),
    STONE((byte) 2, "Stone", 2),
    AUTOMATIC_HARVESTER((byte) 3, "Harvester", 3);

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
