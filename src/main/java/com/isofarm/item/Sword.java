package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

public class Sword extends Tool {

    public Sword(Tier tier) {
        super((byte) 0, tier.getName() + ToolType.SWORD.getName(), 150, ToolType.SWORD,
                tier,tier.getDurability() + ToolType.SWORD.getBaseDurability());
    }

    public Sword() {
        this(Tier.WOOD);
    }
    
    @Override
    public Item copy() {
        return new Sword(getTier());
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
