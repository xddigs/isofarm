package com.isofarm.data;

import com.isofarm.item.Item;

@DataClass
public record Stack(Item item, int amount) {}
