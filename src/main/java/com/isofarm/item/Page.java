package com.isofarm.item;

import com.isofarm.data.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Page {
    private final List<Line> lines;

    public Page() {
        this.lines = new ArrayList<>();
    }

    public List<Line> getLines() {
        return lines;
    }

    public void addLine(String line) {
        lines.add(new Line(line));
    }

    public void addLine(String line, Consumer<Line> action) {
        lines.add(new Line(line, action));
    }

    public Line getLine(int index) {
        return lines.get(index);
    }

    public void setLine(int index, String line) {
        lines.set(index, new Line(line));
    }

    public void insertLine(int index, String line) {
        lines.add(index, new Line(line));
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

    public void forEachLine(Consumer<Line> consumer) {
        lines.forEach(consumer);
    }
}