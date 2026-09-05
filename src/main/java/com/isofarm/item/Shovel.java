package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

/**
 * Encapsulates the state and operations required by shovel within the game runtime.
 */
public class Shovel extends Tool {

    /**
     * Creates a new {@code Shovel} instance.
     * @param tier the {@link Tier} supplied as {@code tier}
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
     * {@inheritDoc}
     * Creates an independent copy that preserves the relevant state of this object.
     * @return the {@link Item} representing the copy result
     */
    @Override
    public Item copy() {
        return new Shovel(getTier());
    }

    /**
     * {@inheritDoc}
     * Applies enchanting and updates the affected character or item state.
     * @param enchantment the {@link Enchantment} supplied as {@code enchantment}
     * @return {@code boolean}; the enchanting result
     */
    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
