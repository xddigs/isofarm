package com.isofarm.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Page {
    private final List<String> lines;

    public Page() {
        this.lines = new ArrayList<>();
    }

    public List<String> getLines() {
        return lines;
    }

    public void addLine(String line) {
        lines.add(line);
    }

    public String getLine(int index) {
        return lines.get(index);
    }

    public void setLine(int index, String line) {
        lines.set(index, line);
    }

    public void insertLine(int index, String line) {
        lines.add(index, line);
    }

    public void forEachLine(Consumer<String> consumer) {
        lines.forEach(consumer);
    }

    public void removeLine(int index) {
        lines.remove(index);
    }

    public void removeLine(String line) {
        lines.remove(line);
    }

    public int size() {
        return lines.size();
    }

    public void clear() {
        lines.clear();
    }
}
