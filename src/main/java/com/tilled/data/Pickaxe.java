package com.tilled.data;

public class Pickaxe extends Tool {
    private Tier tier;

    public Pickaxe(Tier tier) {
        super((byte) 3, tier.getName() + " Pickaxe", 100,
                tier.getDurability() + 16);
        this.tier = tier;
    }

    public Pickaxe() {
        this(Tier.COPPER);
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

    @Override
    public Item copy() {
        return new Pickaxe(tier);
    }
}
