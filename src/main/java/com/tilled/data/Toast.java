package com.tilled.data;

import com.tilled.utils.K;

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

    public Toast(
            float startX,
            float startY,
            float targetX,
            float targetY,
            ToastData type,
            String message,
            float duration
    ) {
        this.x = startX;
        this.y = startY;

        this.targetX = targetX;
        this.targetY = targetY;

        this.type = type;
        this.message = message;
        this.duration = duration;

        this.elapsed = 0.0f;
        this.exiting = false;
    }

    public void update(float delta) {
        elapsed += delta;

        if (!exiting && elapsed >= duration) {
            exiting = true;

            targetX = K.Window.DEFAULT_WIDTH +
                    K.UI.TOAST_WIDTH +
                    50.0f;
        }

        float speed = exiting
                ? K.UI.TOAST_EXIT_SPEED
                : K.UI.TOAST_SLIDE_SPEED;

        float factor = 1.0f - (float) Math.exp(-speed * delta);

        x += (targetX - x) * factor;
        y += (targetY - y) * factor;
    }

    public boolean isFinished() {
        if (!exiting) {
            return false;
        }

        return x >= targetX - 2.0f;
    }

    public ToastData getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public float getDuration() {
        return duration;
    }

    public float getElapsed() {
        return elapsed;
    }

    public boolean isExiting() {
        return exiting;
    }

    public void setTargetX(float targetX) {
        this.targetX = targetX;
    }

    public void setTargetY(float targetY) {
        this.targetY = targetY;
    }
}