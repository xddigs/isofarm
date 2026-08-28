package com.isofarm.craft;

import com.isofarm.data.BlockData;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.item.*;

import java.util.List;
import java.util.Map;

public record Recipe(Tier tier, Item result, int resultAmount, List<Ingredient> ingredients) {

    public static Recipe of(Tier tier, Item result, int resultAmount, Ingredient... ingredients) {
        return new Recipe(tier, result, resultAmount, List.of(ingredients));
    }

    public boolean match(Map<Craftable, Integer> inputIngredients) {
        if (inputIngredients.size() != ingredients.size()) {
            return false;
        }

        for (Ingredient req : ingredients) {
            Craftable reqKey = req.craftable();
            int requiredAmount = req.amount();

            boolean found = false;
            for (Map.Entry<Craftable, Integer> entry : inputIngredients.entrySet()) {
                if (isSameCraftable(reqKey, entry.getKey())) {
                    if (entry.getValue() != requiredAmount) return false;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public boolean canCraftWith(Map<Craftable, Integer> availableMaterials) {
        for (Ingredient req : ingredients) {
            Craftable reqKey = req.craftable();
            int requiredAmount = req.amount();

            int available = 0;
            for (Map.Entry<Craftable, Integer> entry : availableMaterials.entrySet()) {
                if (isSameCraftable(reqKey, entry.getKey())) {
                    available += entry.getValue();
                }
            }
            if (available < requiredAmount) return false;
        }
        return true;
    }

    public static boolean isSameCraftable(Craftable a, Craftable b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        if (a instanceof BlockData bd1 && b instanceof BlockData bd2) return bd1 == bd2;
        if (a instanceof MaterialID mid1 && b instanceof MaterialID mid2) return mid1 == mid2;
        if (a instanceof MiningComponent mc1 && b instanceof MiningComponent mc2) {
            return mc1.getTier() == mc2.getTier() && mc1.getId() == mc2.getId();
        }
        if (a instanceof Block blk1 && b instanceof Block blk2) return blk1.getType() == blk2.getType();
        if (a instanceof Material mat1 && b instanceof Material mat2) return mat1.getId() == mat2.getId();

        return a.getId() == b.getId();
    }

    @Override
    public String toString() {
        return "Recipe=" + result.getName() + " x " + resultAmount;
    }
}