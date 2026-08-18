package com.tilled.gui;

import com.tilled.data.UIElement;
import com.tilled.utils.Settings;

public class UITooltip extends UIElement {
    private String text = "";
    private float padding = Settings.getScaledPadding();
    private float offsetX = Settings.getScaledSpacing();
    private float offsetY = Settings.getScaledSpacing();

    public UITooltip() {
        super(0.0f, 0.0f, 0.0f, 0.0f);
        setZIndex(Integer.MAX_VALUE);
        setInteractable(false);
        hide();
    }

    public UITooltip(String text) {
        this();
        setText(text);
    }

    @Override
    public void render() {
        if (!isActuallyVisible() || text == null || text.isBlank()) {
            return;
        }

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

    public void updatePosition(float mouseX, float mouseY, float windowWidth, float windowHeight) {
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