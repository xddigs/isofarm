package com.isofarm.data;

import com.isofarm.graphics.TextureAtlas;
import com.isofarm.item.Block;
import com.isofarm.item.MiningComponent;
import com.isofarm.utils.K;
import com.isofarm.utils.Local;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Enumerates the supported block data values.
 */
@DataClass
public enum BlockData {
    AIR((byte) 0, (byte) 0, (byte) 0, false, false, 0, null, SoundGroup.SILENT, 0f, true, new Object[]{}, Tier.NONE),
    DIRT((byte) 1, (byte) 1, (byte) 0, true, false, 100, "assets/textures/blocks/dirt.png", SoundGroup.SOIL, 0.9f, false, new Object[]{}, Tier.NONE),
    GRASS((byte) 2, (byte) 2, (byte) 0, true, false, 120, "assets/textures/blocks/grass_top.png", "assets/textures/blocks/grass_bottom.png","assets/textures/blocks/grass.png",SoundGroup.SOIL, 1.0f, false, new Seed[]{new Seed()}, Tier.NONE),
    STONE((byte) 3, (byte) 3, (byte) 0, false, false, 150, "assets/textures/blocks/stone.png", SoundGroup.HARD, 6.0f, false, new Object[]{}, Tier.NONE),
    TILLED_DIRT((byte) 4, (byte) 4, (byte) 0, true, false,  110, "assets/textures/blocks/dirt_tilled.png", "assets/textures/blocks/dirt.png", "assets/textures/blocks/dirt.png", SoundGroup.SOIL, 0.9f, false, new Object[]{}, Tier.NONE),
    VOIDSTONE((byte) 5, (byte) 5, (byte) 0, false, false, 999, "assets/textures/blocks/voidstone.png", SoundGroup.HARD, 999999.0f, false,  new Object[]{}, Tier.NONE),
    GLASS((byte) 6, (byte) 6, (byte) 0, false,false,  200, "assets/textures/blocks/glass.png", SoundGroup.GLASS, 1.2f, true, new Object[]{}, Tier.NONE),
    OAK_LOG((byte) 7, (byte) 7, (byte) 0, false, false, 100, "assets/textures/blocks/oak_log_top.png", "assets/textures/blocks/oak_log_bottom.png", "assets/textures/blocks/oak_log_side.png", SoundGroup.HARD, 2.2f, false,  new Object[]{}, Tier.WOODEN),
    OAK_WOOD((byte) 8, (byte) 8, (byte) 0, false, false, 100, "assets/textures/blocks/oak_plank.png", SoundGroup.HARD, 4.0f, false,  new Object[]{}, Tier.WOODEN),
    OAK_LEAVES((byte) 9, (byte) 9, (byte) 0, false, false, 100, "assets/textures/blocks/oak_leaves.png", SoundGroup.SOIL, 1.1f, false,  new Object[]{MaterialID.STICK, "OAK_BONSAI"}, Tier.NONE),
    SNOW((byte) 10, (byte) 10, (byte) 0, false, false, 120, "assets/textures/blocks/snow.png", SoundGroup.SNOW, 0.8f, false, new Object[]{}, Tier.NONE),

