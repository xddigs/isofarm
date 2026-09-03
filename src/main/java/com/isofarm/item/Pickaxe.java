package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

/**
 * Provides pickaxe behavior.
 */
public class Pickaxe extends Tool {

    /**
     * Creates a new {@code Pickaxe} instance.
     * @param tier the tier value
     */
    public Pickaxe(Tier tier) {
        super((byte) 1, ToolType.PICKAXE.getName(), 100, ToolType.PICKAXE,
                tier, tier.getDurability() + ToolType.PICKAXE.getBaseDurability());
    }

    /**
     * Creates a new {@code Pickaxe} instance.
     */
    public Pickaxe() {
        this(Tier.WOODEN);
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Pickaxe(getTier());
    }

    /**
     * Performs the enchanting operation.
     * @param enchantment the enchantment value
     * @return the enchanting result
     */
    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
