package com.isofarm.craft;

import com.isofarm.data.*;
import com.isofarm.item.*;

import java.util.*;
import java.util.function.Function;

public class RecipeRegistry {
    public static final RecipeRegistry reg = new RecipeRegistry();
    private static final List<Recipe> recipes = new LinkedList<>();

    public List<Recipe> init() {
        recipes.clear();
        registerSmeltingRecipes();
        create(Tier.LEATHER).result(new Block(BlockData.OAK_WOOD), 4).with(BlockData.OAK_LOG, 1).add();
        create(Tier.LEATHER).result(new Material(Tier.NONE, MaterialID.STICK), 4).with(BlockData.OAK_WOOD, 1).add();
        create(Tier.LEATHER).result(new Book(false), 1).with(MaterialID.LEATHER, 3).with(MaterialID.PAPER, 2).add();
        create(Tier.LEATHER).result(new Backpack(), 1).with(MaterialID.LEATHER, 3).add();
        create(Tier.LEATHER).result(new Bucket(), 1).with(new MiningComponent(Tier.STEEL, MaterialID.INGOT), 3).add();
        registerToolSet(Tier.LEATHER, BlockData.OAK_WOOD);

        Map<Tier, Tier> metalProgression = Map.of(
                Tier.COPPER, Tier.COPPER,
                Tier.IRON, Tier.COPPER,
                Tier.STEEL, Tier.IRON,
                Tier.GOLD, Tier.STEEL,
                Tier.PLATINUM, Tier.GOLD,
                Tier.DIAMOND, Tier.PLATINUM);

        metalProgression.forEach((toolTier, requiredStationTier) -> {
            Craftable mainMaterial = new MiningComponent(toolTier, MaterialID.INGOT);
            registerToolSet(requiredStationTier, mainMaterial);
        });

        recipes.sort(Comparator.comparing(recipe -> recipe.result().getName(),
                String.CASE_INSENSITIVE_ORDER));
        return recipes;
    }

    private void registerToolSet(Tier stationTier, Craftable primaryMat) {
        if (stationTier.equals(Tier.NONE) || stationTier.equals(Tier.LEATHER)) return;
        registerTool(stationTier, primaryMat, 2, 1, Sword::new);
        registerTool(stationTier, primaryMat, 3, 2, Pickaxe::new);
        registerTool(stationTier, primaryMat, 3, 3, Axe::new);
        registerTool(stationTier, primaryMat, 2, 2, Hoe::new);
        registerTool(stationTier, primaryMat, 1, 2, Shovel::new);
    }

    private void registerTool(Tier stationTier, Craftable mat, int matAmount, int stickAmount,
                                     Function<Tier, Item> constructor) {
        create(stationTier)
                .result(constructor.apply(getTierFromMaterial(mat)), 1)
                .with(mat, matAmount)
                .with(MaterialID.STICK, stickAmount)
                .add();
    }

    private Tier getTierFromMaterial(Craftable mat) {
        return (mat instanceof MiningComponent mc) ? mc.getTier() : Tier.WOOD;
    }

    private void registerSmeltingRecipes() {
        Tier[] metalTiers = {Tier.COPPER, Tier.IRON, Tier.STEEL, Tier.GOLD, Tier.PLATINUM, Tier.DIAMOND};
        for (Tier tier : metalTiers) {
            create(tier)
                    .result(new MiningComponent(tier, MaterialID.INGOT), 1)
                    .with(new MiningComponent(tier, MaterialID.RAW_ORE), 1).add();
        }
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public RecipeBuilder create(Tier tier) {
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
            Recipe recipe = new Recipe(tier, result, amount, List.copyOf(ingredients));
            recipes.add(recipe);
            return recipe;
        }
    }
}