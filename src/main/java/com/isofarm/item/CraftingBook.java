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
        System.out.println(recipes.size());
        if (!hasContent()) {
            return;
        }

        final int linesPerPage = 8;
        Page page = new Page();
        addPage(page);
        int lineCount = 0;
        for (Recipe recipe : recipes) {
            List<String> lines = recipe.toBookLines();
            if (lineCount + lines.size() > linesPerPage) {
                page = new Page();
                addPage(page);
                lineCount = 0;
            }

            for (String line : lines) {
                page.addLine(line);
                lineCount++;
            }
        }
    }
}
