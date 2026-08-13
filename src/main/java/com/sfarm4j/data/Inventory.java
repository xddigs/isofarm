package com.sfarm4j.data;

import java.util.LinkedList;
import java.util.List;

public class Inventory {
    private final List<Item> items;

    public Inventory() {
        this.items = new LinkedList<>();
    }

    public List<Item> getItems() {
        return items;
    }

    public void add(Item item) {
        items.add(item);
    }

    public void remove(Item item) {
        items.remove(item);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public Item get(int index) {
        return items.get(index);
    }

    public Item get(Item item) {
        return items.stream().filter(i -> i.equals(item)).findFirst()
                .orElse(null);
    }

    public int getAmount(Item item) {
        return items.stream().filter(i -> i.equals(item)).mapToInt(
                Item::getAmount).sum();
    }
}
