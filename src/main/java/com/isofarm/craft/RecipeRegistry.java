package com.isofarm.craft;

import com.isofarm.data.*;
import com.isofarm.item.*;

import java.util.*;
import java.util.function.Function;

/**
 * Provides recipe registry behavior.
 */
public class RecipeRegistry {
    public static final RecipeRegistry reg = new RecipeRegistry();
    private static final List<Recipe> recipes = new LinkedList<>();

    /**
     * Initializes the component.
     * @return the init result
     */
    public List<Recipe> init() {
        recipes.clear();
        registerSmeltingRecipes();
        registerMaterialRecipes();

        create(Tier.LEATHER).result(new Block(BlockData.OAK_WOOD), 4).with(BlockData.fromIdTo(BlockData.OAK_LOG.getId()),1).add();
        create(Tier.LEATHER).result(new Material(Tier.NONE, MaterialID.STICK), 4).with(BlockData.fromIdTo(BlockData.OAK_WOOD.getId()), 1).add();
        create(Tier.LEATHER).result(new Book(false), 1).with(MaterialID.LEATHER, 3).with(MaterialID.PAPER, 2).add();
        create(Tier.LEATHER).result(new Backpack(), 1).with(MaterialID.LEATHER, 3).add();
        create(Tier.LEATHER).result(new Bucket(), 1).with(new MiningComponent(Tier.STEEL, MaterialID.INGOT), 3).add();
        registerToolSet(Tier.LEATHER, BlockData.fromIdTo(BlockData.OAK_WOOD.getId()));

        Map<Tier, Tier> metalProgression = Map.of(
                Tier.COPPER, Tier.COPPER,
                Tier.IRON, Tier.COPPER,
                Tier.STEEL, Tier.IRON,
                Tier.GOLDEN, Tier.STEEL,
                Tier.PLATINUM, Tier.GOLDEN,
                Tier.DIAMOND, Tier.PLATINUM);

        metalProgression.forEach((toolTier, requiredStationTier) -> {
            Craftable mainMaterial = new MiningComponent(toolTier, MaterialID.INGOT);
            registerToolSet(requiredStationTier, mainMaterial);
        });

        return recipes;
    }

    /**
     * Performs the register tool set operation.
     * @param stationTier the station tier value
     * @param primaryMat the primary mat value
     */
    private void registerToolSet(Tier stationTier, Craftable primaryMat) {
        if (stationTier.equals(Tier.NONE)) return;
        registerTool(stationTier, primaryMat, 2, 1, Sword::new);
        registerTool(stationTier, primaryMat, 3, 2, Pickaxe::new);
        registerTool(stationTier, primaryMat, 3, 3, Axe::new);
        registerTool(stationTier, primaryMat, 2, 2, Hoe::new);
        registerTool(stationTier, primaryMat, 1, 2, Shovel::new);
    }

    /**
     * Performs the register tool operation.
     * @param stationTier the station tier value
     * @param mat the mat value
     * @param matAmount the mat amount value
     * @param stickAmount the stick amount value
     * @param constructor the constructor value
     */
    private void registerTool(Tier stationTier, Craftable mat, int matAmount, int stickAmount,
                                     Function<Tier, Item> constructor) {
        create(stationTier)
                .result(constructor.apply(getTierFromMaterial(mat)), 1)
                .with(mat, matAmount)
                .with(MaterialID.STICK, stickAmount)
                .add();
    }

    /**
     * Returns the tier from material.
     * @param mat the mat value
     * @return the tier from material
     */
    private Tier getTierFromMaterial(Craftable mat) {
        return (mat instanceof MiningComponent mc) ? mc.getTier() : Tier.WOODEN;
    }

    /**
     * Performs the register smelting recipes operation.
     */
    private void registerSmeltingRecipes() {
        Tier[] metalTiers = {Tier.COPPER, Tier.IRON, Tier.STEEL, Tier.GOLDEN, Tier.PLATINUM, Tier.DIAMOND};
        for (Tier tier : metalTiers) {
            create(tier)
                    .result(new MiningComponent(tier, MaterialID.INGOT), 1)
                    .with(new MiningComponent(tier, MaterialID.RAW_ORE), 1).add();
        }
    }

    /**
     * Performs the register material recipes operation.
     */
    private void registerMaterialRecipes() {
        Tier tier = Tier.NONE;
        create(Tier.LEATHER)
                .with(new Material(tier, MaterialID.SUGAR_CANE), 1)
                .result(new Material(tier, MaterialID.PAPER), 2).add();
        create(Tier.LEATHER)
                .with(new Material(tier, MaterialID.SUGAR_CANE), 1)
                .result(new Material(tier, MaterialID.SUGAR), 4).add();
    }

    /**
     * Returns the recipes.
     * @return the recipes
     */
    public List<Recipe> getRecipes() {
        List<Recipe> sortedRecipes = new ArrayList<>(recipes);
        sortedRecipes.sort(Comparator.comparing(
                recipe -> recipe.result().getDisplayName(),
                String.CASE_INSENSITIVE_ORDER
        ));
        return sortedRecipes;
    }

    /**
     * Returns create.
     * @param tier the tier value
     * @return the create result
     */
    public RecipeBuilder create(Tier tier) {
        return new RecipeBuilder(tier);
    }

    /**
     * Provides recipe builder behavior.
     */
    public static class RecipeBuilder {
        private final Tier tier;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private Item result;
        private int amount = 1;

        /**
         * Creates a new {@code RecipeBuilder} instance.
         * @param tier the tier value
         */
        public RecipeBuilder(Tier tier) {
            this.tier = tier;
        }

        /**
         * Performs the result operation.
         * @param result the result value
         * @param amount the amount value
         * @return the result result
         */
        public RecipeBuilder result(Item result, int amount) {
            this.result = result;
            this.amount = amount;
            return this;
        }

        /**
         * Performs the with operation.
         * @param craftable the craftable value
         * @param count the count value
         * @return the with result
         */
        public RecipeBuilder with(Craftable craftable, int count) {
            this.ingredients.add(new Ingredient(craftable, count));
            return this;
        }

        /**
         * Adds add.
         * @return the add result
         */
        public Recipe add() {
            Recipe recipe = new Recipe(tier, result, amount, List.copyOf(ingredients));
            recipes.add(recipe);
            return recipe;
        }
    }
}