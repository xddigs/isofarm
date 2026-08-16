package com.tilled.data;

import com.tilled.utils.K;

import java.util.*;
import java.util.stream.Collectors;

@DataClass
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
        int current = items.getOrDefault(item, 0);
        long newAmount = (long) current + amount;
        int cappedAmount = (int) Math.min(newAmount, K.World.MAX_STACK);
        items.put(item, cappedAmount);
        sort();
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

    public void sort() {
        Map<Item, Integer> sorted = items.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((a, b) -> {
                    boolean aIsCoin = a instanceof Coin;
                    boolean bIsCoin = b instanceof Coin;
                    if (aIsCoin && !bIsCoin) return 1;
                    if (!aIsCoin && bIsCoin) return -1;
                    return String.CASE_INSENSITIVE_ORDER
                            .compare(a.getName(), b.getName());
                })).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
        items.clear();
        items.putAll(sorted);
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
                .findFirst().orElse(null);
    }

    public int getAmount(Item item) {
        return items.getOrDefault(item, 0);
    }

    public <T extends Item> boolean hasItemOfType(Class<T> type) {
        return items.entrySet().stream().anyMatch(entry ->
                type.isInstance(entry.getKey()) && entry.getValue() > 0);
    }

    public <T extends Item> Optional<T> getItemOfType(Class<T> type) {
        return items.keySet().stream().filter(entry ->
                type.isInstance(entry) && items.get(entry) > 0)
                .map(type::cast).findFirst();
    }

    public <T extends Item> Optional<Byte> getFirstItemIdOfType(Class<T> type) {
        return items.entrySet().stream().filter(entry ->
                type.isInstance(entry.getKey()) && entry.getValue() > 0)
                .map(entry -> entry.getKey().getId()).findFirst();
    }

    public <T extends Item> boolean hasItemWithId(Class<T> type, byte id) {
        return items.entrySet().stream().anyMatch(entry ->
                type.isInstance(entry.getKey()) &&
                entry.getKey().getId() == id && entry.getValue() > 0);
    }

    public <T extends Item> int getTotalAmountOfType(Class<T> type) {
        return items.entrySet().stream().filter(entry ->
                type.isInstance(entry.getKey())).mapToInt(Map.Entry::getValue).sum();
    }
}