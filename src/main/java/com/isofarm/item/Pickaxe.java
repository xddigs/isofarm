package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

/**
 * Encapsulates the state and operations required by pickaxe within the game runtime.
 */
public class Pickaxe extends Tool {

    /**
     * Creates a new {@code Pickaxe} instance.
     * @param tier the {@link Tier} supplied as {@code tier}
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
     * {@inheritDoc}
     * Creates an independent copy that preserves the relevant state of this object.
     * @return the {@link Item} representing the copy result
     */
    @Override
    public Item copy() {
        return new Pickaxe(getTier());
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
