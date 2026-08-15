package com.sfarm4j.data;

import com.sfarm4j.utils.K;
import org.joml.Vector2f;
import org.joml.Vector3f;

@DataClass
public enum BlockData {
    DIRT((byte) 0, "Dirt", 100, K.Colors.STONE, 0, 0),
    GRASS((byte) 1, "Grass", 120, K.Colors.GRASS, 1, 0),
    STONE((byte) 2, "Stone", 150, K.Colors.STONE, 2, 0),
    DISPENSER((byte) 3, "Dispenser", 500, K.Colors.STONE, 3, 0);

    public static final int ATLAS_COLS = K.UI.ICON_BLOCK_ATLAS_FRAMES;
    public static final int ATLAS_ROWS = K.UI.ICON_BLOCK_ATLAS_ROWS;

    private final byte id;
    private final String name;
    private final int value;
    private final Vector3f color;
    private final int tileX;
    private final int tileY;

    BlockData(byte id, String name, int value, Vector3f color, int tileX, int tileY) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.color = color;
        this.tileX = tileX;
        this.tileY = tileY;
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

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public Vector2f getAtlasScale() {
        return new Vector2f(1.0f / ATLAS_COLS, 1.0f / ATLAS_ROWS);
    }

    public Vector2f getAtlasOffset() {
        float scaleX = 1.0f / ATLAS_COLS;
        float scaleY = 1.0f / ATLAS_ROWS;
        float offsetX = tileX * scaleX;
        float offsetY = (ATLAS_ROWS - 1 - tileY) * scaleY;
        return new Vector2f(offsetX, offsetY);
    }
}