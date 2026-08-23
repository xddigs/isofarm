package com.isofarm.data;

import com.isofarm.item.Item;

import java.util.List;

@DataClass
public record Recipe(String id, String displayName,
                     List<RecipeIngredient> ingredients,
                     Item result, int resultAmount) {
}