package com.isofarm.item;

import com.isofarm.craft.Recipe;
import com.isofarm.craft.RecipeRegistry;
import com.isofarm.data.Inventory;
import com.isofarm.entity.Player;
import com.isofarm.service.CraftingService;
import com.isofarm.wrld.GameMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides crafting book behavior.
 */
public class CraftingBook extends Book implements Undroppable {
    private static final int LINES_PER_PAGE = 16;

    /**
     * Creates a new {@code CraftingBook} instance.
     * @param player the player value
     */
    public CraftingBook(Player player) {
        super();
        if (!hasContent() || player == null) {
            return;
        }

        reload(player);
    }

    /**
     * Performs the reload operation.
     * @param player the player value
     */
    @Override
    public void reload(Player player) {
        clearPages();
        List<Recipe> recipes = RecipeRegistry.reg.getRecipes();
        if (recipes.isEmpty()) return;

        Map<Item, Integer> recipePageMap = new HashMap<>();
        int tempLineCount = 0;
        int tempPageIndex = 0;

        for (Recipe recipe : recipes) {
            List<String> lines = recipe.toBookLines();
            if (lines.isEmpty()) continue;
            if (tempLineCount + lines.size() > LINES_PER_PAGE) {
                tempPageIndex++;
                tempLineCount = 0;
            }

            if (recipe.result() != null) {
                recipePageMap.putIfAbsent(recipe.result(), tempPageIndex);
            }

            tempLineCount += lines.size();
        }

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

            String tooltip = recipe.ingredients()
                    .stream()
                    .map(ingredient -> ingredient.craftable()
                            .getDisplayName() + " x " + ingredient.amount())
                    .collect(Collectors.joining("\n"));

            page.addLine(lines.getFirst(),
                    line -> CraftingService.cs.craft(player, recipe),
                    tooltip); lineCount++;
        }
    }

    /**
     * Performs the use operation.
     * @param gameMaster the game master value
     * @param isCtrlHeld the is ctrl held value
     * @return the use result
     */
    @Override
    public boolean use(GameMaster gameMaster, boolean isCtrlHeld) {
        Inventory inventory = gameMaster.getPlayer().getInventory();
        if (inventory == null) return false;
        if (isCtrlHeld) {
            if (!inventory.hasBookEquipped()) {
                inventory.equipBook(this);
                gameMaster.getGameUIService().resetHotbarPosition();
            } else {
                inventory.unequipBook();
            }
        } else {
            super.use(gameMaster, isCtrlHeld);
        }
        return true;
    }
}