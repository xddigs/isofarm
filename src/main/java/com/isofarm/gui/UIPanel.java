package com.isofarm.gui;

import org.joml.Vector4f;

/**
 * Provides uipanel behavior.
 */
public class UIPanel extends UIElement {
    private final Vector4f color = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    /**
     * Creates a new {@code UIPanel} instance.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     */
    public UIPanel(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    /**
     * Renders render.
     */
    @Override
    public void render() {
        renderChildren();
    }

    /**
     * Returns the color.
     * @return the color
     */
    public Vector4f getColor() {
        return new Vector4f(color);
    }

    /**
     * Sets the color.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }
}