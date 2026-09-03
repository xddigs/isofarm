package com.isofarm.data;

import java.util.Locale;
import java.util.function.Consumer;

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

    Tier(byte id, String name, int durability) {
        this.id = id;
        this.name = name;
        this.durability = durability;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return "item.tier." + name().toLowerCase(Locale.ROOT);
    }

    public int getDurability() {
        return durability;
    }

    public boolean isInvalidTier() {
        return this == NONE || this == LEATHER || this == WOODEN;
    }

    public static void forEach(Consumer<Tier> consumer) {
        for (Tier tier : values()) {
            consumer.accept(tier);
        }
    }
}
