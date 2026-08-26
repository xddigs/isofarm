package com.isofarm.item;

import com.isofarm.data.Enchantment;

@FunctionalInterface
public interface Enchantable {
    boolean enchanting(Enchantment enchantment);
}
