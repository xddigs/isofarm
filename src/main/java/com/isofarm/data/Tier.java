package com.isofarm.data;

public enum Tier {
    LEATHER((byte) -1, "Leather", 1),
    WOOD((byte) 0, "Wooden", 64),
    ADVANCED((byte) 0, "Advanced", 0),
    COPPER((byte) 1, "Copper", 128),
    IRON((byte) 2, "Iron", 160),
    STEEL((byte) 3, "Steel", 192),
    GOLD((byte) 4, "Golden", 64),
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

    public int getDurability() {
        return durability;
    }
}
