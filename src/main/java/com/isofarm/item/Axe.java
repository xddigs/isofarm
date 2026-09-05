package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

/**
 * Encapsulates the state and operations required by axe within the game runtime.
 */
public class Axe extends Tool {

    /**
     * Creates a new {@code Axe} instance.
     * @param tier the {@link Tier} supplied as {@code tier}
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
     * {@inheritDoc}
     * Creates an independent copy that preserves the relevant state of this object.
     * @return the {@link Item} representing the copy result
     */
    @Override
    public Item copy() {
        return new Axe(getTier());
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
