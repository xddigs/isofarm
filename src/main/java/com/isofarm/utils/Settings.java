package com.isofarm.utils;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class Settings {
    private static final float BASE_ICON_SIZE = 32.0f;
    private static final float[] GUI_SCALES = {0.5f, 1.0f, 1.25f, 1.5f, 2.0f};
    private static float fov = 80f;
    private static final float SHADOW_MAP_SIZE = 4096.0f;
    private static float mouseSensitivity = 0.4f;
    private static int renderDistance = 12;
    private static int unloadMargin = 2;
    private static int guiScaleIndex = 2;
    private static boolean doEnableMotions = true;
    private static boolean isOrthographic = true;
    private static float maxInteractionDistance = 5.0f;
    private static boolean doEnableMusic = true;
    private static boolean doEnableDebugInfo = true;

    private Settings() {}

    public static float getShadowMapSize() {
        return SHADOW_MAP_SIZE;
    }

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

    public static float getScaledCornerRadius() {
        return getScaledIcon() * 0.25f;
    }

    public static float getScaledThickness() {
        return getScaledIcon() * 0.050f;
    }

    public static float getScaledHeader() {
        return getScaledIcon();
    }

    public static float getScaledGUI() {
        return getScaledIcon() * 1.25f;
    }

    public static boolean isOrthographic() {
        return isOrthographic;
    }

    public static void setOrthographic(boolean isOrthographic) {
        Settings.isOrthographic = isOrthographic;
    }

    public static float getMaxInteractionDistance() {
        return isOrthographic() ? 1500f : maxInteractionDistance;
    }

    public static void setMaxInteractionDistance(float maxInteractionDistance) {
        Settings.maxInteractionDistance = maxInteractionDistance;
    }

    public static float getFov() {
        return fov;
    }

    public static void setFov(float fov) {
        Settings.fov = fov;
    }

    public static float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public static float doEnableMotions() {
        return doEnableMotions ? 0.8f : 0.0f;
    }

    public static void setDoEnableMotions(boolean doEnableMotions) {
        Settings.doEnableMotions = doEnableMotions;
    }

    public static void toggleMotionBlur() {
        doEnableMotions = !doEnableMotions;
    }

    public static void toggleOrthographic() {
        isOrthographic = !isOrthographic;
    }

    public static void toggleMusic() {
        doEnableMusic = !doEnableMusic;
    }

    public static boolean doEnableMusic() {
        return doEnableMusic;
    }

    public static void toggleDebugInfo() {
        doEnableDebugInfo = !doEnableDebugInfo;
    }

    public static boolean doEnableDebugInfo() {
        return doEnableDebugInfo;
    }

    public static int getRenderDistance() {
        return renderDistance;
    }

    public static void setRenderDistance(int renderDistance) {
        Settings.renderDistance = renderDistance;
    }

    public static int getUnloadMargin() {
        return unloadMargin;
    }

    public static void setUnloadMargin(int unloadMargin) {
        Settings.unloadMargin = unloadMargin;
    }

    public static int getGuiScaleIndex() {
        return guiScaleIndex;
    }
}