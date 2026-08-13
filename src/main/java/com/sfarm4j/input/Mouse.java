package com.sfarm4j.input;

import static org.lwjgl.glfw.GLFW.*;

public class Mouse {
    private static final boolean[] buttons = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private static float x = 0, y = 0;
    private static float lastX = 0, lastY = 0;
    private static float deltaX = 0, deltaY = 0;
    private static boolean firstMouse = true;

    public static void init(long windowId) {
        glfwSetCursorPosCallback(windowId, (_,
                                            xpos, ypos) -> {
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

        glfwSetMouseButtonCallback(windowId, (_,
                                              button, action, _) -> {
            if (button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST) {
                buttons[button] = (action != GLFW_RELEASE);
            }
        });
    }

    public static void clearDeltas() {
        deltaX = 0;
        deltaY = 0;
    }

    public static boolean isButtonDown(int button) {
        return button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST && buttons[button];
    }

    public static float getDeltaX() { return deltaX; }
    public static float getDeltaY() { return deltaY; }
    public static float getX() { return x; }
    public static float getY() { return y; }
}