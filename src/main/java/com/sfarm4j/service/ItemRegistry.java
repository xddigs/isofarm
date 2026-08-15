package com.sfarm4j.service;

import com.sfarm4j.data.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemRegistry {
    private final Map<String, Supplier<Item>> items = new HashMap<>();

    public void register(String id, Supplier<Item> factory) {
        items.put(id.toLowerCase(), factory);
    }

    public Item create(String id) {
        if (id == null) {
            return null;
        }

        Supplier<Item> factory = items.get(id.toLowerCase());
        if (factory == null) {
            return null;
        }

        return factory.get();
    }

    public boolean contains(String id) {
        return id != null && items.containsKey(id.toLowerCase());
    }

    public void unregister(String id) {
        if (id != null) {
            items.remove(id.toLowerCase());
        }
    }

    public void clear() {
        items.clear();
    }

    public int size() {
        return items.size();
    }
}