package com.isofarm.gui;

import com.isofarm.data.UIElement;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;

@SuppressWarnings("unused")
public class UITooltip extends UIElement {
    private String text = "";
    private float padding = Settings.getScaledPadding();
    private float offsetX = Settings.getScaledSpacing();
    private float offsetY = Settings.getScaledSpacing();
    private static final float OFFSET_PADDING = 2.2f;

    public UITooltip() {
        super(0.0f, 0.0f, 0.0f, 0.0f);
        setZIndex(Integer.MAX_VALUE);
        setInteractable(false);
        hide();
    }

    @Override
    public void render() {
        if (!isActuallyVisible() || text == null || text.isBlank()) {
            return;
        }

        GUI.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteWidth(), getAbsoluteHeight(),
                K.UI.UI_BACKGROUND_COLOR, Settings.getScaledCornerRadius(),
                K.UI.UI_BORDER_COLOR, Settings.getScaledThickness());

        GUI.drawString(text, getAbsoluteX() + padding,
                getAbsoluteY() + padding * OFFSET_PADDING, GUI.getNormalFont(),
                K.UI.UI_TEXT_COLOR);

        renderChildren();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public UITooltip text(String text) {
        setText(text);
        UIFont font = GUI.getNormalFont();
        float textWidth = GUI.getStringWidth(this.text, font);
        float textHeight = font.getSize();
        float totalWidth = textWidth + (padding * 2.0f);
        float totalHeight = textHeight + (padding * 2.0f);
        setSize(totalWidth, totalHeight);
        return this;
    }

    public float getPadding() {
        return padding;
    }

    public void setPadding(float padding) {
        this.padding = Math.max(0.0f, padding);
    }

    public UITooltip padding(float padding) {
        setPadding(padding);
        return this;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public UITooltip offset(float x, float y) {
        setOffsetX(x);
        setOffsetY(y);
        return this;
    }

    public void updatePosition(float mouseX, float mouseY,
                               float windowWidth, float windowHeight) {
        float x = mouseX + offsetX;
        float y = mouseY + offsetY;

        if (x + getWidth() > windowWidth) {
            x = mouseX - getWidth() - offsetX;
        }

        if (y + getHeight() > windowHeight) {
            y = mouseY - getHeight() - offsetY;
        }

        setPosition(x, y);
    }
}