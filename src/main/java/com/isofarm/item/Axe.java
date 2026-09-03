package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

/**
 * Provides axe behavior.
 */
public class Axe extends Tool {

    /**
     * Creates a new {@code Axe} instance.
     * @param tier the tier value
     */
    public Axe(Tier tier) {
        super((byte) 2, ToolType.AXE.getName(), 150, ToolType.AXE,
                tier, tier.getDurability() + ToolType.AXE.getBaseDurability());
    }

    /**
     * Creates a new {@code Axe} instance.
     */
    public Axe() {
        this(Tier.WOODEN);
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Axe(getTier());
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
