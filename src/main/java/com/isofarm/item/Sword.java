package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

/**
 * Encapsulates the state and operations required by sword within the game runtime.
 */
public class Sword extends Tool {

    /**
     * Creates a new {@code Sword} instance.
     * @param tier the {@link Tier} supplied as {@code tier}
     */
    public Sword(Tier tier) {
        super((byte) 0, ToolType.SWORD.getName(), 150, ToolType.SWORD,
                tier, tier.getDurability() + ToolType.SWORD.getBaseDurability());
    }

    /**
     * Creates a new {@code Sword} instance.
     */
    public Sword() {
        this(Tier.WOODEN);
    }
    
    /**
     * {@inheritDoc}
     * Creates an independent copy that preserves the relevant state of this object.
     * @return the {@link Item} representing the copy result
     */
    @Override
    public Item copy() {
        return new Sword(getTier());
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
