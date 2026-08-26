package com.isofarm.data;

import com.isofarm.graphics.TextureAtlas;
import com.isofarm.item.Craftable;
import com.isofarm.item.Item;
import com.isofarm.item.MiningComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("all")
@DataClass
public enum BlockData implements Craftable {
    AIR((byte) 0, (byte) 0, "Empty", false, 0, null, null, null, SoundGroup.SILENT, 0f, true, new MaterialID[]{}, Tier.NONE),
    DIRT((byte) 1, (byte) 0, "Dirt", true, 100, "assets/textures/blocks/dirt.png", SoundGroup.SOIL, 0.9f, false, new MaterialID[]{}, Tier.NONE),
    GRASS((byte) 2, (byte) 0, "Grass", true, 120, "assets/textures/blocks/grass.png", SoundGroup.SOIL, 1.0f, false, new MaterialID[]{}, Tier.NONE),
    STONE((byte) 3, (byte) 0, "Stone", false, 150, "assets/textures/blocks/stone.png", SoundGroup.HARD, 6.0f, false, new MaterialID[]{}, Tier.NONE),
    TILLED_DIRT((byte) 4, (byte) 0, "Tilled Dirt", true, 110, "assets/textures/blocks/dirt_tilled.png", "assets/textures/blocks/dirt.png", "assets/textures/blocks/dirt.png", SoundGroup.SOIL, 0.9f, false, new MaterialID[]{}, Tier.NONE),
    VOIDSTONE((byte) 5, (byte) 0, "Voidstone", false, 999, "assets/textures/blocks/voidstone.png", SoundGroup.HARD, 999999.0f, false, new MaterialID[]{}, Tier.NONE),
    GLASS((byte) 6, (byte) 0, "Glass", false, 200, "assets/textures/blocks/glass.png", SoundGroup.GLASS, 1.2f, true, new MaterialID[]{}, Tier.NONE),
    OAK_LOG((byte) 7, (byte) 0, "Oak Log", false, 100, "assets/textures/blocks/oak_log_top.png", "assets/textures/blocks/oak_log_bottom.png", "assets/textures/blocks/oak_log_side.png", SoundGroup.HARD, 2.2f, false, new MaterialID[]{}, Tier.WOOD),
    OAK_WOOD((byte) 8, (byte) 0, "Oak Wood", false, 100, "assets/textures/blocks/oak_plank.png", SoundGroup.HARD, 4.0f, false, new MaterialID[]{}, Tier.WOOD),
    OAK_LEAVES((byte) 9, (byte) 0, "Oak Leaves", false, 100, "assets/textures/blocks/oak_leaves.png", SoundGroup.SOIL, 1.1f, false, new MaterialID[]{MaterialID.STICK}, Tier.NONE),
    SNOW((byte) 10, (byte) 0, "Snow", false, 120, "assets/textures/blocks/snow.png", SoundGroup.SNOW, 0.8f, false, new MaterialID[]{}, Tier.NONE),
    COPPER_ORE((byte) 11, (byte) 1, "Copper Ore Block", false, 150, "assets/textures/blocks/copper_ore.png", SoundGroup.HARD, 6.0f, false, new MiningComponent[]{new MiningComponent(Tier.COPPER, MaterialID.RAW_ORE)}, Tier.COPPER),
    IRON_ORE((byte) 12, (byte) 1, "Iron Ore Block", false, 150, "assets/textures/blocks/iron_ore.png", SoundGroup.HARD, 8.0f, false, new MiningComponent[]{new MiningComponent(Tier.IRON, MaterialID.RAW_ORE)}, Tier.IRON),
    STEEL_ORE((byte) 13, (byte) 1, "Steel Ore Block", false, 200, "assets/textures/blocks/steel_ore.png", SoundGroup.HARD, 10.0f, false, new MiningComponent[]{new MiningComponent(Tier.STEEL, MaterialID.RAW_ORE)}, Tier.STEEL),
    GOLD_ORE((byte) 14, (byte) 1, "Golden Ore Block", false, 500, "assets/textures/blocks/gold_ore.png", SoundGroup.HARD, 12.0f, false, new MiningComponent[]{new MiningComponent(Tier.GOLD, MaterialID.RAW_ORE)}, Tier.GOLD),
    PLATINUM_ORE((byte) 15, (byte) 1, "Platinum Ore Block", false, 800, "assets/textures/blocks/platinum_ore.png", SoundGroup.HARD, 14.0f, false, new MiningComponent[]{new MiningComponent(Tier.PLATINUM, MaterialID.RAW_ORE)}, Tier.PLATINUM),
    DIAMOND_ORE((byte) 16, (byte) 1, "Diamond Ore Block", false, 1000, "assets/textures/blocks/diamond_ore.png", SoundGroup.HARD, 16.0f, false, new MiningComponent[]{new MiningComponent(Tier.DIAMOND, MaterialID.RAW_ORE)}, Tier.DIAMOND),
    BLUE_FLOWER((byte) 19, (byte) 0, "Blue Flower", false, 10, "assets/textures/blocks/blue_flower.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    BRIGHT_FLOWER((byte) 20, (byte) 0, "Bright Flower", false, 10, "assets/textures/blocks/bright_flower.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    GHOSTFLOWER((byte) 21, (byte) 0, "Ghostflower", false, 15, "assets/textures/blocks/ghostflower.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    LILLY((byte) 22, (byte) 0, "Lilly", false, 10, "assets/textures/blocks/lilly.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    RED_MUSHROOM((byte) 23, (byte) 0, "Red Mushroom", false, 15, "assets/textures/blocks/red_mushroom.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    ROSE((byte) 24, (byte) 0, "Rose", false, 10, "assets/textures/blocks/rose.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    ROSE_SHORT((byte) 25, (byte) 0, "Short Rose", false, 10, "assets/textures/blocks/rose_short.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    ROSEBUSH((byte) 26, (byte) 0, "Rosebush", false, 20, "assets/textures/blocks/rosebush.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    ROSES((byte) 27, (byte) 0, "Roses Cluster", false, 15, "assets/textures/blocks/roses.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    SHORT_GRASS((byte) 28, (byte) 0, "Short Grass", false, 5, "assets/textures/blocks/short_grass.png", SoundGroup.SOIL, 0.01f, true, new Seed[]{new Seed(CropType.WHEAT)}, Tier.NONE),
    TALL_GRASS((byte) 29, (byte) 0, "Tall Grass", false, 5, "assets/textures/blocks/tall_grass.png", SoundGroup.SOIL, 0.01f, true, new Seed[]{new Seed(CropType.WHEAT)}, Tier.NONE),
    TULIP((byte) 30, (byte) 0, "Tulip", false, 10, "assets/textures/blocks/tulip.png", SoundGroup.SOIL, 0.0f, true, new MaterialID[]{}, Tier.NONE),
    WATER((byte) 127, (byte) -1, "Water", false, 80, "assets/textures/blocks/water.png", SoundGroup.WATER, 0.0f, true, new MaterialID[]{}, Tier.NONE);

    private static final BlockData[] BY_ID = createById();
    public static final BlockData[] ORES = {
            COPPER_ORE, IRON_ORE, STEEL_ORE, GOLD_ORE, PLATINUM_ORE, DIAMOND_ORE
    };
    public static final BlockData[] PLANTS = {
            BLUE_FLOWER, BRIGHT_FLOWER, GHOSTFLOWER, LILLY,
            RED_MUSHROOM, ROSE, ROSE_SHORT, ROSEBUSH, ROSES,
            SHORT_GRASS, TALL_GRASS, TULIP
    };

    private final byte id;
    private final byte row;
    private final String name;
    private final boolean isTillable;
    private final boolean isTransparent;
    private final Object[] drops;
    private final Tier tier;
    private final int value;

    private final String topPath;
    private final String bottomPath;
    private final String sidePath;
    private final SoundGroup soundGroup;
    private final float destroyTime;

    private TextureAtlas.TextureRegion topRegion;
    private TextureAtlas.TextureRegion bottomRegion;
    private TextureAtlas.TextureRegion sideRegion;

    BlockData(byte id, byte row, String name, boolean isTillable, int value, String topPath, String bottomPath, String sidePath,
              SoundGroup soundGroup, float destroyTime, boolean isTransparent, Object[] drops, Tier tier) {
        this.id = id;
        this.row = row;
        this.name = name;
        this.isTillable = isTillable;
        this.value = value;
        this.topPath = topPath;
        this.bottomPath = bottomPath;
        this.sidePath = sidePath;
        this.soundGroup = soundGroup;
        this.destroyTime = destroyTime;
        this.isTransparent = isTransparent;
        this.drops = drops;
        this.tier = tier;
    }

    BlockData(byte id, byte row, String name, boolean isTillable, int value, String texturePath,
              SoundGroup soundGroup, float destroyTime, boolean isTransparent, Object[] drops, Tier tier) {
        this(id, row, name, isTillable, value, texturePath, texturePath, texturePath, soundGroup, destroyTime, isTransparent, drops, tier);
    }

    public static List<String> getAllTexturePaths() {
        List<String> paths = new ArrayList<>();
        for (BlockData block : values()) {
            if (block.topPath != null && !paths.contains(block.topPath)) paths.add(block.topPath);
            if (block.bottomPath != null && !paths.contains(block.bottomPath)) paths.add(block.bottomPath);
            if (block.sidePath != null && !paths.contains(block.sidePath)) paths.add(block.sidePath);
        }
        return paths;
    }

    public boolean isSolid() {
        return this != AIR && !isPlant() && this != WATER;
    }

    private static BlockData[] createById() {
        byte maxId = 0;
        for (BlockData block : values()) {
            if (block.id > maxId) {
                maxId = block.id;
            }
        }

        BlockData[] result = new BlockData[maxId + 1];
        for (BlockData block : values()) {
            result[block.id] = block;
        }

        return result;
    }

    public static BlockData fromId(byte id) {
        int index = Byte.toUnsignedInt(id);
        if (index < 0 || index >= BY_ID.length) {
            return null;
        }
        return BY_ID[index];
    }

    public static BlockData[] all() {
        return values();
    }

    public static BlockData[] getOres() {
        return ORES;
    }

    public static BlockData getOre(Tier tier) {
        for (BlockData block : ORES) {
            if (block.getTier() == tier) return block;
        }
        return null;
    }

    public static BlockData getRandomOre() {
        return ORES[(int) (Math.random() * ORES.length)];
    }

    public void initRegions(TextureAtlas atlas) {
        if (atlas == null) return;
        if (topPath != null) this.topRegion = atlas.getRegion(topPath);
        if (bottomPath != null) this.bottomRegion = atlas.getRegion(bottomPath);
        if (sidePath != null) this.sideRegion = atlas.getRegion(sidePath);
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

    public boolean isPlant() {
        return Arrays.stream(PLANTS).anyMatch(block -> block == this);
    }

    public boolean isTillable() {
        return isTillable;
    }

    public boolean isTransparent() {
        return isTransparent;
    }

    public String getTopPath() {
        return topPath;
    }

    public String getBottomPath() {
        return bottomPath;
    }

    public String getSidePath() {
        return sidePath;
    }

    public TextureAtlas.TextureRegion getTopRegion() {
        return topRegion;
    }

    public TextureAtlas.TextureRegion getBottomRegion() {
        return bottomRegion;
    }

    public TextureAtlas.TextureRegion getSideRegion() {
        return sideRegion;
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
        return drops != null && drops.length > 0;
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