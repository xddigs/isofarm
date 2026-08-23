package com.isofarm.gui;

import org.joml.Vector4f;

public class UIPanel extends UIElement {
    private final Vector4f color = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    public UIPanel(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    @Override
    public void render() {
        renderChildren();
    }

    public Vector4f getColor() {
        return new Vector4f(color);
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }
}