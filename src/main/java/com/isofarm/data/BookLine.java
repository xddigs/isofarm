package com.isofarm.data;

import java.util.function.Consumer;

public class BookLine {
    private String text;
    private Consumer<BookLine> action;
    private String tooltipText;

    public BookLine(String text) {
        this(text, null);
    }

    public BookLine(String text, Consumer<BookLine> action) {
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

    public void setClick(Consumer<BookLine> action) {
        this.action = action;
    }

    public void click() {
        if (action != null) {
            action.accept(this);
        }
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public BookLine setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }
}