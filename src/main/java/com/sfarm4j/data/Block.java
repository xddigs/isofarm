package com.sfarm4j.data;

@DataClass
public enum Block {
    DIRT((byte) 0, "Dirt"),
    GRASS((byte) 1, "Grass"),
    STONE((byte) 2, "Stone"),;

    private final byte id;
    private final String name;

    Block(byte id, String name) {
        this.id = id;
        this.name = name;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
