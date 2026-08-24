package com.isofarm.data;

import com.isofarm.item.Item;

@DataClass
public record Ingredient(MaterialID materialID, int amount) implements Item {

    @Override
    public byte getId() {
        return materialID.getId();
    }

    @Override
    public String getName() {
        return materialID.getName();
    }

    @Override
    public int getValue() {
        return materialID.getValue();
    }

    @Override
    public Item copy() {
        return new Ingredient(materialID, amount);
    }
}