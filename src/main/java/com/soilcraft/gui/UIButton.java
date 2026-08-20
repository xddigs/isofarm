package com.soilcraft.gui;

import com.soilcraft.data.UIElement;
import com.soilcraft.graphics.SpriteSheet;
import org.joml.Vector4f;

@SuppressWarnings("all")
public class UIButton extends UIElement {
    private SpriteSheet spriteSheet;
    private int spriteFrame;
    private Runnable onClick;

    private Vector4f normalColor = new Vector4f(0.15f, 0.15f, 0.15f, 1.0f);
    private Vector4f hoverColor = new Vector4f(0.25f, 0.25f, 0.25f, 1.0f);
    private Vector4f pressedColor = new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);

    public UIButton(float x, float y, float width, float height) {
        super(x, y, width, height);
        setInteractable(true);
        setFocusable(true);
    }

    @Override
    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);

        if (button != 0 || !isHovered()) {
            return false;
        }

        if (onClick != null) {
            onClick.run();
        }

        return false;
    }

    @Override
    public void render() {
        Vector4f color = normalColor;

        if (isPressed()) {
            color = pressedColor;
        } else if (isHovered()) {
            color = hoverColor;
        }

        GUI.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteWidth(),
                getAbsoluteHeight(),
                new Vector4f(color.x, color.y, color.z, color.w * getWorldOpacity()));

        if (spriteSheet != null) {
            float size = Math.min(getAbsoluteWidth(), getAbsoluteHeight()) * 0.65f;
            float x = getAbsoluteX() + (getAbsoluteWidth() - size) * 0.5f;
            float y = getAbsoluteY() + (getAbsoluteHeight() - size) * 0.5f;
            GUI.drawSprite(spriteSheet, spriteFrame, x, y, size, size,
                    new Vector4f(1.0f, 1.0f, 1.0f, getWorldOpacity()));
        }

        renderChildren();
    }

    public SpriteSheet getSpriteSheet() {
        return spriteSheet;
    }

    public void setSpriteSheet(SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    public int getSpriteFrame() {
        return spriteFrame;
    }

    public void setSpriteFrame(int spriteFrame) {
        this.spriteFrame = Math.max(0, spriteFrame);
    }

    public Runnable getOnClick() {
        return onClick;
    }

    public UIButton setOnClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public Vector4f getNormalColor() {
        return new Vector4f(normalColor);
    }

    public UIButton setNormalColor(float r, float g, float b, float a) {
        normalColor.set(r, g, b, a);
        return this;
    }

    public Vector4f getHoverColor() {
        return new Vector4f(hoverColor);
    }

    public UIButton setHoverColor(float r, float g, float b, float a) {
        hoverColor.set(r, g, b, a);
        return this;
    }

    public Vector4f getPressedColor() {
        return new Vector4f(pressedColor);
    }

    public UIButton setPressedColor(float r, float g, float b, float a) {
        pressedColor.set(r, g, b, a);
        return this;
    }
}