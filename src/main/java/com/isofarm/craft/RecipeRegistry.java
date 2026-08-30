package com.isofarm.craft;

import com.isofarm.data.*;
import com.isofarm.item.*;

import java.util.*;
import java.util.function.Function;

public class RecipeRegistry {
    private static final List<Recipe> recipes = new LinkedList<>();

    public static List<Recipe> init() {
        recipes.clear();
        registerSmeltingRecipes();

        create(Tier.LEATHER).result(new Block(BlockData.OAK_WOOD), 4).with(BlockData.OAK_LOG, 1).add();
        create(Tier.LEATHER).result(new Material(Tier.NONE, MaterialID.STICK), 4).with(BlockData.OAK_WOOD, 1).add();
        create(Tier.LEATHER).result(new Material(Tier.NONE, MaterialID.BOOK), 1).with(MaterialID.LEATHER, 3).with(MaterialID.PAPER, 2).add();
        create(Tier.LEATHER).result(new Bucket(BlockData.AIR, Tier.WOOD), 1).with(BlockData.OAK_WOOD, 3).add();
        create(Tier.LEATHER).result(new CraftingBook(), 1).with(MaterialID.LEATHER, 2).add();

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

            create(requiredStationTier)
                    .result(new Backpack(ToolType.BACKPACK, toolTier), 1)
                    .with(mainMaterial, 3)
                    .with(MaterialID.LEATHER, 3).add();

            create(requiredStationTier)
                    .result(new CraftingKit(ToolType.CRAFTING_KIT, toolTier), 1)
                    .with(mainMaterial, 3)
                    .with(MaterialID.LEATHER, 2).add();

            create(toolTier)
                    .result(new Bucket(BlockData.AIR, toolTier), 1)
                    .with(mainMaterial, 3)
                    .with(MaterialID.STICK, 2).add();
        });

        recipes.sort(Comparator.comparing(recipe -> recipe.result().getName(),
                String.CASE_INSENSITIVE_ORDER));
        return recipes;
    }

    private static void registerToolSet(Tier stationTier, Craftable primaryMat) {
        registerTool(stationTier, primaryMat, 2, 2, Hoe::new);
        registerTool(stationTier, primaryMat, 3, 2, Pickaxe::new);
        registerTool(stationTier, primaryMat, 1, 2, Shovel::new);
        registerTool(stationTier, primaryMat, 3, 3, Axe::new);
        registerTool(stationTier, primaryMat, 2, 1, Sword::new);
    }

    private static void registerTool(Tier stationTier, Craftable mat, int matAmount, int stickAmount,
                                     Function<Tier, Item> constructor) {
        create(stationTier)
                .result(constructor.apply(getTierFromMaterial(mat)), 1)
                .with(mat, matAmount)
                .with(MaterialID.STICK, stickAmount)
                .add();
    }

    private static Tier getTierFromMaterial(Craftable mat) {
        return (mat instanceof MiningComponent mc) ? mc.getTier() : Tier.WOOD;
    }

    private static void registerSmeltingRecipes() {
        Tier[] metalTiers = {Tier.COPPER, Tier.IRON, Tier.STEEL, Tier.GOLD, Tier.PLATINUM, Tier.DIAMOND};
        for (Tier tier : metalTiers) {
            create(tier)
                    .result(new MiningComponent(tier, MaterialID.INGOT), 1)
                    .with(new MiningComponent(tier, MaterialID.RAW_ORE), 1).add();
        }
    }

    public static List<Recipe> getRecipes() {
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
            Recipe recipe = new Recipe(tier, result, amount, List.copyOf(ingredients));
            recipes.add(recipe);
            return recipe;
        }
    }
}