package com.isofarm.data;

import com.isofarm.utils.K;
import org.joml.Vector2f;

@SuppressWarnings("all")
@DataClass
public enum BlockData {
    AIR((byte) 0, "Air", 0, 0, 0, SoundGroup.SILENT, 0f, true, new MaterialID[]{}),
    DIRT((byte) 1, "Dirt", true, 100, 0, 0, SoundGroup.SOIL, 0.9f, false, new MaterialID[]{}),
    GRASS((byte) 2, "Grass", true, 120, 1, 0, SoundGroup.SOIL, 1.0f, false, new MaterialID[]{}),
    STONE((byte) 3, "Stone", 150, 2, 0, SoundGroup.HARD, 6.0f, false, new MaterialID[]{}),
    TILLED_DIRT((byte) 4, "Tilled Dirt", true, false, new MaterialID[]{}, 110, 3, 0, 0, 0, 0, 0, SoundGroup.SOIL, 0.9f),
    VOIDSTONE((byte) 5, "Voidstone", 999, 4, 0, SoundGroup.HARD, 999999.0f, false, new MaterialID[]{}),
    GLASS((byte) 6, "Glass", 200, 5, 0, SoundGroup.GLASS, 1.2f, true, new MaterialID[]{}),
    OAK_LOG((byte) 7, "Log", false, false, new MaterialID[]{MaterialID.WOOD}, 100, 6, 0, 6, 2, 6, 1, SoundGroup.HARD, 1.2f),
    OAK_WOOD((byte) 8, "Oak Wood", 100, 7, 0, SoundGroup.HARD, 4.0f, false, new MaterialID[]{}),
    OAK_LEAVES((byte) 9, "Oak Leaves", 100, 8, 0, SoundGroup.SOIL, 1.1f, false, new MaterialID[]{MaterialID.STICK}),
    SNOW((byte) 10, "Snow", 120, 9, 0, SoundGroup.SNOW, 0.8f, false, new MaterialID[]{}),

    WATER((byte) -1, "Water", 100, 0, 0, SoundGroup.SILENT, 0f, true, new MaterialID[]{}),
    CROP((byte) -1, "Crop", -1, 0, 0, SoundGroup.SOIL, 1f, false, new MaterialID[]{});

    public static final int ATLAS_COLS = K.UI.BLOCK_ATLAS_COLUMNS;
    public static final int ATLAS_ROWS = K.UI.BLOCK_ATLAS_ROWS;

    private final byte id;
    private final String name;
    private final boolean isTillable;
    private final boolean isTransparent;
    private final MaterialID[] drops;
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

    private final SoundGroup soundGroup;

    private final float destroyTime;

    BlockData(byte id, String name, int value, int tileX, int tileY,
              boolean isTillable, SoundGroup soundGroup, float destroyTime, boolean isTransparent, MaterialID[] drops) {
        this(id, name, isTillable, isTransparent, drops, value, tileX, tileY, tileX, tileY,
                tileX, tileY, soundGroup, destroyTime);
    }

    BlockData(byte id, String name, int value, int tileX, int tileY,
              SoundGroup soundGroup, float destroyTime, boolean isTransparent, MaterialID[] drops) {
        this(id, name, false, isTransparent, drops, value, tileX, tileY, tileX,
                tileY, tileX, tileY, soundGroup, destroyTime);
    }

    BlockData(byte id, String name, boolean isTillable, int value, int
            tileX, int tileY, SoundGroup soundGroup, float destroyTime, boolean isTransparent, MaterialID[] drops) {
        this(id, name, isTillable, isTransparent, drops, value, tileX, tileY, tileX,
                tileY, tileX, tileY, soundGroup, destroyTime);
    }

    BlockData(byte id, String name, int value, boolean isTillable, int topTileX,
              int topTileY, SoundGroup soundGroup, float destroyTime, boolean isTransparent, MaterialID[] drops) {
        this(id, name, isTillable, isTransparent, drops, value, topTileX, topTileY,
                0, 0, 0, 0, soundGroup, destroyTime);
    }

    BlockData(byte id, String name, boolean isTillable, boolean isTransparent, MaterialID[] drops, int value, int topTileX,
              int topTileY, int bottomTileX, int bottomTileY, int sideTileX,
              int sideTileY, SoundGroup soundGroup, float destroyTime) {
        this.id = id;
        this.name = name;
        this.isTillable = isTillable;
        this.isTransparent = isTransparent;
        this.drops = drops;
        this.value = value;
        this.topTileX = topTileX;
        this.topTileY = topTileY;
        this.bottomTileX = bottomTileX;
        this.bottomTileY = bottomTileY;
        this.sideTileX = sideTileX;
        this.sideTileY = sideTileY;
        this.soundGroup = soundGroup;
        this.destroyTime = destroyTime;

        this.atlasScale = new Vector2f(1.0f / ATLAS_COLS, 1.0f / ATLAS_ROWS);
        this.topAtlasOffset = calculateAtlasOffset(topTileX, topTileY);
        this.bottomAtlasOffset = calculateAtlasOffset(bottomTileX, bottomTileY);
        this.sideAtlasOffset = calculateAtlasOffset(sideTileX, sideTileY);
    }

    public static BlockData fromId(byte id) {
        for (BlockData block : values()) {
            if (block.getId() == id) {
                return block;
            }
        }
        return null;
    }

    public static BlockData[] all() {
        return values();
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

    public boolean isTransparent() {
        return isTransparent;
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

    public SoundGroup getSoundGroup() {
        return soundGroup;
    }

    public float getDestroyTime() {
        return destroyTime;
    }

    public MaterialID[] getDrops() {
        return drops;
    }

    public boolean hasDrops() {
        return drops.length > 0;
    }

    public MaterialID getRandomDrop() {
        if (!hasDrops()) return null;
        if (Math.random() < 0.50f) {
            return drops[(int) (Math.random() * (drops.length - 1))];
        }
        return null;
    }
}