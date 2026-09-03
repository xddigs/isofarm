package com.isofarm.gui;

import org.joml.Vector4f;

/**
 * Provides uiprogress bar behavior.
 */
public class UIProgressBar extends UIElement {
    private float value;
    private float maxValue;
    
    private float borderWidth = 2.0f;
    private float cornerRadius = 4.0f;
    private boolean showText;

    private Vector4f backgroundColor = new Vector4f(0.15f, 0.15f, 0.15f, 0.8f);
    private Vector4f fillColor = new Vector4f(0.8f, 0.2f, 0.2f, 1.0f);
    private Vector4f borderColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
    private final Vector4f textColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    /**
     * Creates a new {@code UIProgressBar} instance.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param value the value value
     * @param maxValue the max value value
     * @param showText the show text value
     */
    public UIProgressBar(float x, float y, float width, float height,
                         float value, float maxValue, boolean showText) {
        super(x, y, width, height);
        this.maxValue = Math.max(1.0f, maxValue);
        this.value = Math.clamp(value, 0.0f, this.maxValue);
        this.showText = showText;
    }

    /**
     * Renders render.
     */
    @Override
    public void render() {
        if (!isVisible()) {
            return;
        }

        float absX = getAbsoluteX();
        float absY = getAbsoluteY();
        float absW = getAbsoluteWidth();
        float absH = getAbsoluteHeight();

        GUI.drawRect(absX, absY, absW, absH, backgroundColor,
                cornerRadius, borderColor, borderWidth);

        float fillPercentage = Math.clamp(value / maxValue, 0.0f, 1.0f);
        float fillWidth = (absW - (borderWidth * 2.0f)) * fillPercentage;

        if (fillWidth > 0.0f) {
            float fillX = absX + borderWidth;
            float fillY = absY + borderWidth;
            float fillH = absH - (borderWidth * 2.0f);
            
            float fillRadius = Math.max(0.0f, cornerRadius - borderWidth);
            GUI.drawRect(fillX, fillY, fillWidth, fillH, fillColor, fillRadius);
        }

        if (showText) {
            String text = (int) value + " / " + (int) maxValue;
            UIFont font = GUI.getSmallBoldFont();
            float textW = GUI.getStringWidth(text, font);
            float textX = absX + (absW - textW) * 0.5f;
            float textY = GUI.getCenteredTextY(text, font, absY, absH);

            GUI.drawString(text, textX, textY, font, textColor);
        }
    }

    /**
     * Sets the values.
     * @param value the value value
     * @param maxValue the max value value
     * @return the set values result
     */
    public UIProgressBar setValues(float value, float maxValue) {
        this.maxValue = Math.max(1.0f, maxValue);
        this.value = Math.clamp(value, 0.0f, this.maxValue);
        return this;
    }

    /**
     * Sets the value.
     * @param value the value value
     * @return the set value result
     */
    public UIProgressBar setValue(float value) {
        this.value = Math.clamp(value, 0.0f, maxValue);
        return this;
    }

    /**
     * Sets the colors.
     * @param fillColor the fill color value
     * @param backgroundColor the background color value
     * @return the set colors result
     */
    public UIProgressBar setColors(Vector4f fillColor, Vector4f backgroundColor) {
        if (fillColor != null) this.fillColor.set(fillColor);
        if (backgroundColor != null) this.backgroundColor.set(backgroundColor);
        return this;
    }

    /**
     * Sets the border.
     * @param borderColor the border color value
     * @param borderWidth the border width value
     * @return the set border result
     */
    public UIProgressBar setBorder(Vector4f borderColor, float borderWidth) {
        if (borderColor != null) this.borderColor.set(borderColor);
        this.borderWidth = borderWidth;
        return this;
    }

    /**
     * Sets the corner radius.
     * @param cornerRadius the corner radius value
     * @return the set corner radius result
     */
    public UIProgressBar setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }

    /**
     * Sets the show text.
     * @param showText the show text value
     * @return the set show text result
     */
    public UIProgressBar setShowText(boolean showText) {
        this.showText = showText;
        return this;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public float getValue() { return value; }
    /**
     * Returns the max value.
     * @return the max value
     */
    public float getMaxValue() { return maxValue; }
}