package com.isofarm.item;

import com.isofarm.craft.Recipe;
import com.isofarm.data.*;

@DataClass
public class CraftingKit extends Tool {
    private Recipe selectedRecipe = null;

    public CraftingKit(ToolType type, Tier tier) {
        super((byte) 0, tier.getName() + type.getName(), 50, type, tier,
                tier.getDurability() + type.getBaseDurability());
    }

    public CraftingKit() {
        this(ToolType.CRAFTING_KIT, Tier.LEATHER);
    }

    @Override
    public Item copy() {
        return new CraftingKit(ToolType.CRAFTING_KIT, Tier.LEATHER);
    }

    public Recipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(Recipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
