package com.isofarm.data;

import com.isofarm.graphics.TextureAtlas;
import com.isofarm.item.Block;
import com.isofarm.item.MiningComponent;
import com.isofarm.utils.K;
import com.isofarm.utils.Local;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@DataClass
public enum BlockData {
    AIR((byte) 0, (byte) 0, (byte) 0, "Empty", false, false, 0, null, SoundGroup.SILENT, 0f, true, new Object[]{}, Tier.NONE),
    DIRT((byte) 1, (byte) 1, (byte) 0, "Dirt", true, false, 100, "assets/textures/blocks/dirt.png", SoundGroup.SOIL, 0.9f, false, new Object[]{}, Tier.NONE),
    GRASS((byte) 2, (byte) 2, (byte) 0, "Grass", true, false, 120, "assets/textures/blocks/grass_top.png", "assets/textures/blocks/grass_bottom.png","assets/textures/blocks/grass.png",SoundGroup.SOIL, 1.0f, false, new Seed[]{new Seed()}, Tier.NONE),
    STONE((byte) 3, (byte) 3, (byte) 0, "Stone", false, false, 150, "assets/textures/blocks/stone.png", SoundGroup.HARD, 6.0f, false, new Object[]{}, Tier.NONE),
    TILLED_DIRT((byte) 4, (byte) 4, (byte) 0, "Tilled Dirt", true, false,  110, "assets/textures/blocks/dirt_tilled.png", "assets/textures/blocks/dirt.png", "assets/textures/blocks/dirt.png", SoundGroup.SOIL, 0.9f, false, new Object[]{}, Tier.NONE),
    VOIDSTONE((byte) 5, (byte) 5, (byte) 0, "Voidstone", false, false, 999, "assets/textures/blocks/voidstone.png", SoundGroup.HARD, 999999.0f, false,  new Object[]{}, Tier.NONE),
    GLASS((byte) 6, (byte) 6, (byte) 0, "Glass", false,false,  200, "assets/textures/blocks/glass.png", SoundGroup.GLASS, 1.2f, true, new Object[]{}, Tier.NONE),
    OAK_LOG((byte) 7, (byte) 7, (byte) 0, "Oak Log", false, false, 100, "assets/textures/blocks/oak_log_top.png", "assets/textures/blocks/oak_log_bottom.png", "assets/textures/blocks/oak_log_side.png", SoundGroup.HARD, 2.2f, false,  new Object[]{}, Tier.WOODEN),
    OAK_WOOD((byte) 8, (byte) 8, (byte) 0, "Oak Wood", false, false, 100, "assets/textures/blocks/oak_plank.png", SoundGroup.HARD, 4.0f, false,  new Object[]{}, Tier.WOODEN),
    OAK_LEAVES((byte) 9, (byte) 9, (byte) 0, "Oak Leaves", false, false, 100, "assets/textures/blocks/oak_leaves.png", SoundGroup.SOIL, 1.1f, false,  new Object[]{MaterialID.STICK, "OAK_BONSAI"}, Tier.NONE),
    SNOW((byte) 10, (byte) 10, (byte) 0, "Snow", false, false, 120, "assets/textures/blocks/snow.png", SoundGroup.SNOW, 0.8f, false, new Object[]{}, Tier.NONE),

    COPPER_ORE((byte) 11, (byte) 1, (byte) 1, "Copper Ore Block", false, false, 150, "assets/textures/blocks/copper_ore.png", SoundGroup.HARD, 6.0f, false, new MiningComponent[]{new MiningComponent(Tier.COPPER, MaterialID.RAW_ORE)}, Tier.COPPER),
    IRON_ORE((byte) 12, (byte) 2, (byte) 1, "Iron Ore Block", false, false, 150, "assets/textures/blocks/iron_ore.png", SoundGroup.HARD, 8.0f, false, new MiningComponent[]{new MiningComponent(Tier.IRON, MaterialID.RAW_ORE)}, Tier.IRON),
    STEEL_ORE((byte) 13, (byte) 3, (byte) 1, "Steel Ore Block", false, false, 200, "assets/textures/blocks/steel_ore.png", SoundGroup.HARD, 10.0f, false,new MiningComponent[]{new MiningComponent(Tier.STEEL, MaterialID.RAW_ORE)}, Tier.STEEL),
    GOLD_ORE((byte) 14, (byte) 4, (byte) 1, "Golden Ore Block", false, false, 500, "assets/textures/blocks/gold_ore.png", SoundGroup.HARD, 12.0f, false, new MiningComponent[]{new MiningComponent(Tier.GOLDEN, MaterialID.RAW_ORE)}, Tier.GOLDEN),
    PLATINUM_ORE((byte) 15, (byte) 5, (byte) 1, "Platinum Ore Block", false, false, 800, "assets/textures/blocks/platinum_ore.png", SoundGroup.HARD, 14.0f, false, new MiningComponent[]{new MiningComponent(Tier.PLATINUM, MaterialID.RAW_ORE)}, Tier.PLATINUM),
    DIAMOND_ORE((byte) 16, (byte) 6, (byte) 1, "Diamond Ore Block", false, false, 1000, "assets/textures/blocks/diamond_ore.png", SoundGroup.HARD, 16.0f, false, new MiningComponent[]{new MiningComponent(Tier.DIAMOND, MaterialID.RAW_ORE)}, Tier.DIAMOND),
    GRAVEL((byte) 17, (byte) 7, (byte) 1, "Gravel", false, false, 100, "assets/textures/blocks/gravel.png", SoundGroup.SOIL, 0.9f, false, new Object[]{}, Tier.NONE),
    SAND((byte) 18, (byte) 8, (byte) 1, "Sand", false, false, 100, "assets/textures/blocks/sand.png", SoundGroup.SOIL,0.9f, false, new Object[]{}, Tier.NONE),
    FOSSIL((byte) 19, (byte) 9, (byte) 1, "Fossil", false, false, 100, "assets/textures/blocks/fossil.png", SoundGroup.HARD, 8.0f, false, new Object[]{}, Tier.NONE),
    BASALT((byte) 20,(byte) 10,(byte) 1, "Basalt", false,  false, 100, "assets/textures/blocks/basalt.png", SoundGroup.HARD, 6.0f, false, new Object[]{}, Tier.NONE),

