package com.soilcraft.data;

public class Pickaxe extends Tool {

    public Pickaxe(Tier tier) {
        super((byte) 3, tier.getName() + " Pickaxe", 100, ToolType.PICKAXE,
                tier,tier.getDurability() + ToolType.PICKAXE.getBaseDurability());
    }

    public Pickaxe() {
        this(Tier.WOOD);
    }

    @Override
    public Item copy() {
        return new Pickaxe(getTier());
    }
}
