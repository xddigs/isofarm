package com.isofarm.input;

import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;

public final class Joystick {
    public static final int BUTTON_A = GLFW_GAMEPAD_BUTTON_A;
    public static final int BUTTON_B = GLFW_GAMEPAD_BUTTON_B;
    public static final int BUTTON_X = GLFW_GAMEPAD_BUTTON_X;
    public static final int BUTTON_Y = GLFW_GAMEPAD_BUTTON_Y;
    public static final int BUTTON_LEFT_BUMPER = GLFW_GAMEPAD_BUTTON_LEFT_BUMPER;
    public static final int BUTTON_RIGHT_BUMPER = GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER;
    public static final int BUTTON_BACK = GLFW_GAMEPAD_BUTTON_BACK;
    public static final int BUTTON_START = GLFW_GAMEPAD_BUTTON_START;
    public static final int BUTTON_GUIDE = GLFW_GAMEPAD_BUTTON_GUIDE;
    public static final int BUTTON_LEFT_THUMB = GLFW_GAMEPAD_BUTTON_LEFT_THUMB;
    public static final int BUTTON_RIGHT_THUMB = GLFW_GAMEPAD_BUTTON_RIGHT_THUMB;
    public static final int BUTTON_DPAD_UP = GLFW_GAMEPAD_BUTTON_DPAD_UP;
    public static final int BUTTON_DPAD_RIGHT = GLFW_GAMEPAD_BUTTON_DPAD_RIGHT;
    public static final int BUTTON_DPAD_DOWN = GLFW_GAMEPAD_BUTTON_DPAD_DOWN;
    public static final int BUTTON_DPAD_LEFT = GLFW_GAMEPAD_BUTTON_DPAD_LEFT;

    public static final int AXIS_LEFT_X = GLFW_GAMEPAD_AXIS_LEFT_X;
    public static final int AXIS_LEFT_Y = GLFW_GAMEPAD_AXIS_LEFT_Y;
    public static final int AXIS_RIGHT_X = GLFW_GAMEPAD_AXIS_RIGHT_X;
    public static final int AXIS_RIGHT_Y = GLFW_GAMEPAD_AXIS_RIGHT_Y;
    public static final int AXIS_LEFT_TRIGGER = GLFW_GAMEPAD_AXIS_LEFT_TRIGGER;
    public static final int AXIS_RIGHT_TRIGGER = GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER;

    private static final int JOYSTICK_COUNT = GLFW_JOYSTICK_LAST + 1;
    private static final int BUTTON_COUNT = GLFW_GAMEPAD_BUTTON_LAST + 1;
    private static final int AXIS_COUNT = GLFW_GAMEPAD_AXIS_LAST + 1;

    private static final boolean[][] buttons = new boolean[JOYSTICK_COUNT][BUTTON_COUNT];
    private static final boolean[][] lastButtons = new boolean[JOYSTICK_COUNT][BUTTON_COUNT];
    private static final float[][] axes = new float[JOYSTICK_COUNT][AXIS_COUNT];
    private static final boolean[] connected = new boolean[JOYSTICK_COUNT];
    private static int activeJoystick = -1;

    private Joystick() {}

    public static void init() {
        poll();
        for (int joystick = 0; joystick < JOYSTICK_COUNT; joystick++) {
            System.arraycopy(buttons[joystick], 0, lastButtons[joystick], 0, BUTTON_COUNT);
        }
    }

    public static void update() {
        for (int joystick = 0; joystick < JOYSTICK_COUNT; joystick++) {
            System.arraycopy(buttons[joystick], 0, lastButtons[joystick], 0, BUTTON_COUNT);
        }
        poll();
    }

    private static void poll() {
        activeJoystick = -1;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            GLFWGamepadState state = GLFWGamepadState.malloc(stack);

            for (int joystick = GLFW_JOYSTICK_1; joystick <= GLFW_JOYSTICK_LAST; joystick++) {
                boolean available = glfwJoystickPresent(joystick) && glfwJoystickIsGamepad(joystick);
                connected[joystick] = available;
                clearCurrentState(joystick);

                if (!available || !glfwGetGamepadState(joystick, state)) continue;
                if (activeJoystick < 0) activeJoystick = joystick;

                ByteBuffer polledButtons = state.buttons();
                for (int button = 0; button < BUTTON_COUNT; button++) {
                    buttons[joystick][button] = polledButtons.get(button) == GLFW_PRESS;
                }

                FloatBuffer polledAxes = state.axes();
                for (int axis = 0; axis < AXIS_COUNT; axis++) {
                    axes[joystick][axis] = polledAxes.get(axis);
                }
            }
        }
    }

    private static void clearCurrentState(int joystick) {
        for (int button = 0; button < BUTTON_COUNT; button++) buttons[joystick][button] = false;
        for (int axis = 0; axis < AXIS_COUNT; axis++) axes[joystick][axis] = 0.0f;
    }

    public static boolean isButtonPressed(int button) {
        return isButtonPressed(activeJoystick, button);
    }

    public static boolean isButtonPressed(int joystick, int button) {
        return valid(joystick, button) && buttons[joystick][button] && !lastButtons[joystick][button];
    }

    public static boolean isButtonDown(int button) {
        return isButtonDown(activeJoystick, button);
    }

    public static boolean isButtonDown(int joystick, int button) {
        return valid(joystick, button) && buttons[joystick][button];
    }

    public static boolean isButtonReleased(int button) {
        return isButtonReleased(activeJoystick, button);
    }

    public static boolean isButtonReleased(int joystick, int button) {
        return valid(joystick, button) && !buttons[joystick][button] && lastButtons[joystick][button];
    }

    public static boolean isKeyPressed(int button) {
        return isButtonPressed(button);
    }

    public static boolean isKeyDown(int button) {
        return isButtonDown(button);
    }

    public static boolean isKeyReleased(int button) {
        return isButtonReleased(button);
    }

    public static float getAxis(int axis) {
        return getAxis(activeJoystick, axis);
    }

    public static float getAxis(int joystick, int axis) {
        if (joystick < 0 || joystick >= JOYSTICK_COUNT || axis < 0 || axis >= AXIS_COUNT) return 0.0f;
        return axes[joystick][axis];
    }

    public static float getAxis(int axis, float deadZone) {
        float value = getAxis(axis);
        return Math.abs(value) < Math.clamp(deadZone, 0.0f, 1.0f) ? 0.0f : value;
    }

    public static boolean isConnected() {
        return activeJoystick >= 0;
    }

    public static boolean isConnected(int joystick) {
        return joystick >= 0 && joystick < JOYSTICK_COUNT && connected[joystick];
    }

    public static int getActiveJoystick() {
        return activeJoystick;
    }

    public static String getName(int joystick) {
        return isConnected(joystick) ? glfwGetGamepadName(joystick) : null;
    }

    private static boolean valid(int joystick, int button) {
        return joystick >= 0 && joystick < JOYSTICK_COUNT && button >= 0 && button < BUTTON_COUNT;
    }
}
