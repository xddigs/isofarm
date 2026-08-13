package com.sfarm4j.input;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {
    private static final boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    private static final boolean[] lastKeys = new boolean[GLFW_KEY_LAST + 1];

    public static void init(long windowId) {
        glfwSetKeyCallback(windowId, (_, key, _, action, _) -> {
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

    public static boolean isKeyReleased(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST
                && !keys[keyCode] && lastKeys[keyCode];
    }
}