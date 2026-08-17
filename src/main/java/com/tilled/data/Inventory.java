package com.tilled.data;

import com.tilled.utils.K;

import java.util.*;

@DataClass
public class Inventory {
    private final List<InventorySlot> slots;

    public Inventory() {
        this.slots = new ArrayList<>();

        for (int i = 0; i < K.World.TOTAL_SLOTS; i++) {
            this.slots.add(new InventorySlot());
        }
    }

    public Map<Item, Integer> getItems() {
        Map<Item, Integer> result = new LinkedHashMap<>();
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                result.put(slot.getItem(), slot.getItem().getAmount());
            }
        }

        return Collections.unmodifiableMap(result);
    }

    public void add(Item item, int amount) {
        if (item == null || amount <= 0) return;

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItem().equals(item)) {
                int current = slot.getItem().getAmount();
                long newAmount = (long) current + amount;
                slot.getItem().setAmount((int) Math.min(newAmount, K.World.MAX_STACK));
                return;
            }
        }

        for (InventorySlot slot : slots) {
            if (slot.isEmpty()) {
                slot.setItem(item);
                slot.getItem().setAmount(Math.min(amount, K.World.MAX_STACK));
                return;
            }
        }
    }


    public void remove(Item item, int amount) {
        if (item == null || amount <= 0) return;

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItem().equals(item)) {
                int current = slot.getItem().getAmount();

                if (current <= amount) {
                    slot.clear();
                } else {
                    slot.getItem().setAmount(current - amount);
                }
                return;
            }
        }
    }

    public void pickAndDrop(int fromIndex, int toIndex) {
        if (!isValidIndex(fromIndex) || !isValidIndex(toIndex) ||
                fromIndex == toIndex) {
            return;
        }

        InventorySlot from = slots.get(fromIndex);
        InventorySlot to = slots.get(toIndex);
        if (from.isEmpty()) return;
        Item temp = from.getItem();
        from.setItem(to.getItem());
        to.setItem(temp);
    }


    public void sort() {
        slots.sort((a, b) -> {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;

            Item first = a.getItem();
            Item second = b.getItem();

            boolean aIsCoin = first instanceof Coin;
            boolean bIsCoin = second instanceof Coin;

            if (aIsCoin && !bIsCoin) return 1;
            if (!aIsCoin && bIsCoin) return -1;

            return String.CASE_INSENSITIVE_ORDER.compare(first.getName(), second.getName());
        });
    }

    public List<Item> getHotbarItems() {
        List<Item> hotbar = new ArrayList<>();

        for (InventorySlot slot : slots) {
            if (slot.isEmpty()) continue;

            hotbar.add(slot.getItem());

            if (hotbar.size() >= K.UI.HOTBAR_SLOTS) {
                break;
            }
        }

        return hotbar;
    }

    public void clear() {
        slots.clear();
    }

    public boolean isEmpty() {
        return slots.stream().allMatch(InventorySlot::isEmpty);
    }

    public int size() {
        return (int) slots.stream().filter(slot -> !slot.isEmpty()).count();
    }

    public Item get(int index) {
        InventorySlot slot = getSlot(index);

        if (slot.isEmpty()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return slot.getItem();
    }

    public Item get(Item item) {
        if (item == null) return null;

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItem().equals(item)) {
                return slot.getItem();
            }
        }

        return null;
    }

    public int getAmount(Item item) {
        if (item == null) return 0;

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItem().equals(item)) {
                return slot.getItem().getAmount();
            }
        }

        return 0;
    }

    public <T extends Item> boolean hasItemOfType(Class<T> type) {
        return slots.stream().filter(slot -> !slot.isEmpty())
                .anyMatch(slot -> type.isInstance(slot.getItem())
                        && slot.getItem().getAmount() > 0);
    }

    public <T extends Item> Optional<T> getItemOfType(Class<T> type) {
        return slots.stream().filter(slot -> !slot.isEmpty())
                .filter(slot -> type.isInstance(slot.getItem())
                && slot.getItem().getAmount() > 0)
                .map(slot -> type.cast(slot.getItem())).findFirst();
    }

    public <T extends Item> Optional<Byte> getFirstItemIdOfType(Class<T> type) {
        return slots.stream().filter(slot -> !slot.isEmpty())
                .filter(slot -> type.isInstance(slot.getItem())
                && slot.getItem().getAmount() > 0)
                .map(slot -> slot.getItem().getId()).findFirst();
    }

    public <T extends Item> boolean hasItemWithId(Class<T> type, byte id) {
        return slots.stream().filter(slot -> !slot.isEmpty())
                .anyMatch(slot -> type.isInstance(slot.getItem()) && slot.getItem()
                        .getId() == id && slot.getItem().getAmount() > 0);
    }

    public <T extends Item> int getTotalAmountOfType(Class<T> type) {
        return slots.stream().filter(slot -> !slot.isEmpty())
                .filter(slot -> type.isInstance(slot.getItem()))
                .mapToInt(slot -> slot.getItem().getAmount()).sum();
    }

    public List<InventorySlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public InventorySlot getSlot(int index) {
        if (index < 0 || index >= slots.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + slots.size());
        }

        return slots.get(index);
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < slots.size();
    }
}
