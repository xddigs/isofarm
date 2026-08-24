package com.isofarm.service;

import com.isofarm.data.MaterialID;
import com.isofarm.data.Recipe;
import com.isofarm.data.Ingredient;
import com.isofarm.data.Tier;
import com.isofarm.item.Hoe;
import com.isofarm.item.Item;
import com.isofarm.item.Material;
import com.isofarm.item.Pickaxe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RecipeRegistry implements Service<Recipe> {
    private static final List<Recipe> recipes = new LinkedList<>();

    public static List<Recipe> init() {
        create(Tier.WOOD)
        .result(new Material(MaterialID.STICK), 4)
        .with(MaterialID.WOOD, 1);

        create(Tier.WOOD)
        .result(new Hoe(Tier.WOOD), 1)
        .with(MaterialID.WOOD, 1)
        .with(MaterialID.STICK, 3);

        create(Tier.WOOD)
        .result(new Pickaxe(Tier.WOOD), 1)
        .with(MaterialID.WOOD, 2)
        .with(MaterialID.STICK, 3);

        return recipes;
    }

    public static RecipeBuilder create(Tier tier) {
        return new RecipeBuilder(tier);
    }

    public static class RecipeBuilder {
        private final Tier tier;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private Item result;
        private int amount = 1;

        public RecipeBuilder(Tier tier) {
            this.tier = tier;
        }

        public RecipeBuilder result(Item result, int amount) {
            this.result = result;
            this.amount = amount;
            return this;
        }

        public RecipeBuilder with(MaterialID material, int count) {
            this.ingredients.add(new Ingredient(material, count));
            recipes.add(new Recipe(tier, result, amount, ingredients));
            return this;
        }
    }
}
