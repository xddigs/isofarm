package com.isofarm.data;

import com.isofarm.utils.K;

/**
 * Provides toast behavior.
 */
@DataClass
public class Toast {
    private final ToastData type;
    private final String message;

    private float x;
    private float y;

    private float targetX;
    private float targetY;

    private final float duration;

    private float elapsed;
    private boolean exiting;

    private float windowWidth;

    /**
     * Creates a new {@code Toast} instance.
     * @param startX the start x value
     * @param startY the start y value
     * @param targetX the target x value
     * @param targetY the target y value
     * @param type the type value
     * @param message the message value
     * @param duration the duration value
     */
    public Toast(float startX, float startY, float targetX, float targetY,
                 ToastData type, String message, float duration) {
        this.x = startX;
        this.y = startY;

        this.targetX = targetX;
        this.targetY = targetY;

        this.type = type;
        this.message = message;
        this.duration = duration;

        this.elapsed = 0.0f;
        this.exiting = false;

        this.windowWidth = K.Window.DEFAULT_WIDTH;
    }

    /**
     * Sets the window width.
     * @param windowWidth the window width value
     */
    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;

    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    public void update(float delta) {
        elapsed += delta;

        if (!exiting && elapsed >= duration) {
            exiting = true;
            targetX = windowWidth + K.UI.TOAST_WIDTH;
        }

        float speed = exiting ? K.UI.TOAST_EXIT_SPEED
                : K.UI.TOAST_SLIDE_SPEED;

        float factor = 1.0f - (float) Math.exp(-speed * delta);
        x += (targetX - x) * factor;
        y += (targetY - y) * factor;
    }

    /**
     * Checks whether the finished condition is met.
     * @return {@code true} if finished; otherwise {@code false}
     */
    public boolean isFinished() {
        if (!exiting) {
            return false;
        }

        return x >= targetX - 2.0f;
    }

    /**
     * Returns the type.
     * @return the type
     */
    public ToastData getType() {
        return type;
    }

    /**
     * Returns the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the x.
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the y.
     * @return the y
     */
    public float getY() {
        return y;
    }

    /**
     * Returns the target x.
     * @return the target x
     */
    public float getTargetX() {
        return targetX;
    }

    /**
     * Returns the target y.
     * @return the target y
     */
    public float getTargetY() {
        return targetY;
    }

    /**
     * Returns the duration.
     * @return the duration
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Returns the elapsed.
     * @return the elapsed
     */
    public float getElapsed() {
        return elapsed;
    }

    /**
     * Checks whether the exiting condition is met.
     * @return {@code true} if exiting; otherwise {@code false}
     */
    public boolean isExiting() {
        return exiting;
    }

    /**
     * Sets the target x.
     * @param targetX the target x value
     */
    public void setTargetX(float targetX) {
        this.targetX = targetX;
    }

    /**
     * Sets the target y.
     * @param targetY the target y value
     */
    public void setTargetY(float targetY) {
        this.targetY = targetY;
    }
}