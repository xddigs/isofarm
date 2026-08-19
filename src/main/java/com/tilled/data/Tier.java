package com.tilled.data;

public enum Tier {
    COPPER("Copper", 128),
    IRON("Iron", 160),
    STEEL("Steel", 192),
    GOLD("Gold", 64),
    PLATINUM("Platinum", 224),
    DIAMOND("Diamond", 1024);

    private final String name;
    private final int durability;

    Tier(String name, int durability) {
        this.name = name;
        this.durability = durability;
    }

    public String getName() {
        return name;
    }

    public int getDurability() {
        return durability;
    }
}
