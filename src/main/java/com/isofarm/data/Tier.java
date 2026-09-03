package com.isofarm.data;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Enumerates the supported tier values.
 */
public enum Tier {
    NONE((byte) -1, "None", 0),
    LEATHER((byte) 0, "Leather", 1),

    WOODEN((byte) 0, "Wooden", 64),

    COPPER((byte) 1, "Copper", 128),
    IRON((byte) 2, "Iron", 160),
    STEEL((byte) 3, "Steel", 192),
    GOLDEN((byte) 4, "Golden", 64),
    PLATINUM((byte) 5, "Platinum", 512),
    DIAMOND((byte) 6, "Diamond", 1024);

    private final byte id;
    private final String name;
    private final int durability;

    /**
     * Creates a new {@code Tier} instance.
     * @param id the id value
     * @param name the name value
     * @param durability the durability value
     */
    Tier(byte id, String name, int durability) {
        this.id = id;
        this.name = name;
        this.durability = durability;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    public String getDisplayName() {
        if (this == NONE) return null;
        return "item.tier." + toStr(this);
    }

    /**
     * Performs the to str operation.
     * @param tier the tier value
     * @return the to str result
     */
    public static String toStr(Tier tier) {
        return tier.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the durability.
     * @return the durability
     */
    public int getDurability() {
        return durability;
    }

    /**
     * Checks whether the invalid tier condition is met.
     * @return {@code true} if invalid tier; otherwise {@code false}
     */
    public boolean isInvalidTier() {
        return this == NONE || this == LEATHER || this == WOODEN;
    }

    /**
     * Performs the for each operation.
     * @param consumer the consumer value
     */
    public static void forEach(Consumer<Tier> consumer) {
        for (Tier tier : values()) {
            consumer.accept(tier);
        }
    }
}
