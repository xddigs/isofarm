package com.isofarm.item;

import com.isofarm.data.Enchantment;

/**
 * Defines the enchantable contract.
 */
@FunctionalInterface
public interface Enchantable {
    /**
     * Performs the enchanting operation.
     * @param enchantment the enchantment value
     * @return the enchanting result
     */
    boolean enchanting(Enchantment enchantment);
}
