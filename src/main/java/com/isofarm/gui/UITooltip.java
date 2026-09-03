package com.isofarm.gui;

import com.isofarm.utils.K;
import com.isofarm.utils.Local;
import com.isofarm.utils.Settings;

/**
 * Provides uitooltip behavior.
 */
@SuppressWarnings("unused")
public class UITooltip extends UIElement {
    private static final float OFFSET_PADDING = 2.2f;
    private String text = "";
    private float padding = Settings.getScaledPadding();
    private float offsetX = Settings.getScaledSpacing();
    private float offsetY = Settings.getScaledSpacing();

    /**
     * Creates a new {@code UITooltip} instance.
     */
    public UITooltip() {
        super(0.0f, 0.0f, 0.0f, 0.0f);
        setZIndex(Integer.MAX_VALUE);
        setInteractable(false);
        hide();
    }

    /**
     * Renders render.
     */
    @Override
    public void render() {
        if (!isActuallyVisible() || text == null || text.isBlank()) return;
        GUI.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteWidth(), getAbsoluteHeight(),
                K.UI.UI_BACKGROUND_COLOR, Settings.getScaledCornerRadius(),
                K.UI.UI_BORDER_COLOR, Settings.getScaledThickness());

        UIFont font = GUI.getNormalFont();
        float lineHeight = font.getSize();
        String[] lines = text.split("\n", -1);
        float textY = getAbsoluteY() + padding * 2.2f;
        for (String line : lines) {
            GUI.drawString(line, getAbsoluteX() + padding, textY, font, K.UI.UI_TEXT_COLOR);
            textY += lineHeight;
        }

        renderChildren();
    }

    /**
     * Returns the text.
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text.
     * @param text the text value
     */
    public void setText(String text) {
        this.text = text == null ? "" : Local.lang.t(text);
    }

    /**
     * Performs the text operation.
     * @param text the text value
     * @return the text result
     */
    public UITooltip text(String text) {
        setText(text);
        UIFont font = GUI.getNormalFont();
        String[] lines = this.text.split("\n", -1);
        float maxWidth = 0.0f;
        for (String line : lines) {
            maxWidth = Math.max(
                    maxWidth,
                    GUI.getStringWidth(line, font)
            );
        }

        float lineHeight = font.getSize();
        float totalWidth = maxWidth + padding * 2.0f;
        float totalHeight = (lineHeight * lines.length) + padding * 2.0f;
        setSize(totalWidth, totalHeight);
        return this;
    }

    /**
     * Returns the padding.
     * @return the padding
     */
    public float getPadding() {
        return padding;
    }

    /**
     * Sets the padding.
     * @param padding the padding value
     */
    public void setPadding(float padding) {
        this.padding = Math.max(0.0f, padding);
    }

    /**
     * Performs the padding operation.
     * @param padding the padding value
     * @return the padding result
     */
    public UITooltip padding(float padding) {
        setPadding(padding);
        return this;
    }

    /**
     * Returns the offset x.
     * @return the offset x
     */
    public float getOffsetX() {
        return offsetX;
    }

    /**
     * Sets the offset x.
     * @param offsetX the offset x value
     */
    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * Returns the offset y.
     * @return the offset y
     */
    public float getOffsetY() {
        return offsetY;
    }

    /**
     * Sets the offset y.
     * @param offsetY the offset y value
     */
    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * Performs the offset operation.
     * @param x the x value
     * @param y the y value
     * @return the offset result
     */
    public UITooltip offset(float x, float y) {
        setOffsetX(x);
        setOffsetY(y);
        return this;
    }

    /**
     * Updates the position.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @param windowWidth the window width value
     * @param windowHeight the window height value
     */
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