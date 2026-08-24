package com.isofarm.service;

import com.isofarm.data.Toast;
import com.isofarm.data.ToastData;
import com.isofarm.utils.K;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ToastService implements Service<Toast> {
    private final List<Toast> toasts;
    private float windowWidth = K.Window.DEFAULT_WIDTH;

    public ToastService() {
        this.toasts = new ArrayList<>();
    }

    public List<Toast> getToasts() {
        return toasts;
    }

    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
        for (Toast toast : toasts) {
            toast.setWindowWidth(windowWidth);
        }
        rearrange();
    }

    public void add(Toast toast) {
        if (toast == null) {
            return;
        }

        toasts.add(toast);
        rearrange();
    }

    public void remove(Toast toast) {
        if (toasts.remove(toast)) {
            rearrange();
        }
    }

    public Toast get(int index) {
        return toasts.get(index);
    }

    public void clear() {
        toasts.clear();
    }

    public int size() {
        return toasts.size();
    }

    public boolean isEmpty() {
        return toasts.isEmpty();
    }

    public void update(float delta) {
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

    public void info(String message) {
        create(ToastData.INFO, message);
    }

    public void success(String message) {
        create(ToastData.SUCCESS, message);
    }

    public void warning(String message) {
        create(ToastData.WARNING, message);
    }

    public void error(String message) {
        create(ToastData.ERROR, message);
    }

    public void reward(String message) {
        create(ToastData.REWARD, message);
    }

    public void purchase(String message) {
        create(ToastData.PURCHASE, message);
    }

    public void sell(String message) {
        create(ToastData.SELL, message);
    }

    private void create(ToastData type, String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        float startX = windowWidth + 500f;
        float targetX = windowWidth - K.UI.TOAST_WIDTH;
        float targetY = K.UI.TOAST_MARGIN_TOP;

        Toast toast = new Toast(startX, targetY, targetX, targetY,
                type, message, K.UI.TOAST_DURATION);

        toast.setWindowWidth(windowWidth);
        add(toast);
    }

    private void rearrange() {
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
}