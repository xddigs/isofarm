package com.isofarm.craft;

import com.isofarm.data.DataClass;
import com.isofarm.item.Craftable;
import com.isofarm.item.Item;

/**
 * Immutable value object containing ingredient.
 */
@DataClass
public record Ingredient(Craftable craftable, int amount) {}