package com.isofarm.gui;

import com.isofarm.data.Recipe;
import com.isofarm.entity.Player;
import com.isofarm.utils.K;

public class RecipeEntryUI extends UIElement {
    private final Recipe recipe;
    private Player player;
    private boolean isSelected;
    private Runnable onClick;

    public RecipeEntryUI(float x, float y, float width, float height, Recipe recipe) {
        super(x, y, width, height);
        this.recipe = recipe;
        setFocusable(true);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public void render() {
        renderBackground();
        renderRecipe();
    }

    private void renderBackground() {
        if (isSelected) {
            GUI.drawRect(getAbsoluteX(), getAbsoluteY(),
                    getWidth(), getHeight(), K.UI.RECIPE_SELECTED_COLOR);
        } else {
            GUI.drawRect(getAbsoluteX(), getAbsoluteY(),
                    getWidth(), getHeight(), K.UI.RECIPE_BACKGROUND_COLOR);
        }
    }

    private void renderRecipe() {

    }

    public Runnable getOnClick() {
        return onClick;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }
}