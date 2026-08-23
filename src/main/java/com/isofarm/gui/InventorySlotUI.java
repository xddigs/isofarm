package com.isofarm.gui;

import com.isofarm.data.*;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.item.Item;
import com.isofarm.item.Tool;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
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

    private int lastAmount = 0;
    private Item lastItem = null;
    private float squishTimer = 0.0f;

    public InventorySlotUI(float x, float y, float width, float height) {
        super(x, y, width, height);
        setFocusable(true);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        int currentAmount = slot != null ? slot.getAmount() : 0;
        Item currentItem = getItem();

        if (currentAmount != lastAmount || currentItem != lastItem) {
            if (currentAmount > 0) {
                triggerSquish();
            }
            lastAmount = currentAmount;
            lastItem = currentItem;
        }

        if (squishTimer > 0.0f) {
            squishTimer -= delta;
            if (squishTimer < 0.0f) {
                squishTimer = 0.0f;
            }
        }
    }

    public void triggerSquish() {
        this.squishTimer = K.UI.SQUISH_DURATION;
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

        float borderSize = Settings.getScaledBorder();
        GUI.drawRect(x, y, width, borderSize, borderTint);
        GUI.drawRect(x, y + height - borderSize, width, borderSize, borderTint);
        GUI.drawRect(x, y, borderSize, height, borderTint);
        GUI.drawRect(x + width - borderSize, y, borderSize, height, borderTint);

        if (!isEmpty() && spriteSheet != null) {
            renderItem();
        }

        renderChildren();
    }

    private void renderItem() {
        float iconSize = Settings.getScaledIcon();

        float scaleX = 1.0f;
        float scaleY = 1.0f;

        if (squishTimer > 0.0f) {
            float progress = squishTimer / K.UI.SQUISH_DURATION;
            float deformation = (float) Math.sin(progress * Math.PI * 2.0) * progress * 0.25f;
            scaleX = 1.0f + deformation;
            scaleY = 1.0f - deformation;
        }

        float renderWidth = iconSize * scaleX;
        float renderHeight = iconSize * scaleY;
        float x = Math.round(getAbsoluteX() + (getAbsoluteWidth() - renderWidth) * 0.5f);
        float y = Math.round(getAbsoluteY() + (getAbsoluteHeight() - renderHeight) * 0.5f);

        GUI.drawSprite(spriteSheet, spriteFrame, x, y, renderWidth, renderHeight, new Vector4f(K.UI.UI_ITEM_TINT.x, K.UI.UI_ITEM_TINT.y, K.UI.UI_ITEM_TINT.z, K.UI.UI_ITEM_TINT.w * getWorldOpacity()));

        renderToolDurability();
        renderAmount();
    }

    private void renderToolDurability() {
        Item item = getItem();
        if (!(item instanceof Tool tool)) return;

        float maxDurability = tool.getMaxDurability();
        if (maxDurability <= 0.0f) return;

        float durability = Math.max(0.0f, Math.min(tool.getDurability(), maxDurability));
        if (durability >= maxDurability) return;
        float progress = durability / maxDurability;

        float padding = Settings.scale(2.0f);
        float barHeight = Settings.getScaledBorder();
        float barWidth =  getAbsoluteWidth() - padding * 2.0f;
        float x = getAbsoluteX() + padding;
        float y = getAbsoluteY() + getAbsoluteHeight() - barHeight - padding;
        GUI.drawRect(x, y, barWidth, barHeight, new Vector4f(0.15f, 0.15f, 0.15f, getWorldOpacity()));

        if (progress <= 0.0f) return;
        float red, green, blue = 0.0f;
        if (progress > 0.5f) {
            float t = (progress - 0.5f) * 2.0f;
            red = 1.0f - t;
            green = 1.0f;
        } else if (progress > 0.25f) {
            float t = (progress - 0.25f) * 4.0f;
            red = 1.0f;
            green = 0.5f + t * 0.5f;
        } else {
            float t = progress * 4.0f;
            red = 1.0f;
            green = t * 0.5f;
        }

        GUI.drawRect(x, y, barWidth * progress, barHeight,
                new Vector4f(red, green, blue, getWorldOpacity()));
    }

    private void renderAmount() {
        Item item = getItem();
        if (item == null || slot.getAmount() <= 1) {
            return;
        }

        String amount = String.valueOf(slot.getAmount());
        float textWidth = getTextWidth(amount);
        float textHeight = countFont.getSize();
        float paddingX = Settings.scale(2.0f);
        float paddingY = Settings.scale(1.0f);
        float x = getAbsoluteX() + getAbsoluteWidth() - textWidth - paddingX;
        float y = getAbsoluteY() + getAbsoluteHeight() - textHeight - paddingY;

        GUI.drawString(amount, x + Settings.scale(0.5f), y + textHeight, countFont,
                new Vector4f(0.0f, 0.0f, 0.0f, getWorldOpacity()));

        GUI.drawString(amount, x, y + textHeight, countFont, new Vector4f(K.UI.UI_TEXT_COLOR.x,
                K.UI.UI_TEXT_COLOR.y, K.UI.UI_TEXT_COLOR.z, K.UI.UI_TEXT_COLOR.w * getWorldOpacity()));
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