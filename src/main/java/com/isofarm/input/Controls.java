package com.isofarm.input;

/**
 * Queries a logical action across keyboard, mouse and the active gamepad.
 */
public final class Controls {
    private Controls() {}

    public static boolean isPressed(ControlAction action) {
        return Keyboard.isKeyPressed(action)
                || Mouse.isButtonPressed(action)
                || Joystick.isButtonPressed(action);
    }

    public static boolean isDown(ControlAction action) {
        return Keyboard.isKeyDown(action)
                || Mouse.isButtonDown(action)
                || Joystick.isButtonDown(action);
    }

    public static boolean isReleased(ControlAction action) {
        return Keyboard.isKeyReleased(action)
                || Mouse.isButtonReleased(action)
                || Joystick.isButtonReleased(action);
    }

    public static float getAxis(ControlAction action) {
        return Joystick.getAxis(action);
    }
}
