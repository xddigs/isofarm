package com.isofarm.data;

import com.isofarm.item.Item;

/**
 * Stores stack data.
 */
@DataClass
public record Stack(Item item, int amount) {}
