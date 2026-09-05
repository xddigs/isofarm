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
     * @param id the {@code byte} supplied as {@code id}
     * @param name the {@link String} supplied as {@code name}
     * @param baseDamage the {@code float} supplied as {@code baseDamage}
     * @param usableOn an array of {@link BlockData} values supplied as {@code usableOn}
     * @param baseDurability the {@code int} supplied as {@code baseDurability}
     * @param efficiency an array of {@code float} values supplied as {@code efficiency}
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
     * {@inheritDoc}
     * Returns the id.
     * @return {@code byte}; the id
     */
    @Override
    public byte getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     * Returns the name.
     * @return the {@link String} representing the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     * Returns the display name.
     * @return the {@link String} representing the display name
     */
    @Override
    public String getDisplayName() {
        return "item." + name().toLowerCase(Locale.ROOT);
    }

    /**
     * {@inheritDoc}
     * Returns the value.
     * @return {@code int}; the value
     */
    @Override
    public int getValue() {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Creates an independent copy that preserves the relevant state of this object.
     * @return the {@link Item} representing the copy result
     */
    @Override
    public Item copy() {
        return this;
    }

    /**
     * Returns the base damage.
     * @return {@code float}; the base damage
     */
    public float getBaseDamage() {
        return baseDamage;
    }

    /**
     * Returns the usable on.
     * @return an array of {@link BlockData} values; the usable on
     */
    public BlockData[] getUsableOn() {
        return usableOn;
    }

    /**
     * Returns the base durability.
     * @return {@code int}; the base durability
     */
    public int getBaseDurability() {
        return baseDurability;
    }

    /**
     * Returns the efficiency.
     * @return an array of {@code float} values; the efficiency
     */
    public float[] getEfficiency() {
        return efficiency;
    }
}
