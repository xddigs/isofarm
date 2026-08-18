package com.tilled.utils;

public class Settings {

    public static final float BASE_ICON_SIZE = 32.0f;
    public static final float[] GUI_SCALES = {0.5f, 1.0f, 1.25f, 1.5f, 2.0f};
    public static float fov = 80f;
    public static float mouseSensitivity = 0.4f;
    public static int renderDistance = 8;
    public static int unloadMargin = 2;
    public static int guiScaleIndex = 2;
    public static boolean doEnableMotions = true;

    private Settings() {}

    public static float getScale() {
        return GUI_SCALES[guiScaleIndex];
    }

    public static float getScaledIcon() {
        return BASE_ICON_SIZE * getScale();
    }

    public static float getScaledText() {
        return getScaledIcon() * 0.75f;
    }

    public static float getScaledButton() {
        return getScaledIcon() * 0.5f;
    }

    public static float getScaledLabel() {
        return getScaledIcon() * 0.35f;
    }

    public static float getScaledTooltip() {
        return getScaledIcon() * 0.25f;
    }

    public static float getScaledBorder() {
        return getScaledIcon() * 0.1f;
    }

    public static float getScaledFrame() {
        return getScaledIcon() * 0.05f;
    }

    public static float getScaledWindow() {
        return getScaledIcon() * 0.025f;
    }

    public static float getScaledSlot() {
        return getScaledIcon();
    }

    public static float getScaledSpacing() {
        return getScaledIcon() * 0.125f;
    }

    public static float getScaledPadding() {
        return getScaledIcon() * 0.375f;
    }

    public static float getScaledHeader() {
        return getScaledIcon();
    }

    public static float getScaledGUI() {
        return getScaledIcon() * 1.25f;
    }
}