package com.tilled.gui;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import com.tilled.utils.K;
import com.tilled.utils.Settings;
import org.joml.Vector4f;

import java.lang.Character;

@SuppressWarnings("all")
public class InventorySlotUI extends UIElement {
    private InventorySlot slot;
    private SpriteSheet spriteSheet;
    private int spriteFrame;
    private UIFont countFont = GUI.getNormalFont();

    private boolean selected;
    private boolean hovered;

    private boolean selectedOutline = false;
    private Vector4f selectionOutlineColor = K.UI.UI_HOTBAR_SELECTED_COLOR;
    private float selectionOutlineThickness = Settings.getScaledThickness() * 2f;

    public InventorySlotUI(float x, float y, float width, float height) {
        super(x, y, width, height);
        setFocusable(true);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void render() {
        float x = getAbsoluteX();
        float y = getAbsoluteY();
        float width = getAbsoluteWidth();
        float height = getAbsoluteHeight();

        Vector4f background = selected ? K.UI.UI_SELECTED_COLOR : hovered ? K.UI.UI_HOVERED_COLOR : K.UI.UI_BACKGROUND_COLOR_SLOT;
        GUI.drawRect(x, y, width, height, new Vector4f(background.x, background.y, background.z, background.w * getWorldOpacity()));
        Vector4f border = K.UI.UI_SELECTED_BORDER_COLOR;
        Vector4f borderTint = new Vector4f(border.x, border.y, border.z, border.w * getWorldOpacity());

        GUI.drawRect(x, y, width, 1.0f, borderTint);
        GUI.drawRect(x, y + height - 1.0f, width, 1.0f, borderTint);
        GUI.drawRect(x, y, 1.0f, height, borderTint);
        GUI.drawRect(x + width - 1.0f, y, 1.0f, height, borderTint);

        if (!isEmpty() && spriteSheet != null) {
            renderItem();
        }

        renderChildren();
    }

    private void renderItem() {
        float iconSize = Math.min(
                Settings.getScaledIcon(),
                Math.min(getAbsoluteWidth(), getAbsoluteHeight()) - 4.0f);

        float x = getAbsoluteX() + (getAbsoluteWidth() - iconSize) * 0.5f;
        float y = getAbsoluteY() + (getAbsoluteHeight() - iconSize) * 0.5f;
        GUI.drawSprite(spriteSheet, spriteFrame, x, y, iconSize, iconSize,
                new Vector4f(K.UI.UI_ITEM_TINT.x, K.UI.UI_ITEM_TINT.y,
                        K.UI.UI_ITEM_TINT.z, K.UI.UI_ITEM_TINT.w * getWorldOpacity()));
        renderAmount();
    }

    private void renderAmount() {
        Item item = getItem();

        if (item == null || item.getAmount() <= 1) {
            return;
        }

        String amount = String.valueOf(item.getAmount());

        float textWidth = getTextWidth(amount);
        float textHeight = countFont.getSize();
        float x = getAbsoluteX() + getAbsoluteWidth() - textWidth - 4.0f;
        float y = getAbsoluteY() + getAbsoluteHeight() - textHeight - 2.0f;

        GUI.drawString(amount, x + 1.0f, y + textHeight, countFont,
                new Vector4f(0.0f, 0.0f, 0.0f, getWorldOpacity()));

        GUI.drawString(amount, x, y + textHeight, countFont,
                new Vector4f(K.UI.UI_TEXT_COLOR.x, K.UI.UI_TEXT_COLOR.y, K.UI.UI_TEXT_COLOR.z, K.UI.UI_TEXT_COLOR.w * getWorldOpacity()));
    }

    private float getTextWidth(String value) {
        float width = 0.0f;
        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            var glyph = countFont.getGlyph(codePoint);

            if (glyph != null) {
                width += glyph.xadvance();
            }

            i += Character.charCount(codePoint);
        }

        return width;
    }

    public InventorySlot getSlot() {
        return slot;
    }

    public void setSlot(InventorySlot slot) {
        this.slot = slot;
    }

    public Item getItem() {
        return slot != null ? slot.getItem() : null;
    }

    public boolean isEmpty() {
        return slot == null || slot.isEmpty();
    }

    public SpriteSheet getSpriteSheet() {
        return spriteSheet;
    }

    public void setSpriteSheet(SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    public int getSpriteFrame() {
        return spriteFrame;
    }

    public void setSpriteFrame(int spriteFrame) {
        this.spriteFrame = Math.max(0, spriteFrame);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public UIFont getCountFont() {
        return countFont;
    }

    public void setCountFont(UIFont countFont) {
        if (countFont != null) {
            this.countFont = countFont;
        }
    }
}