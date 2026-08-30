package com.isofarm.service;

import com.isofarm.craft.Ingredient;
import com.isofarm.craft.Recipe;
import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.item.*;
import com.isofarm.utils.ToastFactory;

public class CraftingService {
    public static final CraftingService cs = new CraftingService();

    public boolean canCraft(Player player, Recipe recipe) {
        if (player == null || recipe == null) return false;
        Inventory inventory = player.getInventory();
        if (inventory == null) return false;

        for (Ingredient ingredient : recipe.ingredients()) {
            if (count(inventory, ingredient) < ingredient.amount()) {
                return false;
            }
        }
        return true;
    }

    public boolean craft(Player player, Recipe recipe) {
        if (player == null || recipe == null) return false;

        if (!canCraft(player, recipe)) {
            ToastFactory.error("You don't have enough ingredients");
            return false;
        }

        Inventory inventory = player.getInventory();
        consume(inventory, recipe);
        give(player, recipe);
        ToastFactory.success("Crafted " + recipe.result().getName());
        return true;
    }

    private void consume(InventorySlot[] inputSlots, Recipe recipe) {
        for (Ingredient ingredient : recipe.ingredients()) {
            int remainingToDeduct = ingredient.amount();
            for (InventorySlot slot : inputSlots) {
                if (slot == null || slot.isEmpty()) {
                    continue;
                }

                if (!matchesIngredient(ingredient, slot.getItem())) {
                    continue;
                }
                int amountInSlot = slot.getAmount();
                int toTake = Math.min(remainingToDeduct, amountInSlot);
                slot.setAmount(amountInSlot - toTake);

                if (slot.getAmount() <= 0) {
                    slot.clear();
                }

                remainingToDeduct -= toTake;
                if (remainingToDeduct <= 0) {
                    break;
                }
            }
        }
    }

    private void consume(Inventory inventory, Recipe recipe) {
        InventorySlot[] slots = inventory.getSlots().toArray(new InventorySlot[0]);
        consume(slots, recipe);
    }

    private int count(Inventory inventory, Ingredient ingredient) {
        if (inventory == null || ingredient == null) {

            return 0;
        }

        int amount = 0;
        for (InventorySlot slot : inventory.getSlots()) {
            if (slot == null || slot.isEmpty()) {
                continue;
            }
            if (matchesIngredient(ingredient, slot.getItem())) {
                amount += slot.getAmount();
            }
        }
        return amount;
    }

    public boolean matchesIngredient(Ingredient ingredient, Item item) {
        if (ingredient == null || item == null) {
            return false;
        }

        Craftable craftable = ingredient.craftable();

        if (craftable instanceof BlockData bd && item instanceof Block block) {
            return block.getType() == bd;
        }

        if (craftable instanceof MaterialID materialID && item instanceof Material material) {
            return material.getId() == materialID.getId();
        }

        if (craftable instanceof MiningComponent miningComponent
                && item instanceof MiningComponent itemMiningComponent) {
            return miningComponent.getTier() == itemMiningComponent.getTier()
                    && miningComponent.getId() == itemMiningComponent.getId();
        }

        if (craftable instanceof Item craftableItem) {
            return isSameType(craftableItem, item);
        }

        return false;
    }

    private void give(Player player, Recipe recipe) {
        Item result = recipe.result().copy();
        Inventory inventory = player.getInventory();
        int remaining = inventory.add(result, recipe.resultAmount());
        if (!player.hasSpace()) {
            player.addToBackpack(result, remaining);
        }

        if (!player.hasSpace()) {
            ToastFactory.error("Not enough inventory space");
        }
    }

    public boolean isSameType(Item a, Item b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.getClass() != b.getClass()) {
            return false;
        }

        return switch (a) {
            case Produce p1 when b instanceof Produce p2 -> p1.getType() == p2.getType();
            case Seed s1 when b instanceof Seed s2 -> s1.getType() == s2.getType();
            case Crop c1 when b instanceof Crop c2 -> c1.getCropType() == c2.getCropType();
            case Block b1 when b instanceof Block b2 -> b1.getType() == b2.getType();
            case Tool t1 when b instanceof Tool t2 -> t1.getId() == t2.getId() && t1.getType() == t2.getType();
            default -> a.getName().equals(b.getName());
        };
    }
}