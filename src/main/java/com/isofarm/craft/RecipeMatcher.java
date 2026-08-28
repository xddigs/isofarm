package com.isofarm.craft;

import com.isofarm.data.InventorySlot;
import com.isofarm.item.Craftable;
import com.isofarm.item.Item;

import java.util.*;

public class RecipeMatcher {

    public static Map<Craftable, Integer> summarizeSlots(InventorySlot[] slots) {
        Map<Craftable, Integer> summary = new HashMap<>();
        for (InventorySlot slot : slots) {
            if (slot != null && !slot.isEmpty()) {
                Craftable key = extract(slot.getItem());
                if (key != null) {
                    summary.put(key, summary.getOrDefault(key, 0) + slot.getAmount());
                }
            }
        }
        return summary;
    }

    public static Optional<Recipe> match(InventorySlot[] slots, List<Recipe> registeredRecipes) {
        Map<Craftable, Integer> input = summarizeSlots(slots);
        if (input.isEmpty()) return Optional.empty();

        return registeredRecipes.stream()
                .filter(recipe -> recipe.match(input))
                .findFirst();
    }

    public static List<Recipe> find(Map<Craftable, Integer> availableMaterials,
                                    List<Recipe> registeredRecipes) {
        return registeredRecipes.stream()
                .filter(recipe -> recipe.canCraftWith(availableMaterials))
                .sorted(Comparator.comparingInt((Recipe r) -> r.ingredients().size()).reversed())
                .toList();
    }

    private static Craftable extract(Item item) {
        if (item instanceof Craftable c) return c;
        return null;
    }
}