package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

public class Shovel extends Tool {

    public Shovel(Tier tier) {
        super((byte) 4, tier.getDisplayName() + ToolType.SHOVEL.getDisplayName(), 50, ToolType.SHOVEL,
                tier,tier.getDurability() + ToolType.SHOVEL.getBaseDurability());
    }

    public Shovel() {
        this(Tier.WOODEN);
    }

    @Override
    public Item copy() {
        return new Shovel(getTier());
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
