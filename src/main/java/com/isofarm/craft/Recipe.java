package com.isofarm.craft;

import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.item.*;

import java.util.ArrayList;
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

        return switch (a) {
            case MaterialID mid1 when b instanceof MaterialID mid2 -> mid1 == mid2;
            case MiningComponent mc1 when b instanceof MiningComponent mc2 -> mc1.getTier() == mc2.getTier()
                    && mc1.getId() == mc2.getId();
            case Block blk1 when b instanceof Block blk2 -> blk1.getType() == blk2.getType();
            case Material mat1 when b instanceof Material mat2 -> mat1.getId() == mat2.getId();
            default -> a.getId() == b.getId();
        };

    }

    public List<String> toBookLines() {
        List<String> lines = new ArrayList<>();
        lines.add("**" + result.getDisplayName() + " x " + resultAmount);
        for (Ingredient ingredient : ingredients) {
            lines.add(ingredient.craftable().getDisplayName() + " x " + ingredient.amount());
        }
        lines.add("-");
        return lines;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(result.getName())
                .append(" x ")
                .append(resultAmount)
                .append("]");

        for (Ingredient ingredient : ingredients) {
            sb.append(ingredient.craftable().getName())
                    .append(" x ")
                    .append(ingredient.amount())
                    .append(", ");
        }
        return sb.toString();
    }
}