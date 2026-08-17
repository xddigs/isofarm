package com.tilled.data;

import com.tilled.utils.K;
import org.joml.Vector2f;

@SuppressWarnings("all")
@DataClass
public enum BlockData {
    DIRT((byte) 1, "Dirt", 100, 0, 0),
    GRASS((byte) 2, "Grass", true, 120, 1, 0, 1, 3, 1, 1),
    STONE((byte) 3, "Stone", 150, 2, 0),
    DISPENSER((byte) 4, "Dispenser", 500, 3, 0),
    TILLED_DIRT((byte) 5, "Tilled Dirt", true, 110, 4, 0, 0, 0, 0, 0),
    WATER((byte) 6, "Water", 100, 5, 0),
    CROP((byte) 7, "Crop", -1, 6, 0);

    public static final int ATLAS_COLS = K.UI.BLOCK_ATLAS_FRAMES;
    public static final int ATLAS_ROWS = K.UI.ICON_BLOCK_ATLAS_ROWS;

    private final byte id;
    private final String name;
    private final boolean isTillable;
    private final int value;

    private final int topTileX;
    private final int topTileY;
    private final int bottomTileX;
    private final int bottomTileY;
    private final int sideTileX;
    private final int sideTileY;

    private final Vector2f atlasScale;
    private final Vector2f topAtlasOffset;
    private final Vector2f bottomAtlasOffset;
    private final Vector2f sideAtlasOffset;

    BlockData(byte id, String name, int value, int tileX, int tileY, boolean isTillable) {
        this(id, name, isTillable, value, tileX, tileY, tileX, tileY, tileX, tileY);
    }

    BlockData(byte id, String name, int value, int tileX, int tileY) {
        this(id, name, false, value,
                tileX, tileY,
                tileX, tileY,
                tileX, tileY);
    }

    BlockData(byte id, String name, boolean isTillable, int value,
              int topTileX, int topTileY,
              int bottomTileX, int bottomTileY,
              int sideTileX, int sideTileY) {
        this.id = id;
        this.name = name;
        this.isTillable = isTillable;
        this.value = value;
        this.topTileX = topTileX;
        this.topTileY = topTileY;
        this.bottomTileX = bottomTileX;
        this.bottomTileY = bottomTileY;
        this.sideTileX = sideTileX;
        this.sideTileY = sideTileY;

        this.atlasScale = new Vector2f(1.0f / ATLAS_COLS, 1.0f / ATLAS_ROWS);
        this.topAtlasOffset = calculateAtlasOffset(topTileX, topTileY);
        this.bottomAtlasOffset = calculateAtlasOffset(bottomTileX, bottomTileY);
        this.sideAtlasOffset = calculateAtlasOffset(sideTileX, sideTileY);
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

    public boolean isTillable() {
        return isTillable;
    }

    public int getTileX() {
        return sideTileX;
    }

    public int getTileY() {
        return sideTileY;
    }

    public Vector2f getAtlasScale() {
        return atlasScale;
    }

    private Vector2f calculateAtlasOffset(int tx, int ty) {
        float scaleX = 1.0f / ATLAS_COLS;
        float scaleY = 1.0f / ATLAS_ROWS;
        float offsetX = tx * scaleX;
        float offsetY = (ATLAS_ROWS > 1) ? (ATLAS_ROWS - 1 - ty) * scaleY : ty * scaleY;
        return new Vector2f(offsetX, offsetY);
    }

    public Vector2f getTopAtlasOffset() {
        return topAtlasOffset;
    }

    public Vector2f getBottomAtlasOffset() {
        return bottomAtlasOffset;
    }

    public Vector2f getSideAtlasOffset() {
        return sideAtlasOffset;
    }

    public Vector2f getAtlasOffset() {
        return sideAtlasOffset;
    }

    public static BlockData fromId(byte id) {
        for (BlockData block : values()) {
            if (block.getId() == id) {
                return block;
            }
        }
        return null;
    }
}