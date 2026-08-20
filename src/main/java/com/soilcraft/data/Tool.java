package com.soilcraft.data;

@DataClass
public abstract class Tool extends Item {
    private final ToolType type;
    private Tier tier;
    private int durability;

    public Tool(byte id, String name, int value,
                ToolType type, Tier tier, int durability) {
        super(id, name, value);
        this.type = type;
        this.tier = tier;
        this.durability = durability;
    }

    public ToolType getType() {
        return type;
    }

    public Tier getTier() {
        return tier;
    }

    public void upgrade(Tier tier) {
        Tier[] tiers = Tier.values();
        int index = tier.ordinal();
        if (index < tiers.length - 1) {
            this.tier = tiers[index + 1];
        }
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public void use() {
        if (canBeUsed()) {
            durability--;
        }
    }

    public void misuse() {
        if (canBeUsed()) {
            durability *= -2;
        }
    }

    public void repair() {
        durability += Math.clamp(durability,
                0, tier.getDurability());
    }

    public boolean canBeUsed() {
        return durability > 0;
    }
}