    TALL_GRASS((byte) 21, (byte) 1, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Tall Grass", false, true, 5, "assets/textures/blocks/tall_grass.png", SoundGroup.SOIL, 0.01f, true, new Object[]{new Seed(CropType.WHEAT)}, Tier.NONE),
    ROSE((byte) 22, (byte) 2, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Rose", false, true, 10, "assets/textures/blocks/rose.png", SoundGroup.SOIL, 0.01f, true, new Object[]{}, Tier.NONE),
    ROSEBUSH((byte) 23, (byte) 3, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Rosebush", false, true, 20, "assets/textures/blocks/rosebush.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    LILY((byte) 24, (byte) 4, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Lily", false, true, 10, "assets/textures/blocks/lily.png", SoundGroup.SOIL, 0.01f, true, new Object[]{}, Tier.NONE),
    GHOSTFLOWER((byte) 25, (byte) 5, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Ghostflower", false, true, 15, "assets/textures/blocks/ghostflower.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    RED_MUSHROOM((byte) 26, (byte) 6, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Red Mushroom", false, true, 15, "assets/textures/blocks/red_mushroom.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    BRIGHT_FLOWER((byte) 27, (byte) 7, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Bright Flower", false, true, 10, "assets/textures/blocks/bright_flower.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    BLUE_FLOWER((byte) 28, (byte) 8, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Blue Flower", false, true, 10, "assets/textures/blocks/blue_flower.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    ROSES((byte) 29, (byte) 9, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Roses Cluster", false, true, 15, "assets/textures/blocks/roses.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    TULIP((byte) 30, (byte) 10, (byte) (K.UI.ICON_BLOCK_ROWS - 1), "Tulip", false, true, 10, "assets/textures/blocks/tulip.png", SoundGroup.SOIL, 0.01f, true, new Object[]{}, Tier.NONE),

    OAK_BONSAI((byte) 31, (byte) 1, (byte) 2, "Oak Bonsai", false, true, 100, "assets/textures/blocks/oak_bonsai.png", SoundGroup.SOIL, 0.01f, true, new Object[]{}, Tier.NONE),
    WATER((byte) 127, (byte) -1, (byte) -1, "Water", false, false, 80, "assets/textures/blocks/water.png", SoundGroup.WATER, 0.0f, true, new Object[]{}, Tier.NONE);

    public static final BlockData[] ORES = {COPPER_ORE, IRON_ORE, STEEL_ORE, GOLD_ORE, PLATINUM_ORE, DIAMOND_ORE};
    private static final BlockData[] BY_ID = createById();
    private final byte id;
    private final byte col;
    private final byte row;
    private final String name;
    private final boolean isTillable;
    private final boolean isPlant;
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

    BlockData(byte id, byte col, byte row, String name, boolean isTillable, boolean isPlant, int value,
              String topPath, String bottomPath, String sidePath, SoundGroup soundGroup,
              float destroyTime, boolean isTransparent, Object[] drops, Tier tier) {
        this.id = id;
        this.col = col;
        this.row = row;
        this.name = name;
        this.isTillable = isTillable;
        this.isPlant = isPlant;
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

    BlockData(byte id, byte col, byte row, String name, boolean isTillable, boolean isPlant,
              int value, String texturePath, SoundGroup soundGroup, float destroyTime,
              boolean isTransparent, Object[] drops, Tier tier) {
        this(id, col, row, name, isTillable, isPlant, value, texturePath, texturePath, texturePath, soundGroup, destroyTime, isTransparent, drops, tier);
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

    public static Block fromIdTo(byte id) {
        return new Block(fromId(id));
    }

    public static BlockData fromName(String name) {
        for (BlockData block : all()) {
            if (block.name.equals(name)) return block;
        }
        return null;
    }

    public static BlockData[] allPlants() {
        List<BlockData> result = new ArrayList<>();
        for (BlockData block : values()) {
            if (block.isPlant) result.add(block);
        }
        return result.toArray(new BlockData[0]);
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

    public byte getCol() {
        return col;
    }

    public byte getRow() {
        return row;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return Local.lang.t("block." + name().toLowerCase(Locale.ROOT));
    }

    public int getValue() {
        return value;
    }

    public Tier getTier() {
        return tier;
    }

    public boolean isPlant() {
        return isPlant;
    }

    public boolean isTillable() {
        return isTillable;
    }

    public boolean isTransparent() {
        return isTransparent;
    }

    public boolean isSolid() {
        return this != AIR && !isPlant && !isFluid();
    }

    public boolean isFluid() {
        return this == WATER;
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
            Object rawDrop = drops[index];

            if (rawDrop instanceof String blockKey) {
                return BlockData.valueOf(blockKey);
            }

            return rawDrop;
        }
        return null;
    }
}