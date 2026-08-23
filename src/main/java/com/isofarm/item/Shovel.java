package com.isofarm.item;

import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

public class Shovel extends Tool {

    public Shovel(Tier tier) {
        super((byte) 4, tier.getName() + " Shovel", 50, ToolType.SHOVEL,
                tier,tier.getDurability() + ToolType.SHOVEL.getBaseDurability());
    }

    public Shovel() {
        this(Tier.WOOD);
    }

    @Override
    public Item copy() {
        return new Shovel(getTier());
    }
}
