package com.isofarm.gui;

import com.isofarm.data.Recipe;
import com.isofarm.entity.Player;
import com.isofarm.utils.Settings;

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
        float entryWidth = (slotSize + Settings.getScaledSpacing()) * 5f;

        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            float entryY = headerOffset + i * (slotSize + spacing);
            RecipeEntryUI entry = new RecipeEntryUI(0, entryY, entryWidth, slotSize, recipe);
            entry.setPlayer(player);
            entry.setOnClick(() -> player.craft(recipe));
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
    }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) {
        this.player = player;
        for (RecipeEntryUI entry : entries) {
            entry.setPlayer(player);
        }
    }
}