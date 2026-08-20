package com.soilcraft.gui;

import com.soilcraft.data.UIElement;
import org.joml.Vector4f;

@SuppressWarnings("unused")
public class UILabel extends UIElement {
    private final Vector4f color = new Vector4f(1.0f);
    private String text;
    private UIFont font;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;

    public UILabel(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        this.text = text;
        this.font = GUI.getNormalFont();
    }

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

    public String getText() {
        return text;
    }

    public UILabel setText(String text) {
        this.text = text;
        return this;
    }

    public UIFont getFont() {
        return font;
    }

    public UILabel setFont(UIFont font) {
        this.font = font;
        return this;
    }

    public Vector4f getColor() {
        return new Vector4f(color);
    }

    public UILabel setColor(Vector4f color) {
        if (color == null) {
            this.color.set(1.0f);
        } else {
            this.color.set(color);
        }

        return this;
    }

    public UILabel setColor(float r, float g, float b) {
        this.color.set(r, g, b, 1.0f);
        return this;
    }

    public UILabel setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        return this;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public UILabel setHorizontalAlignment(HorizontalAlignment alignment) {
        this.horizontalAlignment = alignment == null ? HorizontalAlignment.LEFT : alignment;
        return this;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public UILabel setVerticalAlignment(VerticalAlignment alignment) {
        this.verticalAlignment = alignment == null ? VerticalAlignment.TOP : alignment;
        return this;
    }

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

    public enum HorizontalAlignment {
        LEFT, CENTER, RIGHT
    }

    public enum VerticalAlignment {
        TOP, CENTER, BOTTOM
    }
}