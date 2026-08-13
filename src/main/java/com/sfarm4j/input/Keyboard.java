package com.sfarm4j.input;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {
    private static final boolean[] keys = new boolean[GLFW_KEY_LAST + 1];

    public static void init(long windowId) {
        glfwSetKeyCallback(windowId, (window, key, scancode, action, mods) -> {
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                keys[key] = (action != GLFW_RELEASE);
            }
        });
    }

    public static boolean isKeyDown(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST && keys[keyCode];
    }
}