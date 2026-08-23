package com.isofarm.service;

import com.isofarm.data.MaterialID;
import com.isofarm.data.Recipe;
import com.isofarm.data.RecipeIngredient;
import com.isofarm.data.Tier;
import com.isofarm.item.Hoe;

import java.util.LinkedList;
import java.util.List;

public class RecipeRegistry implements Service<Recipe> {
    private static final List<Recipe> recipes = new LinkedList<>();

    public static void init() {
        register(new Recipe(
                "wooden_hoe",
                "Wooden Hoe",
                List.of(new RecipeIngredient(MaterialID.STICK, 2),
                        new RecipeIngredient(MaterialID.WOOD, 1)),
                        new Hoe(Tier.WOOD), 1));
    }

    public static void register(Recipe recipe) {
        recipes.add(recipe);
    }

    public static List<Recipe> getSortedRecipes() {
        return recipes.stream()
                .sorted((a, b) -> a.displayName()
                .compareToIgnoreCase(b.displayName()))
                .toList();
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }
}
