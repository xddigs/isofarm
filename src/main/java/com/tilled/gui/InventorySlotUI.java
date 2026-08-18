package com.tilled.gui;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import org.joml.Vector4f;

import java.lang.Character;

@SuppressWarnings("all")
public class InventorySlotUI extends UIElement {
    private final Vector4f backgroundColor = new Vector4f(0.08f, 0.08f, 0.08f, 1.0f);
    private final Vector4f hoveredColor = new Vector4f(0.12f, 0.12f, 0.12f, 1.0f);
    private final Vector4f selectedColor = new Vector4f(0.16f, 0.16f, 0.16f, 1.0f);
    private final Vector4f borderColor = new Vector4f(0.25f, 0.25f, 0.25f, 1.0f);
    private final Vector4f selectedBorderColor = new Vector4f(0.4f, 0.6f, 1.0f, 1.0f);
    private final Vector4f itemTint = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private final Vector4f countColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private InventorySlot slot;
    private SpriteSheet spriteSheet;
    private int spriteFrame;
    private UIFont countFont = GUI.getNormalFont();

    private boolean selected;
    private boolean hovered;

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

        Vector4f background = selected ? selectedColor : hovered ? hoveredColor : backgroundColor;
        GUI.drawRect(x, y, width, height, new Vector4f(background.x, background.y, background.z, background.w * getWorldOpacity()));
        Vector4f border = selected ? selectedBorderColor : borderColor;
        GUI.drawRect(x, y, width, 1.0f, new Vector4f(border.x, border.y, border.z, border.w * getWorldOpacity()));
        GUI.drawRect(x, y + height - 1.0f, width, 1.0f, new Vector4f(border.x, border.y, border.z, border.w * getWorldOpacity()));
        GUI.drawRect(x, y, 1.0f, height, new Vector4f(border.x, border.y, border.z, border.w * getWorldOpacity()));
        GUI.drawRect(x + width - 1.0f, y, 1.0f, height, new Vector4f(border.x, border.y, border.z, border.w * getWorldOpacity()));

        if (!isEmpty() && spriteSheet != null) {
            renderItem();
        }

        renderChildren();
    }

    private void renderItem() {
        float padding = 2.0f;
        float iconSize = Math.min(getAbsoluteWidth() - padding * 2.0f, getAbsoluteHeight() - padding * 2.0f);
        float x = getAbsoluteX() + (getAbsoluteWidth() - iconSize) * 0.5f;
        float y = getAbsoluteY() + (getAbsoluteHeight() - iconSize) * 0.5f;
        GUI.drawSprite(spriteSheet, spriteFrame, x, y, iconSize, iconSize,
                new Vector4f(itemTint.x, itemTint.y, itemTint.z, itemTint.w * getWorldOpacity()));
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
                new Vector4f(countColor.x, countColor.y, countColor.z, countColor.w * getWorldOpacity()));
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

    public Vector4f getBackgroundColor() {
        return new Vector4f(backgroundColor);
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        backgroundColor.set(r, g, b, a);
    }

    public Vector4f getHoveredColor() {
        return new Vector4f(hoveredColor);
    }

    public void setHoveredColor(float r, float g, float b, float a) {
        hoveredColor.set(r, g, b, a);
    }

    public Vector4f getSelectedColor() {
        return new Vector4f(selectedColor);
    }

    public void setSelectedColor(float r, float g, float b, float a) {
        selectedColor.set(r, g, b, a);
    }

    public Vector4f getBorderColor() {
        return new Vector4f(borderColor);
    }

    public void setBorderColor(float r, float g, float b, float a) {
        borderColor.set(r, g, b, a);
    }

    public Vector4f getSelectedBorderColor() {
        return new Vector4f(selectedBorderColor);
    }

    public void setSelectedBorderColor(float r, float g, float b, float a) {
        selectedBorderColor.set(r, g, b, a);
    }

    public Vector4f getItemTint() {
        return new Vector4f(itemTint);
    }

    public void setItemTint(float r, float g, float b, float a) {
        itemTint.set(r, g, b, a);
    }

    public Vector4f getCountColor() {
        return new Vector4f(countColor);
    }

    public void setCountColor(float r, float g, float b, float a) {
        countColor.set(r, g, b, a);
    }
}