package com.isofarm.gui;

import com.isofarm.data.UIElement;
import org.joml.Vector4f;

public class UIProgressBar extends UIElement {
    private float value;
    private float maxValue;
    
    private float borderWidth = 2.0f;
    private float cornerRadius = 4.0f;
    private boolean showText = true;

    private Vector4f backgroundColor = new Vector4f(0.15f, 0.15f, 0.15f, 0.8f);
    private Vector4f fillColor = new Vector4f(0.8f, 0.2f, 0.2f, 1.0f);
    private Vector4f borderColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
    private final Vector4f textColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    public UIProgressBar(float x, float y, float width, float height, float value, float maxValue) {
        super(x, y, width, height);
        this.maxValue = Math.max(1.0f, maxValue);
        this.value = Math.clamp(value, 0.0f, this.maxValue);
    }

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

    public UIProgressBar setValues(float value, float maxValue) {
        this.maxValue = Math.max(1.0f, maxValue);
        this.value = Math.clamp(value, 0.0f, this.maxValue);
        return this;
    }

    public UIProgressBar setValue(float value) {
        this.value = Math.clamp(value, 0.0f, maxValue);
        return this;
    }

    public UIProgressBar setColors(Vector4f fillColor, Vector4f backgroundColor) {
        if (fillColor != null) this.fillColor.set(fillColor);
        if (backgroundColor != null) this.backgroundColor.set(backgroundColor);
        return this;
    }

    public UIProgressBar setBorder(Vector4f borderColor, float borderWidth) {
        if (borderColor != null) this.borderColor.set(borderColor);
        this.borderWidth = borderWidth;
        return this;
    }

    public UIProgressBar setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }

    public UIProgressBar setShowText(boolean showText) {
        this.showText = showText;
        return this;
    }

    public float getValue() { return value; }
    public float getMaxValue() { return maxValue; }
}