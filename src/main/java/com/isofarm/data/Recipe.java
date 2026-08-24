package com.isofarm.data;

import com.isofarm.item.Item;

import java.util.List;

public record Recipe(Tier tier, Item result, int resultAmount, List<Ingredient> ingredients) {

    public static Recipe of(Tier tier, Item result, int resultAmount, Ingredient... ingredients) {
        return new Recipe(tier, result, resultAmount, List.of(ingredients));
    }
}