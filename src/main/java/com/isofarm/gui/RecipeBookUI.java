package com.isofarm.gui;

import com.isofarm.data.Recipe;
import com.isofarm.entity.Player;
import com.isofarm.service.RecipeRegistry;
import com.isofarm.utils.Settings;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookUI extends UIElement {
    private final List<Recipe> recipes;
    private final List<RecipeEntryUI> entries;
    private Player player;
    private int selectedIndex = -1;
    private int scrollOffset;

    public RecipeBookUI(float x, float y, float width, float height, Player player) {
        super(x, y, width, height);
        this.player = player;
        this.recipes = new ArrayList<>();
        this.entries = new ArrayList<>();
        setFocusable(true);
        loadRecipes();
    }

    private void loadRecipes() {
        recipes.clear();
        recipes.addAll(RecipeRegistry.getSortedRecipes());
        rebuildEntries();
    }

    private void rebuildEntries() {
        for (RecipeEntryUI entry : entries) {
            removeChild(entry);
        }

        entries.clear();
        float slotSize = Settings.scale(32.0f);
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            RecipeEntryUI entry = new RecipeEntryUI(0, i * slotSize, getWidth(), slotSize, recipe);
            final int index = i;
            entry.setOnClick(() -> selectRecipe(index));
            entries.add(entry);
            addChild(entry);
        }
    }

    private void selectRecipe(int index) {
        if (index < 0 || index >= recipes.size()) {
            return;
        }
        selectedIndex = index;
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setSelected(i == selectedIndex);
        }
    }

    public Recipe getSelectedRecipe() {
        if (selectedIndex < 0 || selectedIndex >= recipes.size()) {
            return null;
        }

        return recipes.get(selectedIndex);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;

        for (RecipeEntryUI entry : entries) {
            entry.setPlayer(player);
        }
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void refresh() {
        loadRecipes();
    }

    @Override
    public void render() {
        renderChildren();
    }
}