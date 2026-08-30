package com.isofarm.item;

import com.isofarm.data.BookLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Page {
    private final List<BookLine> lines;

    public Page() {
        this.lines = new ArrayList<>();
    }

    public List<BookLine> getLines() {
        return lines;
    }

    public void addLine(String line) {
        lines.add(new BookLine(line));
    }

    public void addLine(String line, Consumer<BookLine> action) {
        lines.add(new BookLine(line, action));
    }

    public BookLine getLine(int index) {
        return lines.get(index);
    }

    public void setLine(int index, String line) {
        lines.set(index, new BookLine(line));
    }

    public void insertLine(int index, String line) {
        lines.add(index, new BookLine(line));
    }

    public void removeLine(int index) {
        lines.remove(index);
    }

    public void clear() {
        lines.clear();
    }

    public int size() {
        return lines.size();
    }

    public void forEachLine(Consumer<BookLine> consumer) {
        lines.forEach(consumer);
    }
}