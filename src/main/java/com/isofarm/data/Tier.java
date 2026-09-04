package com.isofarm.data;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Enumerates the supported tier values.
 */
public enum Tier {
    NONE((byte) -1, 0),
    LEATHER((byte) 0, 1),
    WOODEN((byte) 0, 64),
    COPPER((byte) 1, 128),
    IRON((byte) 2, 160),
    STEEL((byte) 3, 192),
    GOLDEN((byte) 4, 64),
    PLATINUM((byte) 5, 512),
    DIAMOND((byte) 6, 1024);

    private final byte id;
    private final int durability;

    /**
     * Creates a new {@code Tier} instance.
     * @param id the id value
     * @param durability the durability value
     */
    Tier(byte id, int durability) {
        this.id = id;
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
        return name().toLowerCase(Locale.ROOT);
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
     * Performs the to {@link String} operation.
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
