package com.isofarm.item;

import com.isofarm.data.BookLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Page {
    private final List<BookLine> bookLines;

    public Page() {
        this.bookLines = new ArrayList<>();
    }

    public List<BookLine> getLines() {
        return bookLines;
    }

    public void addLine(String line) {
        bookLines.add(new BookLine(line));
    }

    public void addLine(String line, Consumer<BookLine> action) {
        bookLines.add(new BookLine(line, action));
    }

    public void addLine(String line, Consumer<BookLine> action, String tooltipText) {
        BookLine bookLine = new BookLine(line, action);
        bookLine.setTooltipText(tooltipText);
        bookLines.add(bookLine);
    }

    public BookLine getLine(int index) {
        return bookLines.get(index);
    }

    public void setLine(int index, String line) {
        bookLines.set(index, new BookLine(line));
    }

    public void insertLine(int index, String line) {
        bookLines.add(index, new BookLine(line));
    }

    public void removeLine(int index) {
        bookLines.remove(index);
    }

    public void clear() {
        bookLines.clear();
    }

    public int size() {
        return bookLines.size();
    }

    public void forEachLine(Consumer<BookLine> consumer) {
        bookLines.forEach(consumer);
    }
}