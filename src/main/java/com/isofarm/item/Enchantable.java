package com.isofarm.item;

import com.isofarm.data.Enchantment;

/**
 * Defines the enchantable contract.
 */
@FunctionalInterface
public interface Enchantable {
    /**
     * Applies enchanting and updates the affected character or item state.
     * @param enchantment the {@link Enchantment} supplied as {@code enchantment}
     * @return {@code boolean}; the enchanting result
     */
    boolean enchanting(Enchantment enchantment);
}
