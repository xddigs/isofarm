package com.isofarm.gui;

import com.isofarm.data.Recipe;
import com.isofarm.data.RecipeIngredient;
import com.isofarm.entity.Player;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookUI extends UIElement {
    private final List<Recipe> recipes = new ArrayList<>();
    private final List<RecipeEntryUI> entries = new ArrayList<>();
    private Player player;
    private int selectedIndex = -1;

    public RecipeBookUI(float x, float y, Player player) {
        super(x, y, Settings.getScaledSlot() * 4.0f, Settings.getScaledSlot());
        this.player = player;
        setFocusable(true);
    }

    public void setRecipes(List<Recipe> init) {
        recipes.clear();
        recipes.addAll(init);
        rebuildEntries();
    }

    public void rebuildEntries() {
        for (RecipeEntryUI entry : entries) {
            removeChild(entry);
        }
        entries.clear();

        float slotSize = Settings.getScaledSlot();
        float spacing = Settings.getScaledSpacing();

        float headerOffset = Settings.getScaledPadding() + Settings.getScaledHeader();
        float entryWidth = slotSize * 3.5f;

        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            float entryY = headerOffset + i * (slotSize + spacing);
            RecipeEntryUI entry = new RecipeEntryUI(0, entryY, entryWidth, slotSize, recipe);
            entry.setPlayer(player);
            final int index = i;
            entry.setOnClick(() -> selectedIndex = (selectedIndex == index) ? -1 : index);

            entries.add(entry);
            addChild(entry);
        }

        float totalHeight = headerOffset + (recipes.isEmpty() ? slotSize :
                recipes.size() * (slotSize + spacing) - spacing);
        setSize(entryWidth, totalHeight);
    }

    @Override
    public void render() {
        if (!isVisible()) return;
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setSelected(i == selectedIndex);
        }

        renderChildren();
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            renderIngredientDetails(recipes.get(selectedIndex));
        }
    }

    private void renderIngredientDetails(Recipe recipe) {
        if (recipe.ingredients() == null || recipe.ingredients().isEmpty()) return;

        float slotSize = Settings.getScaledSlot();
        float spacing = Settings.getScaledSpacing();
        float padding = Settings.scale(6.0f);

        RecipeEntryUI selectedEntry = entries.get(selectedIndex);
        float detailX = getAbsoluteX() + selectedEntry.getWidth() + spacing;
        float detailY = selectedEntry.getAbsoluteY();

        float maxTextWidth = 0;
        float fontHeight = GUI.getStringHeight("A", GUI.getNormalFont());
        float lineHeight = Math.max(slotSize * 0.6f, fontHeight + Settings.scale(4.0f));

        for (RecipeIngredient ing : recipe.ingredients()) {
            String line = ing.getName() + " x" + ing.amount();
            float w = GUI.getStringWidth(line, GUI.getNormalFont());
            if (w > maxTextWidth) maxTextWidth = w;
        }

        float detailWidth = (padding * 2.0f) + slotSize * 0.6f + Settings.scale(4.0f) + maxTextWidth;
        float detailHeight = (padding * 2.0f) + (recipe.ingredients().size() * lineHeight);
        GUI.drawRect(detailX, detailY, detailWidth, detailHeight, K.UI.RECIPE_BACKGROUND_COLOR);

        float currentY = detailY + padding;

        for (RecipeIngredient ing : recipe.ingredients()) {
            int required = ing.amount();
            int owned = (player != null && player.getInventory() != null)
                    ? player.getInventory().getAmount(ing) : 0;

            float iconSize = lineHeight - Settings.scale(2.0f);
            float currentX = detailX + padding;

            SpriteSheet sheet = ResourceManager.getItemSpriteSheet(ing);
            if (sheet != null) {
                GUI.drawSprite(sheet, ResourceManager.getItemIconColumn(ing),
                        currentX, currentY, iconSize, iconSize, K.UI.UI_ITEM_TINT);
            }

            currentX += iconSize + Settings.scale(4.0f);
            String ingText = ing.getName() + " x" + required;
            Vector4f textColor = (owned >= required) ? K.UI.UI_TEXT_COLOR : K.UI.UI_TEXT_DISABLED_COLOR;
            float textY = currentY + (iconSize - fontHeight) / 2.0f;
            GUI.drawString(ingText, currentX, textY, GUI.getNormalFont(), textColor);
            currentY += lineHeight;
        }
    }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) {
        this.player = player;
        for (RecipeEntryUI entry : entries) {
            entry.setPlayer(player);
        }
    }
}