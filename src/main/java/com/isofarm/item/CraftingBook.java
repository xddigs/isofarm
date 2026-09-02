package com.isofarm.item;

import com.isofarm.craft.Ingredient;
import com.isofarm.craft.Recipe;
import com.isofarm.craft.RecipeRegistry;
import com.isofarm.data.Inventory;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.service.CraftingService;
import com.isofarm.wrld.GameMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;

public class CraftingBook extends Book implements Undroppable {
    private static final int LINES_PER_PAGE = 16;

    public CraftingBook(Player player) {
        super();
        if (!hasContent() || player == null) {
            return;
        }

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
        int currentPageIndex = 0;

        for (Recipe recipe : recipes) {
            List<String> lines = recipe.toBookLines();
            if (lines.isEmpty()) continue;

            if (lineCount + lines.size() > LINES_PER_PAGE) {
                page = new Page();
                addPage(page);
                currentPageIndex++;
                lineCount = 0;
            }

            page.addLine(lines.getFirst(),
                    line -> CraftingService.cs.craft(player, recipe));
            lineCount++;

            for (int i = 1; i < lines.size(); i++) {
                String lineText = lines.get(i);
                Ingredient targetIngredient = null;
                for (Ingredient ingredient : recipe.ingredients()) {
                    if (ingredient != null && lineText.contains(ingredient.craftable().getName())) {
                        if (recipePageMap.containsKey(ingredient)) {
                            targetIngredient = ingredient;
                            break;
                        }
                    }
                }

                if (targetIngredient != null) {
                    int targetPage = recipePageMap.get(targetIngredient);
                    page.addLine(lineText, line -> navigateTo(targetPage));
                } else {
                    page.addLine(lineText);
                }

                lineCount++;
            }
        }
    }

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