package com.isofarm.data;

import com.isofarm.item.Craftable;
import com.isofarm.item.Item;

@DataClass
public record Ingredient(Craftable craftable, int amount) implements Item {

    @Override
    public byte getId() {
        return craftable.getId();
    }

    @Override
    public String getName() {
        return craftable.getName();
    }

    @Override
    public int getValue() {
        return craftable.getValue();
    }

    @Override
    public Item copy() {
        return new Ingredient(craftable, amount);
    }
}