package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

public class Axe extends Tool {

    public Axe(Tier tier) {
        super((byte) 5, tier.getName() + ToolType.AXE.getName(), 150, ToolType.AXE,
                tier,tier.getDurability() + ToolType.AXE.getBaseDurability());
    }

    public Axe() {
        this(Tier.WOOD);
    }

    @Override
    public Item copy() {
        return new Axe(getTier());
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
