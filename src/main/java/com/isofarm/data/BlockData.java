package com.isofarm.data;

import com.isofarm.item.Craftable;
import com.isofarm.item.Item;
import com.isofarm.item.MiningComponent;
import com.isofarm.utils.K;
import org.joml.Vector2f;

@SuppressWarnings("all")
@DataClass
public enum BlockData implements Craftable {
    AIR((byte) 0, (byte) 0, "Air", 0, 0, 0, SoundGroup.SILENT, 0f, true, new MaterialID[]{}, Tier.NONE),
    DIRT((byte) 1, (byte) 0, "Dirt", true, 100, 0, 0, SoundGroup.SOIL, 0.9f, false, new MaterialID[]{}, Tier.NONE),
    GRASS((byte) 2, (byte) 0, "Grass", true, 120, 1, 0, SoundGroup.SOIL, 1.0f, false, new Seed[]{new Seed(CropType.WHEAT)}, Tier.NONE),
    STONE((byte) 3, (byte) 0, "Stone", 150, 2, 0, SoundGroup.HARD, 6.0f, false, new MaterialID[]{}, Tier.NONE),
    TILLED_DIRT((byte) 4, (byte) 0, "Tilled Dirt", true, false, new MaterialID[]{}, Tier.NONE, 110, 3, 0, 0, 0, 0, 0, SoundGroup.SOIL, 0.9f),
    VOIDSTONE((byte) 5, (byte) 0, "Voidstone", 999, 4, 0, SoundGroup.HARD, 999999.0f, false, new MaterialID[]{}, Tier.NONE),
    GLASS((byte) 6, (byte) 0, "Glass", 200, 5, 0, SoundGroup.GLASS, 1.2f, true, new MaterialID[]{}, Tier.NONE),
    OAK_LOG((byte) 7, (byte) 0, "Oak Log", false, false, new MaterialID[]{}, Tier.WOOD, 100, 6, 2, 6, 1, 6, 1, SoundGroup.HARD, 1.2f),
    OAK_WOOD((byte) 8, (byte) 0, "Oak Wood", 100, 7, 0, SoundGroup.HARD, 4.0f, false, new MaterialID[]{}, Tier.WOOD),
    OAK_LEAVES((byte) 9, (byte) 0, "Oak Leaves", 100, 8, 0, SoundGroup.SOIL, 1.1f, false, new MaterialID[]{MaterialID.STICK}, Tier.NONE),
    SNOW((byte) 10, (byte) 0, "Snow", 120, 9, 0, SoundGroup.SNOW, 0.8f, false, new MaterialID[]{}, Tier.NONE),
    COPPER_ORE((byte) 11, (byte) 1, "Copper Ore Block", 150, 10, 0, SoundGroup.HARD, 6.0f, false, new MiningComponent[]{new MiningComponent(Tier.COPPER, MaterialID.ORE)}, Tier.COPPER),
    WATER((byte) -1, (byte) -1, "Water", 100, 0, 0, SoundGroup.SILENT, 0f, true, new MaterialID[]{}, Tier.NONE),
    CROP((byte) -1, (byte) -1, "Crop", -1, 0, 0, SoundGroup.SOIL, 1f, false, new MaterialID[]{}, Tier.NONE);

    public static final int ATLAS_COLS = K.UI.BLOCK_ATLAS_COLUMNS;
    public static final int ATLAS_ROWS = K.UI.BLOCK_ATLAS_ROWS;

    private final byte id;
    private final byte row;
    private final String name;
    private final boolean isTillable;
    private final boolean isTransparent;
    private final Object[] drops;
    private final Tier tier;
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

    BlockData(byte id, byte row, String name, int value, int tileX, int tileY,
              boolean isTillable, SoundGroup soundGroup, float destroyTime, boolean isTransparent, Object[] drops, Tier tier) {
        this(id, row, name, isTillable, isTransparent, drops, tier, value, tileX, tileY, tileX, tileY,
                tileX, tileY, soundGroup, destroyTime);
    }

    BlockData(byte id, byte row, String name, int value, int tileX, int tileY,
              SoundGroup soundGroup, float destroyTime, boolean isTransparent, Object[] drops, Tier tier) {
        this(id, row, name, false, isTransparent, drops, tier, value, tileX, tileY, tileX,
                tileY, tileX, tileY, soundGroup, destroyTime);
    }

    BlockData(byte id, byte row, String name, boolean isTillable, int value, int
            tileX, int tileY, SoundGroup soundGroup, float destroyTime, boolean isTransparent, Object[] drops, Tier tier) {
        this(id, row, name, isTillable, isTransparent, drops, tier, value, tileX, tileY, tileX,
                tileY, tileX, tileY, soundGroup, destroyTime);
    }

    BlockData(byte id, byte row, String name, int value, boolean isTillable, int topTileX,
              int topTileY, SoundGroup soundGroup, float destroyTime, boolean isTransparent, Object[] drops, Tier tier) {
        this(id, row, name, isTillable, isTransparent, drops, tier, value, topTileX, topTileY,
                0, 0, 0, 0, soundGroup, destroyTime);
    }

    BlockData(byte id, byte row, String name, boolean isTillable, boolean isTransparent, Object[] drops, Tier tier, int value, int topTileX,
              int topTileY, int bottomTileX, int bottomTileY, int sideTileX,
              int sideTileY, SoundGroup soundGroup, float destroyTime) {
        this.id = id;
        this.row = row;
        this.name = name;
        this.isTillable = isTillable;
        this.isTransparent = isTransparent;
        this.drops = drops;
        this.tier = tier;
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

    public byte getRow() {
        return row;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public Item copy() {
        return this;
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

    public Object[] getDrops() {
        return drops;
    }

    public boolean hasDrops() {
        return drops.length > 0;
    }

    public Object getRandomDrop() {
        if (!hasDrops()) return null;
        if (Math.random() < 0.50f) {
            int index = (int) (Math.random() * drops.length);
            return drops[index];
        }
        return null;
    }
}