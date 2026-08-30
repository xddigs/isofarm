package com.isofarm.item;

import com.isofarm.craft.Recipe;
import com.isofarm.craft.RecipeRegistry;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;

import java.util.List;

public class CraftingBook extends Book {
    private final List<Recipe> recipes;

    public CraftingBook() {
        super(Tier.NONE, MaterialID.CRAFTING_BOOK, true);
        this.recipes = RecipeRegistry.getRecipes();
        if (!hasContent()) {
            return;
        }

        final int recipesPerPage = 10;
        Page page = new Page();
        addPage(page);
        for (int i = 0; i < recipes.size(); i++) {
            if (i > 0 && i % recipesPerPage == 0) {
                page = new Page();
                addPage(page);
            }
            page.addLine(recipes.get(i).toString());
        }
    }
}
