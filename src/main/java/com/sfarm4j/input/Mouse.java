package com.sfarm4j.input;

import static org.lwjgl.glfw.GLFW.*;

public class Mouse {
    private static final boolean[] buttons = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private static final boolean[] lastButtons = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];

    private static float x = 0, y = 0;
    private static float lastX = 0, lastY = 0;
    private static float deltaX = 0, deltaY = 0;
    private static boolean firstMouse = true;
    private static float scrollY = 0.0f;

    public static void init(long windowId) {
        glfwSetCursorPosCallback(windowId, (_, xpos, ypos) -> {
            float currentX = (float) xpos;
            float currentY = (float) ypos;

            if (firstMouse) {
                lastX = currentX;
                lastY = currentY;
                firstMouse = false;
            }

            deltaX = currentX - lastX;
            deltaY = currentY - lastY;

            lastX = currentX;
            lastY = currentY;
            x = currentX;
            y = currentY;
        });

        glfwSetMouseButtonCallback(windowId, (_, button, action, _) -> {
            if (button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST) {
                buttons[button] = (action != GLFW_RELEASE);
            }
        });

        glfwSetScrollCallback(windowId, (_, _, yoffset) -> {
            scrollY = (float) yoffset;
        });
    }

    public static void update() {
        System.arraycopy(buttons, 0, lastButtons, 0, buttons.length);
        deltaX = 0;
        deltaY = 0;
        scrollY = 0.0f;
    }

    public static boolean isButtonDown(int button) {
        return button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST && buttons[button];
    }

    public static boolean isButtonPressed(int button) {
        return button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST
                && buttons[button] && !lastButtons[button];
    }

    public static float getDeltaX() { return deltaX; }
    public static float getDeltaY() { return deltaY; }
    public static float getX() { return x; }
    public static float getY() { return y; }

    public static float getScrollY() {
        return scrollY;
    }

    public static void setScrollY(float scrollY) {
        Mouse.scrollY = scrollY;
    }
}