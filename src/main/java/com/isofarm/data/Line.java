package com.isofarm.data;

import java.util.function.Consumer;

public class Line {
    private String text;
    private Consumer<Line> action;

    public Line(String text) {
        this(text, null);
    }

    public Line(String text, Consumer<Line> action) {
        this.text = text;
        this.action = action;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isInteractive() {
        return action != null;
    }

    public void setClick(Consumer<Line> action) {
        this.action = action;
    }

    public void click() {
        if (action != null) {
            action.accept(this);
        }
    }
}