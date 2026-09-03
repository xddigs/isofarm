package com.isofarm.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Provides mouse behavior.
 */
public final class Mouse {
    public static final int BUTTON_1 = GLFW_MOUSE_BUTTON_1;
    public static final int BUTTON_2 = GLFW_MOUSE_BUTTON_2;
    public static final int BUTTON_3 = GLFW_MOUSE_BUTTON_3;
    public static final int BUTTON_4 = GLFW_MOUSE_BUTTON_4;
    public static final int BUTTON_5 = GLFW_MOUSE_BUTTON_5;
    public static final int BUTTON_6 = GLFW_MOUSE_BUTTON_6;
    public static final int BUTTON_7 = GLFW_MOUSE_BUTTON_7;
    public static final int BUTTON_8 = GLFW_MOUSE_BUTTON_8;
    public static final int BUTTON_LEFT = GLFW_MOUSE_BUTTON_LEFT;
    public static final int BUTTON_RIGHT = GLFW_MOUSE_BUTTON_RIGHT;
    public static final int BUTTON_MIDDLE = GLFW_MOUSE_BUTTON_MIDDLE;
    public static final int BUTTON_LAST = GLFW_MOUSE_BUTTON_LAST;

    private static final boolean[] buttons = new boolean[BUTTON_LAST + 1];
    private static final boolean[] lastButtons = new boolean[BUTTON_LAST + 1];

    private static float x = 0, y = 0;
    private static float lastX = 0, lastY = 0;
    private static float deltaX = 0, deltaY = 0;
    private static boolean firstMouse = true;
    private static float scrollY = 0.0f;

    /**
     * Creates a new private* {@code Mouse} instance.
     */
    private Mouse() {}

    /**
     * Initializes the component.
     * @param windowId the window id value
     */
    public static void init(long windowId) {
        glfwSetCursorPosCallback(windowId, (window, xpos, ypos) -> {
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

        glfwSetMouseButtonCallback(windowId, (window, button, action, scanner) -> {
            if (button >= 0 && button <= BUTTON_LAST) {
                buttons[button] = (action != GLFW_RELEASE);
            }
        });

        glfwSetScrollCallback(windowId, (window, xoffset, yoffset) -> {
            scrollY = (float) yoffset;
        });
    }

    /**
     * Updates the current state.
     */
    public static void update() {
        System.arraycopy(buttons, 0, lastButtons, 0, buttons.length);
        deltaX = 0;
        deltaY = 0;
        scrollY = 0.0f;
    }

    /**
     * Checks whether the button down condition is met.
     * @param button the button value
     * @return {@code true} if button down; otherwise {@code false}
     */
    public static boolean isButtonDown(int button) {
        return button >= 0 && button <= BUTTON_LAST && buttons[button];
    }

    /**
     * Checks whether the button pressed condition is met.
     * @param button the button value
     * @return {@code true} if button pressed; otherwise {@code false}
     */
    public static boolean isButtonPressed(int button) {
        return button >= 0 && button <= BUTTON_LAST
                && buttons[button] && !lastButtons[button];
    }

    /**
     * Checks whether the button released condition is met.
     * @param button the button value
     * @return {@code true} if button released; otherwise {@code false}
     */
    public static boolean isButtonReleased(int button) {
        return button >= 0 &&
                button <= BUTTON_LAST &&
                !buttons[button] &&
                lastButtons[button];
    }

    /**
     * Returns the delta x.
     * @return the delta x
     */
    public static float getDeltaX() { return deltaX; }
    /**
     * Returns the delta y.
     * @return the delta y
     */
    public static float getDeltaY() { return deltaY; }
    /**
     * Returns the x.
     * @return the x
     */
    public static float getX() { return x; }
    /**
     * Returns the y.
     * @return the y
     */
    public static float getY() { return y; }

    /**
     * Returns the scroll y.
     * @return the scroll y
     */
    public static float getScrollY() {
        return scrollY;
    }

    /**
     * Sets the scroll y.
     * @param scrollY the scroll y value
     */
    public static void setScrollY(float scrollY) {
        Mouse.scrollY = scrollY;
    }
}
