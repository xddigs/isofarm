package com.isofarm.item;

import com.isofarm.craft.Recipe;
import com.isofarm.craft.RecipeRegistry;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.entity.Player;
import com.isofarm.service.CraftingService;

import java.util.List;

public class CraftingBook extends Book {
    private static final int LINES_PER_PAGE = 16;

    public CraftingBook(Player player) {
        super(Tier.NONE, MaterialID.CRAFTING_BOOK, true);

        if (!hasContent() || player == null) {
            return;
        }

        List<Recipe> recipes = RecipeRegistry.reg.getRecipes();
        Page page = new Page();
        addPage(page);

        int lineCount = 0;

        for (Recipe recipe : recipes) {
            List<String> lines = recipe.toBookLines();
            if (lines.isEmpty()) continue;

            if (lineCount + lines.size() > LINES_PER_PAGE) {
                page = new Page();
                addPage(page);
                lineCount = 0;
            }

            page.addLine(lines.getFirst(),
                    line -> CraftingService.cs.craft(player, recipe));
            lineCount++;

            for (int i = 1; i < lines.size(); i++) {
                page.addLine(lines.get(i));
                lineCount++;
            }
        }
    }
}