package com.isofarm.data;

import com.isofarm.item.Item;

/**
 * Immutable value object containing stack.
 */
@DataClass
public record Stack(Item item, int amount) {}
