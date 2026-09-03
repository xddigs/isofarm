package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

public class Pickaxe extends Tool {

    public Pickaxe(Tier tier) {
        super((byte) 1, ToolType.PICKAXE.getName(), 100, ToolType.PICKAXE,
                tier, tier.getDurability() + ToolType.PICKAXE.getBaseDurability());
    }

    public Pickaxe() {
        this(Tier.WOODEN);
    }

    @Override
    public Item copy() {
        return new Pickaxe(getTier());
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
