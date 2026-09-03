package com.isofarm.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Provides keyboard behavior.
 */
public final class Keyboard {
    private static final boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    private static final boolean[] lastKeys = new boolean[GLFW_KEY_LAST + 1];
    private static final StringBuilder typedCharacters = new StringBuilder();
    private static int modifiers;

    /**
     * Creates a new {@code Keyboard} instance. In private because
     * this is a static class.
     */
    private Keyboard() {}

    /**
     * Initializes the component.
     * @param windowId the window id value
     */
    public static void init(long windowId) {
        glfwSetKeyCallback(windowId, (window, key, scancode, action, mods) -> {
            modifiers = mods;
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                keys[key] = action != GLFW_RELEASE;
            }
        });

        glfwSetCharCallback(windowId, (window, codepoint) -> {
            typedCharacters.appendCodePoint(codepoint);
        });
    }

    /**
     * Updates the current state.
     */
    public static void update() {
        System.arraycopy(keys, 0, lastKeys, 0, keys.length);
    }

    /**
     * Checks whether the key pressed condition is met.
     * @param keyCode the key code value
     * @return {@code true} if key pressed; otherwise {@code false}
     */
    public static boolean isKeyPressed(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST && keys[keyCode] && !lastKeys[keyCode];
    }

    /**
     * Checks whether the key down condition is met.
     * @param keyCode the key code value
     * @return {@code true} if key down; otherwise {@code false}
     */
    public static boolean isKeyDown(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST && keys[keyCode];
    }

    /**
     * Checks whether the key released condition is met.
     * @param keyCode the key code value
     * @return {@code true} if key released; otherwise {@code false}
     */
    public static boolean isKeyReleased(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST && !keys[keyCode] && lastKeys[keyCode];
    }

    /**
     * Performs the any key pressed operation.
     * @return the any key pressed result
     */
    public static boolean anyKeyPressed() {
        for (boolean key : keys) {
            if (key) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the typed characters.
     * @return the typed characters
     */
    public static String getTypedCharacters() {
        String text = typedCharacters.toString();
        typedCharacters.setLength(0);
        return text;
    }

    /**
     * Returns the modifiers.
     * @return the modifiers
     */
    public static int getModifiers() {
        return modifiers;
    }
}