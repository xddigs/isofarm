package com.isofarm.utils;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class Settings {
    private static final float BASE_ICON_SIZE = 16.0f;
    private static final float[] GUI_SCALES = {1.0f, 2.0f, 3.0f};
    private static final float SHADOW_MAP_SIZE = 4096.0f;
    private static float fov = 80f;
    private static float mouseSensitivity = 0.4f;
    private static int renderDistance = 16;
    private static int unloadMargin = 4;
    private static int guiScaleIndex = 2;
    private static boolean doKeepInventory = false;
    private static boolean doEnableMotions = true;
    private static boolean isOrthographic = true;
    private static float maxInteractionDistance = 8.0f;
    private static boolean doEnableMusic = true;
    private static boolean doEnableDebugInfo = true;
    private static boolean doEnableShadows = false;

    private Settings() {
    }

    public static float getShadowMapSize() {
        return SHADOW_MAP_SIZE;
    }

    public static float getScale() {
        return GUI_SCALES[guiScaleIndex];
    }

    public static float scale(float value) {
        return value * getScale();
    }

    public static float getScaledEntity() {
        return scale(0.5f);
    }

    public static float getScaledIcon() {
        return scale(BASE_ICON_SIZE);
    }

    public static float getScaledText() {
        return scale(12.0f);
    }

    public static float getScaledButton() {
        return scale(8.0f);
    }

    public static float getScaledLabel() {
        return scale(5.6f);
    }

    public static float getScaledTooltip() {
        return scale(4.0f);
    }

    public static float getScaledBorder() {
        return scale(1.6f);
    }

    public static float getScaledFrame() {
        return scale(0.8f);
    }

    public static float getScaledWindow() {
        return scale(0.4f);
    }

    public static float getScaledSlot() {
        return getScaledIcon();
    }

    public static float getScaledSpacing() {
        return scale(2.0f);
    }

    public static float getScaledPadding() {
        return scale(6.0f);
    }

    public static float getScaledCornerRadius() {
        return scale(4.0f);
    }

    public static float getScaledThickness() {
        return scale(0.8f);
    }

    public static float getScaledHeader() {
        return getScaledIcon();
    }

    public static boolean isOrthographic() {
        return isOrthographic;
    }

    public static void setOrthographic(boolean isOrthographic) {
        Settings.isOrthographic = isOrthographic;
    }

    public static float getMaxInteractionDistance() {
        return isOrthographic() ? maxInteractionDistance : maxInteractionDistance - 3.0f;
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

    public static boolean doEnableShadows() {
        return doEnableShadows;
    }

    public static void setDoEnableShadows(boolean doEnableShadows) {
        Settings.doEnableShadows = doEnableShadows;
    }

    public static float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public static float doEnableMotions() {
        return doEnableMotions ? 0.8f : 0.0f;
    }

    public static boolean doKeepInventory() {
        return doKeepInventory;
    }

    public static void setDoKeepInventory(boolean doKeepInventory) {
        Settings.doKeepInventory = doKeepInventory;
    }

    public static void setDoEnableMotions(boolean doEnableMotions) {
        Settings.doEnableMotions = doEnableMotions;
    }

    public static void toggleMotionBlur() {
        doEnableMotions = !doEnableMotions;
    }

    public static void toggleKeepInventory() {
        doKeepInventory = !doKeepInventory;
    }

    public static void toggleShadows() {
        doEnableShadows = !doEnableShadows;
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