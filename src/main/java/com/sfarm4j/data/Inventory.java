package com.sfarm4j.data;

import java.util.*;

public class Inventory {
    private final Map<Item, Integer> items;

    public Inventory() {
        this.items = new LinkedHashMap<>();
    }

    public Map<Item, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public void add(Item item, int amount) {
        if (item == null || amount <= 0) return;
        items.merge(item, amount, Integer::sum);
    }

    public void remove(Item item, int amount) {
        if (item == null || amount <= 0) return;

        int current = items.getOrDefault(item, 0);
        if (current <= amount) {
            items.remove(item);
        } else {
            items.put(item, current - amount);
        }
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
        if (index < 0 || index >= items.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + items.size());
        }
        return new ArrayList<>(items.keySet()).get(index);
    }

    public Item get(Item item) {
        return items.keySet().stream()
                .filter(i -> i.equals(item))
                .findFirst()
                .orElse(null);
    }

    public int getAmount(Item item) {
        return items.getOrDefault(item, 0);
    }
}