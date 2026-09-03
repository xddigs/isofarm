package com.isofarm.item;

import com.isofarm.data.BookLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides page behavior.
 */
public class Page {
    private final List<BookLine> bookLines;

    /**
     * Creates a new {@code Page} instance.
     */
    public Page() {
        this.bookLines = new ArrayList<>();
    }

    /**
     * Returns the lines.
     * @return the lines
     */
    public List<BookLine> getLines() {
        return bookLines;
    }

    /**
     * Adds the line.
     * @param line the line value
     */
    public void addLine(String line) {
        bookLines.add(new BookLine(line));
    }

    /**
     * Adds the line.
     * @param line the line value
     * @param action the action value
     */
    public void addLine(String line, Consumer<BookLine> action) {
        bookLines.add(new BookLine(line, action));
    }

    /**
     * Adds the line.
     * @param line the line value
     * @param action the action value
     * @param tooltipText the tooltip text value
     */
    public void addLine(String line, Consumer<BookLine> action, String tooltipText) {
        BookLine bookLine = new BookLine(line, action);
        bookLine.setTooltipText(tooltipText);
        bookLines.add(bookLine);
    }

    /**
     * Returns the line.
     * @param index the index value
     * @return the line
     */
    public BookLine getLine(int index) {
        return bookLines.get(index);
    }

    /**
     * Sets the line.
     * @param index the index value
     * @param line the line value
     */
    public void setLine(int index, String line) {
        bookLines.set(index, new BookLine(line));
    }

    /**
     * Performs the insert line operation.
     * @param index the index value
     * @param line the line value
     */
    public void insertLine(int index, String line) {
        bookLines.add(index, new BookLine(line));
    }

    /**
     * Removes the line.
     * @param index the index value
     */
    public void removeLine(int index) {
        bookLines.remove(index);
    }

    /**
     * Removes clear.
     */
    public void clear() {
        bookLines.clear();
    }

    /**
     * Performs the size operation.
     * @return the size result
     */
    public int size() {
        return bookLines.size();
    }

    /**
     * Performs the for each line operation.
     * @param consumer the consumer value
     */
    public void forEachLine(Consumer<BookLine> consumer) {
        bookLines.forEach(consumer);
    }
}