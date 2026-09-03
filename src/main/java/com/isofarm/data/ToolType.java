package com.isofarm.data;

import com.isofarm.item.Item;

import java.util.Locale;

/**
 * Enumerates the supported tool type values.
 */
public enum ToolType implements Item {
    SWORD((byte) 0, "Sword", 7, BlockData.all(), 10, new float[]{}),
    PICKAXE((byte) 1, "Pickaxe", 6, new BlockData[]{BlockData.GRASS, BlockData.DIRT, BlockData.OAK_LEAVES, BlockData.STONE, BlockData.COPPER_ORE}, 7, new float[]{0.8f, 0.7f, 1.2f, 0.65f, 0.56f}),
    AXE((byte) 2, "Axe", 8, new BlockData[]{BlockData.OAK_LOG, BlockData.OAK_WOOD}, 10, new float[]{1.0f, 0.5f}),
    HOE((byte) 3, "Hoe", 5, new BlockData[]{BlockData.GRASS, BlockData.DIRT, BlockData.TILLED_DIRT}, 8, new float[]{0.5f, 0.25f, 0.75f}),
    SHOVEL((byte) 4, "Shovel", 4, new BlockData[]{BlockData.GRASS, BlockData.DIRT}, 5, new float[]{0.3f, 0.25f});

    private final byte id;
    private final String name;
    private final float baseDamage;
    private final BlockData[] usableOn;
    private final int baseDurability;
    private final float[] efficiency;

    /**
     * Creates a new {@code ToolType} instance.
     * @param id the id value
     * @param name the name value
     * @param baseDamage the base damage value
     * @param usableOn the usable on value
     * @param baseDurability the base durability value
     * @param efficiency the efficiency value
     */
    ToolType(byte id, String name, float baseDamage, BlockData[] usableOn, int baseDurability, float[] efficiency) {
        this.id = id;
        this.name = name;
        this.baseDamage = baseDamage;
        this.usableOn = usableOn;
        this.baseDurability = baseDurability;
        this.efficiency = efficiency;
    }

    /**
     * Returns the id.
     * @return the id
     */
    @Override
    public byte getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return "item." + name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the value.
     * @return the value
     */
    @Override
    public int getValue() {
        return 0;
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return this;
    }

    /**
     * Returns the base damage.
     * @return the base damage
     */
    public float getBaseDamage() {
        return baseDamage;
    }

    /**
     * Returns the usable on.
     * @return the usable on
     */
    public BlockData[] getUsableOn() {
        return usableOn;
    }

    /**
     * Returns the base durability.
     * @return the base durability
     */
    public int getBaseDurability() {
        return baseDurability;
    }

    /**
     * Returns the efficiency.
     * @return the efficiency
     */
    public float[] getEfficiency() {
        return efficiency;
    }
}
