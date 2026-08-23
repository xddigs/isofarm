package com.isofarm.service;

import com.isofarm.data.MaterialID;
import com.isofarm.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemRegistry {
    private final Map<String, Supplier<Item>> items = new HashMap<>();
    private final Map<MaterialID, Supplier<Item>> materials = new HashMap<>();

    public void register(String id, Supplier<Item> factory) {
        items.put(id.toLowerCase(), factory);
    }

    public Item create(String id) {
        if (id == null) return null;
        Supplier<Item> factory = items.get(id.toLowerCase());
        if (factory == null) return null;
        return factory.get();
    }

    public void register(MaterialID materialID,
                         Supplier<Item> factory) {
        if (materialID == null || factory == null) return;
        materials.put(materialID, factory);
    }

    public Item create(MaterialID materialID) {
        if (materialID == null) return null;
        Supplier<Item> factory = materials.get(materialID);
        if (factory == null) return null;
        return factory.get();
    }

    public boolean contains(MaterialID materialID) {
        return materialID != null && materials.containsKey(materialID);
    }

    public void unregister(MaterialID materialID) {
        if (materialID != null) {
            materials.remove(materialID);
        }
    }

    public void clearMaterials() {
        materials.clear();
    }

    public void clear() {
        items.clear();
        materials.clear();
    }

    public int size() {
        return items.size();
    }

    public int materialSize() {
        return materials.size();
    }
}