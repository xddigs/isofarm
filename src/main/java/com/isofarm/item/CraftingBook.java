package com.isofarm.item;

import com.isofarm.craft.Recipe;
import com.isofarm.craft.RecipeRegistry;
import com.isofarm.data.Inventory;
import com.isofarm.entity.Player;
import com.isofarm.service.CraftingService;
import com.isofarm.wrld.GameMaster;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides crafting book behavior.
 */
public class CraftingBook extends Book implements Undroppable {
    private static final int LINES_PER_PAGE = 16;

    /**
     * Creates a new {@code CraftingBook} instance.
     */
    public CraftingBook() {
        super();
        if (!hasContent()) {
            return;
        }

        reload();
    }

    /**
     * Performs the reload operation.
     */
    @Override
    public void reload() {
        clearPages();
        List<Recipe> recipes = RecipeRegistry.reg.getRecipes();
        if (recipes.isEmpty()) return;

        Page page = new Page();
        addPage(page);
        int lineCount = 0;
        for (Recipe recipe : recipes) {
            if (recipe.result() == null) continue;
            if (lineCount >= LINES_PER_PAGE) {
                page = new Page();
                addPage(page);
                lineCount = 0;
            }

            String ingredients = recipe.ingredients()
                    .stream()
                    .map(ingredient -> ingredient.craftable()
                            .getDisplayName() + " x " + ingredient.amount())
                    .collect(Collectors.joining("\n"));
            String tooltip = recipe.result().getDisplayName()
                    + (ingredients.isEmpty() ? "" : "\n" + ingredients);

            page.addItem(recipe.result(),
                    line -> CraftingService.cs.craft(recipe), tooltip);
            lineCount++;
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
        Inventory inventory = Player.plyr.getInventory();
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
