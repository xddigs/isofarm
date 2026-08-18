package com.tilled.gui;

import com.tilled.data.UIElement;
import org.joml.Vector4f;

@SuppressWarnings("all")
public class UIButton extends UIElement {
    private String text;
    private Vector4f normalColor = new Vector4f(0.15f, 0.15f, 0.15f, 1.0f);
    private Vector4f hoverColor = new Vector4f(0.25f, 0.25f, 0.25f, 1.0f);
    private Vector4f pressedColor = new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);

    public UIButton(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        this.text = text;
        setInteractable(true);
    }

    @Override
    public void render() {
        Vector4f color = normalColor;

        if (isPressed()) {
            color = pressedColor;
        } else if (isHovered()) {
            color = hoverColor;
        }

        GUI.drawRect(getAbsoluteX(), getAbsoluteY(),
                getAbsoluteWidth(), getAbsoluteHeight(), color);

        renderChildren();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Vector4f getNormalColor() {
        return new Vector4f(normalColor);
    }

    public void setNormalColor(float r, float g, float b, float a) {
        normalColor.set(r, g, b, a);
    }

    public Vector4f getHoverColor() {
        return new Vector4f(hoverColor);
    }

    public void setHoverColor(float r, float g, float b, float a) {
        hoverColor.set(r, g, b, a);
    }

    public Vector4f getPressedColor() {
        return new Vector4f(pressedColor);
    }

    public void setPressedColor(float r, float g, float b, float a) {
        pressedColor.set(r, g, b, a);
    }
}