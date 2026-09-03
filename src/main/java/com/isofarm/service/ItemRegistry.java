package com.isofarm.service;

import com.isofarm.data.MaterialID;
import com.isofarm.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Provides item registry behavior.
 */
public class ItemRegistry {
    private final Map<String, Supplier<Item>> items = new HashMap<>();
    private final Map<MaterialID, Supplier<Item>> materials = new HashMap<>();

    /**
     * Performs the register operation.
     * @param id the id value
     * @param factory the factory value
     */
    public void register(String id, Supplier<Item> factory) {
        items.put(id.toLowerCase(), factory);
    }

    /**
     * Returns create.
     * @param id the id value
     * @return the create result
     */
    public Item create(String id) {
        if (id == null) return null;
        Supplier<Item> factory = items.get(id.toLowerCase());
        if (factory == null) return null;
        return factory.get();
    }

    /**
     * Performs the register operation.
     * @param materialID the material id value
     * @param factory the factory value
     */
    public void register(MaterialID materialID,
                         Supplier<Item> factory) {
        if (materialID == null || factory == null) return;
        materials.put(materialID, factory);
    }

    /**
     * Returns create.
     * @param materialID the material id value
     * @return the create result
     */
    public Item create(MaterialID materialID) {
        if (materialID == null) return null;
        Supplier<Item> factory = materials.get(materialID);
        if (factory == null) return null;
        return factory.get();
    }

    /**
     * Performs the contains operation.
     * @param materialID the material id value
     * @return the contains result
     */
    public boolean contains(MaterialID materialID) {
        return materialID != null && materials.containsKey(materialID);
    }

    /**
     * Performs the unregister operation.
     * @param materialID the material id value
     */
    public void unregister(MaterialID materialID) {
        if (materialID != null) {
            materials.remove(materialID);
        }
    }

    /**
     * Clears the materials.
     */
    public void clearMaterials() {
        materials.clear();
    }

    /**
     * Removes clear.
     */
    public void clear() {
        items.clear();
        materials.clear();
    }

    /**
     * Performs the size operation.
     * @return the size result
     */
    public int size() {
        return items.size();
    }

    /**
     * Performs the material size operation.
     * @return the material size result
     */
    public int materialSize() {
        return materials.size();
    }

    /**
     * Returns the ids.
     * @return the ids
     */
    public List<String> getIds() {
        return items.keySet()
                .stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * Returns the material ids.
     * @return the material ids
     */
    public List<String> getMaterialIds() {
        return materials.keySet()
                .stream()
                .map(Enum::name)
                .toList();
    }
}