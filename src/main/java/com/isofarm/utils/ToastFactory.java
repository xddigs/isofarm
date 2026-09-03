package com.isofarm.utils;

import com.isofarm.data.Toast;
import com.isofarm.data.ToastData;
import com.isofarm.service.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides toast factory behavior.
 */
@Utils
public class ToastFactory implements Service<Toast> {
    private static final List<Toast> toasts = new ArrayList<>();
    private static float windowWidth = K.Window.DEFAULT_WIDTH;

    /**
     * Returns the toasts.
     * @return the toasts
     */
    public static List<Toast> getToasts() {
        return toasts;
    }

    /**
     * Sets the window width.
     * @param windowWidth the window width value
     */
    public static void setWindowWidth(float windowWidth) {
        ToastFactory.windowWidth = windowWidth;
        for (Toast toast : toasts) {
            toast.setWindowWidth(windowWidth);
        }
        rearrange();
    }

    /**
     * Adds add.
     * @param toast the toast value
     */
    public static void add(Toast toast) {
        if (toast == null) {
            return;
        }

        toasts.add(toast);
        rearrange();
    }

    /**
     * Performs the reload operation.
     */
    public static void reload() {
        toasts.clear();
        rearrange();
    }

    /**
     * Removes remove.
     * @param toast the toast value
     */
    public void remove(Toast toast) {
        if (toasts.remove(toast)) {
            rearrange();
        }
    }

    /**
     * Returns get.
     * @param index the index value
     * @return the get result
     */
    public static Toast get(int index) {
        return toasts.get(index);
    }

    /**
     * Removes clear.
     */
    public static void clear() {
        toasts.clear();
    }

    /**
     * Performs the size operation.
     * @return the size result
     */
    public static int size() {
        return toasts.size();
    }

    /**
     * Checks whether the empty condition is met.
     * @return {@code true} if empty; otherwise {@code false}
     */
    public static boolean isEmpty() {
        return toasts.isEmpty();
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    public static void update(float delta) {
        if (toasts.isEmpty()) {
            return;
        }

        Iterator<Toast> iterator = toasts.iterator();
        while (iterator.hasNext()) {
            Toast toast = iterator.next();

            toast.update(delta);

            if (toast.isFinished()) {
                iterator.remove();
            }
        }

        rearrange();
    }

    /**
     * Performs the info operation.
     * @param message the message value
     */
    public static void info(String message) {
        create(ToastData.INFO, message);
    }

    /**
     * Performs the success operation.
     * @param message the message value
     */
    public static void success(String message) {
        create(ToastData.SUCCESS, message);
    }

    /**
     * Performs the warning operation.
     * @param message the message value
     */
    public static void warning(String message) {
        create(ToastData.WARNING, message);
    }

    /**
     * Performs the error operation.
     * @param message the message value
     */
    public static void error(String message) {
        create(ToastData.ERROR, message);
    }

    /**
     * Performs the reward operation.
     * @param message the message value
     */
    public static void reward(String message) {
        create(ToastData.REWARD, message);
    }

    /**
     * Performs the purchase operation.
     * @param message the message value
     */
    public static void purchase(String message) {
        create(ToastData.PURCHASE, message);
    }

    /**
     * Performs the sell operation.
     * @param message the message value
     */
    public static void sell(String message) {
        create(ToastData.SELL, message);
    }

    /**
     * Returns create.
     * @param type the type value
     * @param message the message value
     */
    private static void create(ToastData type, String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        float startX = windowWidth + 250f;
        float targetX = windowWidth - K.UI.TOAST_WIDTH;
        float targetY = K.UI.TOAST_MARGIN_TOP;

        Toast toast = new Toast(startX, targetY, targetX, targetY,
                type, Local.lang.t(message), K.UI.TOAST_DURATION);

        toast.setWindowWidth(windowWidth);
        add(toast);
    }

    /**
     * Performs the rearrange operation.
     */
    private static void rearrange() {
        float targetX = windowWidth - K.UI.TOAST_WIDTH;
        float y = K.UI.TOAST_MARGIN_TOP;

        for (Toast toast : toasts) {
            if (toast.isExiting()) {
                continue;
            }

            toast.setTargetX(targetX);
            toast.setTargetY(y);
            y += K.UI.TOAST_HEIGHT + K.UI.TOAST_SPACING;
        }
    }

    /**
     * Performs the on resize operation.
     * @param width the width value
     */
    public static void onResize(float width) {
        windowWidth = width;
    }
}