package com.isofarm.craft;

import com.isofarm.data.DataClass;
import com.isofarm.item.Craftable;
import com.isofarm.item.Item;

@DataClass
public record Ingredient(Craftable craftable, int amount) {}