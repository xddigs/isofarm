package com.isofarm.data;

import com.isofarm.item.Item;

import java.util.List;

@DataClass
public record Recipe(String name, List<RecipeIngredient> ingredients,
                     Item result, int resultAmount) {}