    COPPER_ORE((byte) 11, (byte) 1, (byte) 1, false, false, 150, "assets/textures/blocks/copper_ore.png", SoundGroup.HARD, 6.0f, false, new MiningComponent[]{new MiningComponent(Tier.COPPER, MaterialID.RAW_ORE)}, Tier.COPPER),
    IRON_ORE((byte) 12, (byte) 2, (byte) 1, false, false, 150, "assets/textures/blocks/iron_ore.png", SoundGroup.HARD, 8.0f, false, new MiningComponent[]{new MiningComponent(Tier.IRON, MaterialID.RAW_ORE)}, Tier.IRON),
    STEEL_ORE((byte) 13, (byte) 3, (byte) 1, false, false, 200, "assets/textures/blocks/steel_ore.png", SoundGroup.HARD, 10.0f, false,new MiningComponent[]{new MiningComponent(Tier.STEEL, MaterialID.RAW_ORE)}, Tier.STEEL),
    GOLD_ORE((byte) 14, (byte) 4, (byte) 1, false, false, 500, "assets/textures/blocks/gold_ore.png", SoundGroup.HARD, 12.0f, false, new MiningComponent[]{new MiningComponent(Tier.GOLDEN, MaterialID.RAW_ORE)}, Tier.GOLDEN),
    PLATINUM_ORE((byte) 15, (byte) 5, (byte) 1, false, false, 800, "assets/textures/blocks/platinum_ore.png", SoundGroup.HARD, 14.0f, false, new MiningComponent[]{new MiningComponent(Tier.PLATINUM, MaterialID.RAW_ORE)}, Tier.PLATINUM),
    DIAMOND_ORE((byte) 16, (byte) 6, (byte) 1, false, false, 1000, "assets/textures/blocks/diamond_ore.png", SoundGroup.HARD, 16.0f, false, new MiningComponent[]{new MiningComponent(Tier.DIAMOND, MaterialID.RAW_ORE)}, Tier.DIAMOND),
    SAND((byte) 18, (byte) 7, (byte) 1, false, false, 100, "assets/textures/blocks/sand.png", SoundGroup.SOIL, 0.7f, false, new Object[]{}, Tier.NONE),
    GRAVEL((byte) 17, (byte) 8, (byte) 1, false, false, 100, "assets/textures/blocks/gravel.png", SoundGroup.SOIL, 0.9f, false, new Object[]{}, Tier.NONE),
    FOSSIL((byte) 19, (byte) 9, (byte) 1, false, false, 100, "assets/textures/blocks/fossil.png", SoundGroup.HARD, 8.0f, false, new Object[]{}, Tier.NONE),
    BASALT((byte) 20,(byte) 10,(byte) 1, false,  false, 100, "assets/textures/blocks/basalt.png", SoundGroup.HARD, 6.0f, false, new Object[]{}, Tier.NONE),

