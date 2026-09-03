package com.isofarm.gui;

import com.isofarm.utils.Local;
import org.joml.Vector4f;

/**
 * Provides uilabel behavior.
 */
@SuppressWarnings("unused")
public class UILabel extends UIElement {
    private final Vector4f color = new Vector4f(1.0f);
    private String text;
    private UIFont font;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;

    /**
     * Creates a new {@code UILabel} instance.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param text the text value
     */
    public UILabel(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        this.text = text;
        this.font = GUI.getNormalFont();
    }

    /**
     * Renders render.
     */
    @Override
    public void render() {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }

        float textWidth = getTextWidth();
        float textHeight = font.getSize();

        float drawX = getAbsoluteX();
        float drawY = getAbsoluteY();

        switch (horizontalAlignment) {
            case CENTER -> drawX += (getAbsoluteWidth() - textWidth) * 0.5f;
            case RIGHT -> drawX += getAbsoluteWidth() - textWidth;
        }

        switch (verticalAlignment) {
            case CENTER -> drawY += (getAbsoluteHeight() - textHeight) * 0.5f;
            case BOTTOM -> drawY += getAbsoluteHeight() - textHeight;
        }

        GUI.drawString(text, drawX, drawY, font, new Vector4f(color.x,
                color.y, color.z, color.w * getWorldOpacity()));
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
     * @return the set text result
     */
    public UILabel setText(String text) {
        this.text = Local.lang.t(text);
        return this;
    }

    /**
     * Returns the font.
     * @return the font
     */
    public UIFont getFont() {
        return font;
    }

    /**
     * Sets the font.
     * @param font the font value
     * @return the set font result
     */
    public UILabel setFont(UIFont font) {
        this.font = font;
        return this;
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
     * @param color the color value
     * @return the set color result
     */
    public UILabel setColor(Vector4f color) {
        if (color == null) {
            this.color.set(1.0f);
        } else {
            this.color.set(color);
        }

        return this;
    }

    /**
     * Sets the color.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @return the set color result
     */
    public UILabel setColor(float r, float g, float b) {
        this.color.set(r, g, b, 1.0f);
        return this;
    }

    /**
     * Sets the color.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     * @return the set color result
     */
    public UILabel setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        return this;
    }

    /**
     * Returns the horizontal alignment.
     * @return the horizontal alignment
     */
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment.
     * @param alignment the alignment value
     * @return the set horizontal alignment result
     */
    public UILabel setHorizontalAlignment(HorizontalAlignment alignment) {
        this.horizontalAlignment = alignment == null ? HorizontalAlignment.LEFT : alignment;
        return this;
    }

    /**
     * Returns the vertical alignment.
     * @return the vertical alignment
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the vertical alignment.
     * @param alignment the alignment value
     * @return the set vertical alignment result
     */
    public UILabel setVerticalAlignment(VerticalAlignment alignment) {
        this.verticalAlignment = alignment == null ? VerticalAlignment.TOP : alignment;
        return this;
    }

    /**
     * Returns the text width.
     * @return the text width
     */
    public float getTextWidth() {
        if (text == null || text.isEmpty() || font == null) {
            return 0.0f;
        }

        float width = 0.0f;

        for (int i = 0; i < text.length(); i++) {
            var glyph = font.getGlyph(text.charAt(i));
            if (glyph == null) {
                width += font.getSize() * 0.5f;
                continue;
            }

            width += glyph.xadvance();
        }

        return width;
    }

    /**
     * Enumerates the supported horizontal alignment values.
     */
    public enum HorizontalAlignment {
        LEFT, CENTER, RIGHT
    }

    /**
     * Enumerates the supported vertical alignment values.
     */
    public enum VerticalAlignment {
        TOP, CENTER, BOTTOM
    }
}