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
                .result(new Block(BlockData.OAK_WOOD), 4)
                .with(BlockData.OAK_LOG, 1).add();

        create(Tier.LEATHER)
                .result(new Material(Tier.NONE, MaterialID.STICK), 4)
                .with(BlockData.OAK_WOOD, 1).add();

        create(Tier.LEATHER)
                .result(new Hoe(Tier.WOOD), 1)
                .with(BlockData.OAK_WOOD, 2)
                .with(MaterialID.STICK, 3).add();

        create(Tier.LEATHER)
                .result(new Pickaxe(Tier.WOOD), 1)
                .with(BlockData.OAK_WOOD, 3)
                .with(MaterialID.STICK, 2).add();

        create(Tier.LEATHER)
                .result(new Shovel(Tier.WOOD), 1)
                .with(BlockData.OAK_WOOD, 2)
                .with(MaterialID.STICK, 2).add();

        create(Tier.LEATHER)
                .result(new Axe(Tier.WOOD), 1)
                .with(BlockData.OAK_WOOD, 3)
                .with(MaterialID.STICK, 2).add();

        create(Tier.LEATHER)
                .result(new Sword(Tier.WOOD), 1)
                .with(BlockData.OAK_WOOD, 2)
                .with(MaterialID.STICK, 1).add();

        create(Tier.LEATHER)
                .result(new Backpack(ToolType.BACKPACK, Tier.WOOD), 1)
                .with(BlockData.OAK_WOOD, 4)
                .with(MaterialID.STICK, 2).add();

        create(Tier.LEATHER)
                .result(new CraftingKit(ToolType.CRAFTING_KIT, Tier.WOOD), 1)
                .with(BlockData.OAK_WOOD, 4)
                .with(MaterialID.STICK, 4).add();


        create(Tier.COPPER)
                .result(new MiningComponent(Tier.COPPER, MaterialID.INGOT), 1)
                .with(new MiningComponent(Tier.COPPER, MaterialID.ORE), 1).add();

        create(Tier.COPPER)
                .result(new Hoe(Tier.COPPER), 1)
                .with(new MiningComponent(Tier.COPPER, MaterialID.INGOT), 2)
                .with(MaterialID.STICK, 3).add();

        create(Tier.COPPER)
                .result(new Pickaxe(Tier.COPPER), 1)
                .with(new MiningComponent(Tier.COPPER, MaterialID.INGOT), 3)
                .with(MaterialID.STICK, 2).add();

        create(Tier.COPPER)
                .result(new Shovel(Tier.COPPER), 1)
                .with(new MiningComponent(Tier.COPPER, MaterialID.INGOT), 1)
                .with(MaterialID.STICK, 2).add();

        create(Tier.COPPER)
                .result(new Axe(Tier.COPPER), 1)
                .with(new MiningComponent(Tier.COPPER, MaterialID.INGOT), 3)
                .with(MaterialID.STICK, 2).add();

        create(Tier.COPPER)
                .result(new Sword(Tier.COPPER), 1)
                .with(new MiningComponent(Tier.COPPER, MaterialID.INGOT), 2)
                .with(MaterialID.STICK, 1).add();

        create(Tier.COPPER)
                .result(new Backpack(ToolType.BACKPACK, Tier.COPPER), 1)
                .with(new Material(Tier.COPPER, MaterialID.INGOT), 3)
                .with(BlockData.OAK_WOOD, 2).add();

        create(Tier.LEATHER)
                .result(new CraftingKit(ToolType.CRAFTING_KIT, Tier.COPPER), 1)
                .with(new Material(Tier.COPPER, MaterialID.ORE), 3)
                .with(BlockData.OAK_WOOD, 2).add();


        create(Tier.COPPER)
                .result(new MiningComponent(Tier.IRON, MaterialID.INGOT), 1)
                .with(new MiningComponent(Tier.IRON, MaterialID.ORE), 1).add();

        create(Tier.IRON)
                .result(new Hoe(Tier.IRON), 1)
                .with(new MiningComponent(Tier.IRON, MaterialID.INGOT), 2)
                .with(MaterialID.STICK, 3).add();

        create(Tier.IRON)
                .result(new Pickaxe(Tier.IRON), 1)
                .with(new MiningComponent(Tier.IRON, MaterialID.INGOT), 3)
                .with(MaterialID.STICK, 2).add();

        create(Tier.IRON)
                .result(new Shovel(Tier.IRON), 1)
                .with(new MiningComponent(Tier.IRON, MaterialID.INGOT), 1)
                .with(MaterialID.STICK, 2).add();

        create(Tier.IRON)
                .result(new Axe(Tier.IRON), 1)
                .with(new MiningComponent(Tier.IRON, MaterialID.INGOT), 3)
                .with(MaterialID.STICK, 2).add();

        create(Tier.IRON)
                .result(new Sword(Tier.IRON), 1)
                .with(new MiningComponent(Tier.IRON, MaterialID.INGOT), 2)
                .with(MaterialID.STICK, 1).add();

        create(Tier.COPPER)
                .result(new Backpack(ToolType.BACKPACK, Tier.IRON), 1)
                .with(new Material(Tier.IRON, MaterialID.INGOT), 3)
                .with(BlockData.OAK_WOOD, 2).add();

        create(Tier.IRON)
                .result(new CraftingKit(ToolType.CRAFTING_KIT, Tier.IRON), 1)
                .with(new MiningComponent(Tier.IRON, MaterialID.INGOT), 3)
                .with(BlockData.OAK_WOOD, 2).add();

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
