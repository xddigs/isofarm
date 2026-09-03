package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

/**
 * Provides shovel behavior.
 */
public class Shovel extends Tool {

    /**
     * Creates a new {@code Shovel} instance.
     * @param tier the tier value
     */
    public Shovel(Tier tier) {
        super((byte) 4, ToolType.SHOVEL.getName(), 50, ToolType.SHOVEL,
                tier, tier.getDurability() + ToolType.SHOVEL.getBaseDurability());
    }

    /**
     * Creates a new {@code Shovel} instance.
     */
    public Shovel() {
        this(Tier.WOODEN);
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Shovel(getTier());
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
