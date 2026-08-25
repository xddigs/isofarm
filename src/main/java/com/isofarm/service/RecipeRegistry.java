package com.isofarm.service;

import com.isofarm.data.*;
import com.isofarm.item.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RecipeRegistry implements Service<Recipe> {
    private static final List<Recipe> recipes = new LinkedList<>();

    public static List<Recipe> init() {
        create(Tier.LEATHER)
                .result(new Material(MaterialID.WOOD), 4)
                .with(BlockData.OAK_LOG, 1).add();

        create(Tier.LEATHER)
                .result(new Material(MaterialID.STICK), 4)
                .with(MaterialID.WOOD, 1).add();

        create(Tier.LEATHER)
                .result(new MiningComponent(Tier.COPPER, MaterialID.INGOT), 1)
                .with(new MiningComponent(Tier.COPPER, MaterialID.ORE), 1).add();

        create(Tier.LEATHER)
                .result(new Hoe(Tier.WOOD), 1)
                .with(MaterialID.WOOD, 1)
                .with(MaterialID.STICK, 3).add();

        create(Tier.LEATHER)
                .result(new Pickaxe(Tier.WOOD), 1)
                .with(MaterialID.WOOD, 2)
                .with(MaterialID.STICK, 3).add();

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

        public RecipeBuilder with(Craftable craftable, int count) {
            this.ingredients.add(new Ingredient(craftable, count));
            return this;
        }

        public Recipe add() {
            Recipe recipe = new Recipe(tier, result, amount, ingredients);
            recipes.add(recipe);
            return recipe;
        }
    }
}
