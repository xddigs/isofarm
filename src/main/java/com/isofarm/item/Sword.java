package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

/**
 * Provides sword behavior.
 */
public class Sword extends Tool {

    /**
     * Creates a new {@code Sword} instance.
     * @param tier the tier value
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
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Sword(getTier());
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
