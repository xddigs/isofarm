package com.isofarm.service;

import com.isofarm.data.*;
import com.isofarm.item.Hoe;
import com.isofarm.item.Pickaxe;

import java.util.LinkedList;
import java.util.List;

public class RecipeRegistry implements Service<Recipe> {
    private static final List<Recipe> recipes = new LinkedList<>();

    public static List<Recipe> init() {
        register(Recipe.of(Tier.WOOD, new Hoe(Tier.WOOD),1,
                new RecipeIngredient(MaterialID.WOOD, 1),
                new RecipeIngredient(MaterialID.STICK, 3)));
        register(Recipe.of(Tier.WOOD, new Pickaxe(Tier.WOOD), 1,
                new RecipeIngredient(MaterialID.WOOD, 2),
                new RecipeIngredient(MaterialID.STICK, 3)));
        return recipes;
    }

    public static void register(Recipe recipe) {
        recipes.add(recipe);
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }
}
