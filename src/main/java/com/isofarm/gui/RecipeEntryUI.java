package com.isofarm.gui;

import com.isofarm.data.Recipe;
import com.isofarm.entity.Player;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.input.Mouse;
import com.isofarm.item.Item;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

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

    @Override
    public void update(float delta) {
        super.update(delta);

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        if (contains(mouseX, mouseY) && Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            if (onClick != null) {
                onClick.run();
            }
        }
    }

    @Override
    public void render() {
        renderBackground();
        renderRecipeContent();
        renderChildren();
    }

    private void renderBackground() {
        Vector4f bgColor = isSelected ? K.UI.RECIPE_SELECTED_COLOR : K.UI.RECIPE_BACKGROUND_COLOR;
        GUI.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), Settings.getScaledSlot(), bgColor);
    }

    private void renderRecipeContent() {
        if (recipe == null || recipe.result() == null) return;

        Item resultItem = recipe.result();
        SpriteSheet resultSheet = ResourceManager.getItemSpriteSheet(resultItem);

        float paddingLeft = Settings.getScaledPadding() / 2.0f;
        float iconSize = Settings.getScaledIcon();
        float iconY = getAbsoluteY() + (getHeight() - iconSize) / 2.0f;
        Vector4f tint = canPlayerCraft() ? K.UI.UI_ITEM_TINT : K.UI.UI_TEXT_DISABLED_COLOR;

        if (resultSheet != null) {
            GUI.drawSprite(resultSheet, ResourceManager.getItemIconColumn(resultItem),
                    getAbsoluteX() + paddingLeft, iconY, iconSize, iconSize, tint);
        }

        float textX = getAbsoluteX() + paddingLeft + iconSize + Settings.scale(6.0f);
        float fontHeight = GUI.getStringHeight(resultItem.getName(), GUI.getNormalFont());
        float textY = getAbsoluteY() + (getHeight() - fontHeight) / 2.0f;

        Vector4f textColor = canPlayerCraft() ? K.UI.UI_TEXT_COLOR : K.UI.UI_TEXT_DISABLED_COLOR;
        GUI.drawString(resultItem.getName(), textX, textY, GUI.getNormalFont(), textColor);
    }

    private boolean canPlayerCraft() {
        if (player == null || player.getInventory() == null || recipe == null) return false;
        return recipe.ingredients().stream().allMatch(ing ->
                player.getInventory().getAmount(ing) >= ing.amount());
    }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }
    public Recipe getRecipe() { return recipe; }
    public Runnable getOnClick() { return onClick; }
    public void setOnClick(Runnable onClick) { this.onClick = onClick; }
}