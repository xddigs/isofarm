package com.isofarm.utils;

import com.isofarm.data.Singleton;
import com.isofarm.item.Item;

/**
 * Provides settings behavior.
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Singleton
public class Settings {
    private static final float BASE_ICON_SIZE = 16.0f;
    private static final float[] GUI_SCALES = {1.0f, 2.0f, 3.0f};
    private static final float SHADOW_MAP_SIZE = 8192.0f;
    public static Item selectedItem = null;
    private static float fov = 80f;
    private static float ticks = 12000f;
    private static float mouseSensitivity = 0.4f;
    private static int renderDistance = 16;
    private static int unloadMargin = 4;
    private static int guiScaleIndex = 2;
    private static boolean doKeepInventory = true;
    private static boolean doEnableMotions = false;
    private static float maxInteractionDistance = 6.0f;
    private static boolean doEnableMusic = true;
    private static boolean doEnableDebugInfo = true;
    private static boolean doEnableShadows = true;

    /**
     * Creates a new {@code Settings} instance.
     */
    private Settings() {}

    /**
     * Returns the shadow map size.
     * @return the shadow map size
     */
    public static float getShadowMapSize() {
        return SHADOW_MAP_SIZE;
    }

    /**
     * Returns the scale.
     * @return the scale
     */
    public static float getScale() {
        return GUI_SCALES[guiScaleIndex];
    }

    /**
     * Performs the scale operation.
     * @param value the value value
     * @return the scale result
     */
    public static float scale(float value) {
        return value * getScale();
    }

    /**
     * Returns the scaled entity.
     * @return the scaled entity
     */
    public static float getScaledEntity() {
        return scale(0.335f);
    }

    /**
     * Returns the scaled icon.
     * @return the scaled icon
     */
    public static float getScaledIcon() {
        return scale(BASE_ICON_SIZE);
    }

    /**
     * Returns the scaled text.
     * @return the scaled text
     */
    public static float getScaledText() {
        return scale(12.0f);
    }

    /**
     * Returns the scaled button.
     * @return the scaled button
     */
    public static float getScaledButton() {
        return scale(8.0f);
    }

    /**
     * Returns the scaled label.
     * @return the scaled label
     */
    public static float getScaledLabel() {
        return scale(5.6f);
    }

    /**
     * Returns the scaled tooltip.
     * @return the scaled tooltip
     */
    public static float getScaledTooltip() {
        return scale(4.0f);
    }

    /**
     * Returns the scaled border.
     * @return the scaled border
     */
    public static float getScaledBorder() {
        return scale(1.6f);
    }

    /**
     * Returns the scaled frame.
     * @return the scaled frame
     */
    public static float getScaledFrame() {
        return scale(0.8f);
    }

    /**
     * Returns the scaled window.
     * @return the scaled window
     */
    public static float getScaledWindow() {
        return scale(0.4f);
    }

    /**
     * Returns the scaled slot.
     * @return the scaled slot
     */
    public static float getScaledSlot() {
        return getScaledIcon();
    }

    /**
     * Returns the scaled spacing.
     * @return the scaled spacing
     */
    public static float getScaledSpacing() {
        return scale(2.0f);
    }

    /**
     * Returns the scaled padding.
     * @return the scaled padding
     */
    public static float getScaledPadding() {
        return scale(6.0f);
    }

    /**
     * Returns the scaled corner radius.
     * @return the scaled corner radius
     */
    public static float getScaledCornerRadius() {
        return scale(4.0f);
    }

    /**
     * Returns the scaled thickness.
     * @return the scaled thickness
     */
    public static float getScaledThickness() {
        return scale(0.8f);
    }

    /**
     * Returns the scaled header.
     * @return the scaled header
     */
    public static float getScaledHeader() {
        return getScaledIcon();
    }

    /**
     * Returns the max interaction distance.
     * @return the max interaction distance
     */
    public static float getMaxInteractionDistance() {
        return maxInteractionDistance;
    }

    /**
     * Sets the max interaction distance.
     * @param maxInteractionDistance the max interaction distance value
     */
    public static void setMaxInteractionDistance(float maxInteractionDistance) {
        Settings.maxInteractionDistance = maxInteractionDistance;
    }

    /**
     * Returns the fov.
     * @return the fov
     */
    public static float getFov() {
        return fov;
    }

    /**
     * Sets the fov.
     * @param fov the fov value
     */
    public static void setFov(float fov) {
        Settings.fov = fov;
    }

    /**
     * Performs the do enable shadows operation.
     * @return the do enable shadows result
     */
    public static boolean doEnableShadows() {
        return doEnableShadows;
    }

    /**
     * Sets the do enable shadows.
     * @param doEnableShadows the do enable shadows value
     */
    public static void setDoEnableShadows(boolean doEnableShadows) {
        Settings.doEnableShadows = doEnableShadows;
    }

    /**
     * Returns the mouse sensitivity.
     * @return the mouse sensitivity
     */
    public static float getMouseSensitivity() {
        return mouseSensitivity;
    }

    /**
     * Performs the do enable motions operation.
     * @return the do enable motions result
     */
    public static float doEnableMotions() {
        return doEnableMotions ? 0.8f : 0.0f;
    }

    /**
     * Performs the do keep inventory operation.
     * @return the do keep inventory result
     */
    public static boolean doKeepInventory() {
        return doKeepInventory;
    }

    /**
     * Sets the do keep inventory.
     * @param doKeepInventory the do keep inventory value
     */
    public static void setDoKeepInventory(boolean doKeepInventory) {
        Settings.doKeepInventory = doKeepInventory;
    }

    /**
     * Sets the do enable motions.
     * @param doEnableMotions the do enable motions value
     */
    public static void setDoEnableMotions(boolean doEnableMotions) {
        Settings.doEnableMotions = doEnableMotions;
    }

    /**
     * Performs the toggle motion blur operation.
     */
    public static void toggleMotionBlur() {
        doEnableMotions = !doEnableMotions;
    }

    /**
     * Performs the toggle keep inventory operation.
     */
    public static void toggleKeepInventory() {
        doKeepInventory = !doKeepInventory;
    }

    /**
     * Performs the toggle shadows operation.
     */
    public static void toggleShadows() {
        doEnableShadows = !doEnableShadows;
    }

    /**
     * Performs the toggle music operation.
     */
    public static void toggleMusic() {
        doEnableMusic = !doEnableMusic;
    }

    /**
     * Performs the do enable music operation.
     * @return the do enable music result
     */
    public static boolean doEnableMusic() {
        return doEnableMusic;
    }

    /**
     * Performs the toggle debug info operation.
     */
    public static void toggleDebugInfo() {
        doEnableDebugInfo = !doEnableDebugInfo;
    }

    /**
     * Performs the do enable debug info operation.
     * @return the do enable debug info result
     */
    public static boolean doEnableDebugInfo() {
        return doEnableDebugInfo;
    }

    /**
     * Returns the render distance.
     * @return the render distance
     */
    public static int getRenderDistance() {
        return renderDistance;
    }

    /**
     * Sets the render distance.
     * @param renderDistance the render distance value
     */
    public static void setRenderDistance(int renderDistance) {
        Settings.renderDistance = renderDistance;
    }

    /**
     * Returns the unload margin.
     * @return the unload margin
     */
    public static int getUnloadMargin() {
        return unloadMargin;
    }

    /**
     * Sets the unload margin.
     * @param unloadMargin the unload margin value
     */
    public static void setUnloadMargin(int unloadMargin) {
        Settings.unloadMargin = unloadMargin;
    }

    /**
     * Returns the gui scale index.
     * @return the gui scale index
     */
    public static int getGuiScaleIndex() {
        return guiScaleIndex;
    }

    /**
     * Returns the ticks.
     * @return the ticks
     */
    public static float getTicks() {
        return ticks;
    }
}