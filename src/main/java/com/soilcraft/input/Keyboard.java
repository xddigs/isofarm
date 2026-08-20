package com.soilcraft.input;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {
    private static final boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    private static final boolean[] lastKeys = new boolean[GLFW_KEY_LAST + 1];
    private static final StringBuilder typedCharacters = new StringBuilder();
    private static int modifiers;

    public static void init(long windowId) {
        glfwSetKeyCallback(windowId, (unnamed, key, scancode, action, unnamed2) -> {
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                keys[key] = (action != GLFW_RELEASE);
            }
        });

        glfwSetCharCallback(windowId, (window, codepoint) -> {
            typedCharacters.appendCodePoint(codepoint);
        });

        glfwSetKeyCallback(windowId, (unnamed, key, scancode, action, mods) -> {
            modifiers = mods;

            if (key >= 0 && key <= GLFW_KEY_LAST) {
                keys[key] = (action != GLFW_RELEASE);
            }
        });
    }

    public static void update() {
        System.arraycopy(keys, 0, lastKeys, 0, keys.length);
    }

    public static boolean isKeyPressed(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST
                && keys[keyCode] && !lastKeys[keyCode];
    }

    public static boolean isKeyDown(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST && keys[keyCode];
    }

    public static boolean anyKeyPressed() {
        for (boolean key : keys) {
            if (key) return true;
        }

        return false;
    }

    public static boolean isKeyReleased(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST
                && !keys[keyCode] && lastKeys[keyCode];
    }

    public static String getTypedCharacters() {
        String text = typedCharacters.toString();
        typedCharacters.setLength(0);
        return text;
    }

    public static int getModifiers() {
        return modifiers;
    }
}