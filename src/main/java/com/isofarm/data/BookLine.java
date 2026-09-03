package com.isofarm.data;

import java.util.function.Consumer;

/**
 * Provides book line behavior.
 */
public class BookLine {
    private String text;
    private Consumer<BookLine> action;
    private String tooltipText;

    /**
     * Creates a new {@code BookLine} instance.
     * @param text the text value
     */
    public BookLine(String text) {
        this(text, null);
    }

    /**
     * Creates a new {@code BookLine} instance.
     * @param text the text value
     * @param action the action value
     */
    public BookLine(String text, Consumer<BookLine> action) {
        this.text = text;
        this.action = action;
    }

    /**
     * Returns the text.
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text.
     * @param text the text value
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Checks whether the interactive condition is met.
     * @return {@code true} if interactive; otherwise {@code false}
     */
    public boolean isInteractive() {
        return action != null;
    }

    /**
     * Sets the click.
     * @param action the action value
     */
    public void setClick(Consumer<BookLine> action) {
        this.action = action;
    }

    /**
     * Performs the click operation.
     */
    public void click() {
        if (action != null) {
            action.accept(this);
        }
    }

    /**
     * Returns the tooltip text.
     * @return the tooltip text
     */
    public String getTooltipText() {
        return tooltipText;
    }

    /**
     * Sets the tooltip text.
     * @param tooltipText the tooltip text value
     * @return the set tooltip text result
     */
    public BookLine setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }
}