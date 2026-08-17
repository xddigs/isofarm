package com.tilled.utils;

public class Settings {

    private Settings() {}

    public static float fov = 80f;
    public static float mouseSensitivity = 0.4f;
    public static int renderDistance = 8;
    public static int unloadMargin = 2;

    public static final float BASE_ICON_SIZE = 32.0f;
    public static int guiScaleIndex = 0;
    public static final float[] GUI_SCALES = {1.0f, 1.25f, 1.5f, 2.0f};

    public static float getScaledIconSize() {
        return BASE_ICON_SIZE * GUI_SCALES[guiScaleIndex];
    }
}