    TALL_GRASS((byte) 21, (byte) 1, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 5, "assets/textures/blocks/tall_grass.png", SoundGroup.SOIL, 0.01f, true, new Object[]{new Seed(CropType.WHEAT)}, Tier.NONE),
    ROSE((byte) 22, (byte) 2, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 10, "assets/textures/blocks/rose.png", SoundGroup.SOIL, 0.01f, true, new Object[]{}, Tier.NONE),
    ROSEBUSH((byte) 23, (byte) 3, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 20, "assets/textures/blocks/rosebush.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    LILY((byte) 24, (byte) 4, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 10, "assets/textures/blocks/lily.png", SoundGroup.SOIL, 0.01f, true, new Object[]{}, Tier.NONE),
    GHOSTFLOWER((byte) 25, (byte) 5, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 15, "assets/textures/blocks/ghostflower.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    RED_MUSHROOM((byte) 26, (byte) 6, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 15, "assets/textures/blocks/red_mushroom.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    BRIGHT_FLOWER((byte) 27, (byte) 7, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 10, "assets/textures/blocks/bright_flower.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    BLUE_FLOWER((byte) 28, (byte) 8, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 10, "assets/textures/blocks/blue_flower.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    ROSES((byte) 29, (byte) 9, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 15, "assets/textures/blocks/roses.png", SoundGroup.SOIL, 0.01f, true,  new Object[]{}, Tier.NONE),
    TULIP((byte) 30, (byte) 10, (byte) (K.UI.ICON_BLOCK_ROWS - 1), false, true, 10, "assets/textures/blocks/tulip.png", SoundGroup.SOIL, 0.01f, true, new Object[]{}, Tier.NONE),

    OAK_BONSAI((byte) 31, (byte) 1, (byte) 2, false, true, 100, "assets/textures/blocks/oak_bonsai.png", SoundGroup.SOIL, 0.01f, true, new Object[]{}, Tier.NONE),
    WATER((byte) 127, (byte) -1, (byte) -1, false, false, 80, "assets/textures/blocks/water.png", SoundGroup.WATER, 0.0f, true, new Object[]{}, Tier.NONE);

    public static final BlockData[] ORES = {COPPER_ORE, IRON_ORE, STEEL_ORE, GOLD_ORE, PLATINUM_ORE, DIAMOND_ORE};
    private static final BlockData[] BY_ID = createById();
    private final byte id;
    private final byte col;
    private final byte row;
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

    /**
     * Creates a new {@code BlockData} instance.
     * @param id the id value
     * @param col the col value
     * @param row the row value
     * @param isTillable the is tillable value
     * @param isPlant the is plant value
     * @param value the value value
     * @param topPath the top path value
     * @param bottomPath the bottom path value
     * @param sidePath the side path value
     * @param soundGroup the sound group value
     * @param destroyTime the destroy time value
     * @param isTransparent the is transparent value
     * @param drops the drops value
     * @param tier the tier value
     */
    BlockData(byte id, byte col, byte row, boolean isTillable, boolean isPlant, int value,
              String topPath, String bottomPath, String sidePath, SoundGroup soundGroup,
              float destroyTime, boolean isTransparent, Object[] drops, Tier tier) {
        this.id = id;
        this.col = col;
        this.row = row;
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

    /**
     * Creates a new {@code BlockData} instance.
     * @param id the id value
     * @param col the col value
     * @param row the row value
     * @param isTillable the is tillable value
     * @param isPlant the is plant value
     * @param value the value value
     * @param texturePath the texture path value
     * @param soundGroup the sound group value
     * @param destroyTime the destroy time value
     * @param isTransparent the is transparent value
     * @param drops the drops value
     * @param tier the tier value
     */
    BlockData(byte id, byte col, byte row, boolean isTillable, boolean isPlant,
              int value, String texturePath, SoundGroup soundGroup, float destroyTime,
              boolean isTransparent, Object[] drops, Tier tier) {
        this(id, col, row, isTillable, isPlant, value, texturePath, texturePath, texturePath, soundGroup, destroyTime, isTransparent, drops, tier);
    }

    /**
     * Returns the all texture paths.
     * @return the all texture paths
     */
    public static List<String> getAllTexturePaths() {
        List<String> paths = new ArrayList<>();
        for (BlockData block : values()) {
            if (block.topPath != null && !paths.contains(block.topPath)) paths.add(block.topPath);
            if (block.bottomPath != null && !paths.contains(block.bottomPath)) paths.add(block.bottomPath);
            if (block.sidePath != null && !paths.contains(block.sidePath)) paths.add(block.sidePath);
        }
        return paths;
    }

    /**
     * Creates and returns the by id.
     * @return the created by id
     */
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

    /**
     * Performs the from id operation.
     * @param id the id value
     * @return the from id result
     */
    public static BlockData fromId(byte id) {
        int index = Byte.toUnsignedInt(id);
        if (index < 0 || index >= BY_ID.length) {
            return null;
        }
        return BY_ID[index];
    }

    /**
     * Performs the from id to operation.
     * @param id the id value
     * @return the from id to result
     */
    public static Block fromIdTo(byte id) {
        return new Block(fromId(id));
    }

    /**
     * Performs the from name operation.
     * @param name the name value
     * @return the from name result
     */
    public static BlockData fromName(String name) {
        for (BlockData block : all()) {
            if (block.name().equals(name)) return block;
        }
        return null;
    }

    /**
     * Performs the all plants operation.
     * @return the all plants result
     */
    public static BlockData[] allPlants() {
        List<BlockData> result = new ArrayList<>();
        for (BlockData block : values()) {
            if (block.isPlant) result.add(block);
        }
        return result.toArray(new BlockData[0]);
    }

    /**
     * Performs the all operation.
     * @return the all result
     */
    public static BlockData[] all() {
        return values();
    }

    /**
     * Returns the ores.
     * @return the ores
     */
    public static BlockData[] getOres() {
        return ORES;
    }

    /**
     * Returns the ore.
     * @param tier the tier value
     * @return the ore
     */
    public static BlockData getOre(Tier tier) {
        for (BlockData block : ORES) {
            if (block.getTier() == tier) return block;
        }
        return null;
    }

    /**
     * Returns the random ore.
     * @return the random ore
     */
    public static BlockData getRandomOre() {
        return ORES[(int) (Math.random() * ORES.length)];
    }

    /**
     * Initializes the regions.
     * @param atlas the atlas value
     */
    public void initRegions(TextureAtlas atlas) {
        if (atlas == null) return;
        if (topPath != null) this.topRegion = atlas.getRegion(topPath);
        if (bottomPath != null) this.bottomRegion = atlas.getRegion(bottomPath);
        if (sidePath != null) this.sideRegion = atlas.getRegion(sidePath);
    }

    /**
     * Returns the id.
     * @return the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Returns the col.
     * @return the col
     */
    public byte getCol() {
        return col;
    }

    /**
     * Returns the row.
     * @return the row
     */
    public byte getRow() {
        return row;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    public String getDisplayName() {
        return Local.lang.t("block." + name().toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the value.
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the tier.
     * @return the tier
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * Checks whether the plant condition is met.
     * @return {@code true} if plant; otherwise {@code false}
     */
    public boolean isPlant() {
        return isPlant;
    }

    /**
     * Checks whether the tillable condition is met.
     * @return {@code true} if tillable; otherwise {@code false}
     */
    public boolean isTillable() {
        return isTillable;
    }

    /**
     * Checks whether the transparent condition is met.
     * @return {@code true} if transparent; otherwise {@code false}
     */
    public boolean isTransparent() {
        return isTransparent;
    }

    /**
     * Checks whether the solid condition is met.
     * @return {@code true} if solid; otherwise {@code false}
     */
    public boolean isSolid() {
        return this != AIR && !isPlant && !isFluid();
    }

    /**
     * Checks whether the fluid condition is met.
     * @return {@code true} if fluid; otherwise {@code false}
     */
    public boolean isFluid() {
        return this == WATER;
    }

    /**
     * Returns the top path.
     * @return the top path
     */
    public String getTopPath() {
        return topPath;
    }

    /**
     * Returns the bottom path.
     * @return the bottom path
     */
    public String getBottomPath() {
        return bottomPath;
    }

    /**
     * Returns the side path.
     * @return the side path
     */
    public String getSidePath() {
        return sidePath;
    }

    /**
     * Returns the top region.
     * @return the top region
     */
    public TextureAtlas.TextureRegion getTopRegion() {
        return topRegion;
    }

    /**
     * Returns the bottom region.
     * @return the bottom region
     */
    public TextureAtlas.TextureRegion getBottomRegion() {
        return bottomRegion;
    }

    /**
     * Returns the side region.
     * @return the side region
     */
    public TextureAtlas.TextureRegion getSideRegion() {
        return sideRegion;
    }

    /**
     * Returns the sound group.
     * @return the sound group
     */
    public SoundGroup getSoundGroup() {
        return soundGroup;
    }

    /**
     * Returns the destroy time.
     * @return the destroy time
     */
    public float getDestroyTime() {
        return destroyTime;
    }

    /**
     * Returns the drops.
     * @return the drops
     */
    public Object[] getDrops() {
        return drops;
    }

    /**
     * Checks whether the drops condition is met.
     * @return {@code true} if drops; otherwise {@code false}
     */
    public boolean hasDrops() {
        return drops != null && drops.length > 0;
    }

    /**
     * Returns the random drop.
     * @return the random drop
     */
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