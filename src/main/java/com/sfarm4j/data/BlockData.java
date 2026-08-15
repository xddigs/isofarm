package com.sfarm4j.data;

import com.sfarm4j.utils.K;
import org.joml.Vector3f;

@DataClass
public enum BlockData {
    DIRT((byte) 0, "Dirt", 100, K.Colors.STONE),
    GRASS((byte) 1, "Grass", 120, K.Colors.GRASS),
    STONE((byte) 2, "Stone", 150, K.Colors.STONE),
    DISPENSER((byte) 3, "Dispenser", 500, K.Colors.STONE);

    private final byte id;
    private final String name;
    private final int value;
    private final Vector3f color;

    BlockData(byte id, String name, int value, Vector3f color) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.color = color;
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

    public Vector3f getColor() {
        return color;
    }
}
