package com.tilled.data;

import com.tilled.utils.K;

import java.util.*;

@DataClass
public class Inventory {
    private final List<InventorySlot> slots;

    public Inventory() {
        this.slots = new ArrayList<>();

        for (int i = 0; i < K.UI.INVENTORY_SLOTS; i++) {
            this.slots.add(new InventorySlot());
        }
    }

    public Map<Item, Integer> getItems() {
        Map<Item, Integer> result = new LinkedHashMap<>();

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                result.merge(
                        slot.getItem(),
                        slot.getItem().getAmount(),
                        Integer::sum
                );
            }
        }

        return Collections.unmodifiableMap(result);
    }

    public void add(Item item, int amount) {
        if (item == null || amount <= 0) {
            return;
        }

        int remaining = amount;

        for (InventorySlot slot : slots) {
            if (remaining <= 0) {
                break;
            }

            if (slot.isEmpty() || !slot.getItem().equals(item)) {
                continue;
            }

            Item stack = slot.getItem();
            int space = K.World.MAX_STACK - stack.getAmount();

            if (space <= 0) {
                continue;
            }

            int added = Math.min(remaining, space);
            stack.addAmount(added);
            remaining -= added;
        }

        while (remaining > 0) {
            InventorySlot emptySlot = null;

            for (InventorySlot slot : slots) {
                if (slot.isEmpty()) {
                    emptySlot = slot;
                    break;
                }
            }

            if (emptySlot == null) {
                break;
            }

            int added = Math.min(remaining, K.World.MAX_STACK);

            Item stack = item;

            if (added < remaining) {
                try {
                    stack = item.getClass().getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    stack = item;
                }
            }

            stack.setAmount(added);
            emptySlot.setItem(stack);
            remaining -= added;
        }
    }

    public void remove(Item item, int amount) {
        if (item == null || amount <= 0) {
            return;
        }

        int remaining = amount;

        for (InventorySlot slot : slots) {
            if (remaining <= 0) {
                break;
            }

            if (slot.isEmpty() || !slot.getItem().equals(item)) {
                continue;
            }

            Item stack = slot.getItem();
            int current = stack.getAmount();

            if (current <= remaining) {
                remaining -= current;
                slot.clear();
            } else {
                stack.setAmount(current - remaining);
                remaining = 0;
            }
        }
    }

    public void pickAndDrop(int fromIndex, int toIndex) {
        if (!isValidIndex(fromIndex) ||
                !isValidIndex(toIndex) ||
                fromIndex == toIndex) {
            return;
        }

        InventorySlot from = slots.get(fromIndex);
        InventorySlot to = slots.get(toIndex);

        if (from.isEmpty()) {
            return;
        }

        if (to.isEmpty()) {
            to.setItem(from.getItem());
            from.clear();
            return;
        }

        if (to.getItem().equals(from.getItem())) {
            int space = K.World.MAX_STACK - to.getItem().getAmount();

            if (space > 0) {
                int moved = Math.min(space, from.getItem().getAmount());

                to.getItem().addAmount(moved);
                from.getItem().addAmount(-moved);

                if (from.getItem().getAmount() <= 0) {
                    from.clear();
                }

                return;
            }
        }

        Item temp = from.getItem();
        from.setItem(to.getItem());
        to.setItem(temp);
    }

    public void sort() {
        group();

        List<Item> items = new ArrayList<>();
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                items.add(slot.getItem());
                slot.clear();
            }
        }

        items.sort(Comparator
                .comparing((Item item) -> item.getClass().getSimpleName())
                .thenComparing(Item::getName, Comparator.nullsLast(String::compareTo))
                .thenComparingInt(Item::getAmount).reversed());

        for (int i = 0; i < items.size() && i < slots.size(); i++) {
            slots.get(i).setItem(items.get(i));
        }
    }

    public void group() {
        for (int i = 0; i < slots.size(); i++) {
            InventorySlot currentSlot = slots.get(i);
            if (currentSlot.isEmpty()) continue;

            Item currentItem = currentSlot.getItem();

            for (int j = i + 1; j < slots.size(); j++) {
                InventorySlot targetSlot = slots.get(j);
                if (targetSlot.isEmpty()) continue;

                Item targetItem = targetSlot.getItem();

                if (isSameType(currentItem, targetItem)) {
                    int spaceLeft = K.World.MAX_STACK - currentItem.getAmount();
                    if (spaceLeft > 0) {
                        int transfer = Math.min(spaceLeft, targetItem.getAmount());
                        currentItem.setAmount(currentItem.getAmount() + transfer);
                        targetItem.setAmount(targetItem.getAmount() - transfer);

                        if (targetItem.getAmount() <= 0) {
                            targetSlot.clear();
                        }
                    }
                }
            }
        }
    }

    public int take(int index, int amount) {
        if (!isValidIndex(index) || amount <= 0) {
            return 0;
        }

        InventorySlot slot = slots.get(index);

        if (slot.isEmpty()) {
            return 0;
        }

        Item item = slot.getItem();
        int taken = Math.min(amount, item.getAmount());

        item.addAmount(-taken);

        if (item.getAmount() <= 0) {
            slot.clear();
        }

        return taken;
    }

    public int addToStack(int targetIndex, Item item, int amount) {
        if (!isValidIndex(targetIndex) || item == null || amount <= 0) {
            return 0;
        }

        InventorySlot target = slots.get(targetIndex);

        if (target.isEmpty()) {
            int added = Math.min(amount, K.World.MAX_STACK);
            item.setAmount(added);
            target.setItem(item);
            return added;
        }

        Item targetItem = target.getItem();

        if (!targetItem.equals(item)) {
            return 0;
        }

        int space = K.World.MAX_STACK - targetItem.getAmount();

        if (space <= 0) {
            return 0;
        }

        int added = Math.min(amount, space);
        targetItem.addAmount(added);

        return added;
    }

    public int addOne(int targetIndex, Item item) {
        return addToStack(targetIndex, item, 1);
    }

    public List<Item> getHotbarItems() {
        List<Item> hotbar = new ArrayList<>();

        for (InventorySlot slot : slots) {
            if (slot.isEmpty()) {
                continue;
            }

            hotbar.add(slot.getItem());

            if (hotbar.size() >= K.UI.INVENTORY_COLUMNS) {
                break;
            }
        }

        return hotbar;
    }

    public void clear() {
        for (InventorySlot slot : slots) {
            slot.clear();
        }
    }

    public boolean isEmpty() {
        return slots.stream().allMatch(InventorySlot::isEmpty);
    }

    public int size() {
        return (int) slots.stream()
                .filter(slot -> !slot.isEmpty())
                .count();
    }

    public Item get(int index) {
        InventorySlot slot = getSlot(index);

        if (slot.isEmpty()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return slot.getItem();
    }

    public Item get(Item item) {
        if (item == null) {
            return null;
        }

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() &&
                    slot.getItem().equals(item)) {
                return slot.getItem();
            }
        }

        return null;
    }

    public int getAmount(Item item) {
        if (item == null) {
            return 0;
        }

        int amount = 0;

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() &&
                    slot.getItem().equals(item)) {
                amount += slot.getItem().getAmount();
            }
        }

        return amount;
    }

    public <T extends Item> boolean hasItemOfType(Class<T> type) {
        return slots.stream()
                .filter(slot -> !slot.isEmpty())
                .anyMatch(slot ->
                        type.isInstance(slot.getItem()) &&
                                slot.getItem().getAmount() > 0
                );
    }

    public <T extends Item> Optional<T> getItemOfType(Class<T> type) {
        return slots.stream()
                .filter(slot -> !slot.isEmpty())
                .filter(slot ->
                        type.isInstance(slot.getItem()) &&
                                slot.getItem().getAmount() > 0
                )
                .map(slot -> type.cast(slot.getItem()))
                .findFirst();
    }

    public <T extends Item> Optional<Byte> getFirstItemIdOfType(Class<T> type) {
        return slots.stream()
                .filter(slot -> !slot.isEmpty())
                .filter(slot ->
                        type.isInstance(slot.getItem()) &&
                                slot.getItem().getAmount() > 0
                )
                .map(slot -> slot.getItem().getId())
                .findFirst();
    }

    public <T extends Item> boolean hasItemWithId(Class<T> type, byte id) {
        return slots.stream()
                .filter(slot -> !slot.isEmpty())
                .anyMatch(slot ->
                        type.isInstance(slot.getItem()) &&
                                slot.getItem().getId() == id &&
                                slot.getItem().getAmount() > 0
                );
    }

    public <T extends Item> int getTotalAmountOfType(Class<T> type) {
        return slots.stream()
                .filter(slot -> !slot.isEmpty())
                .filter(slot -> type.isInstance(slot.getItem()))
                .mapToInt(slot -> slot.getItem().getAmount())
                .sum();
    }

    public List<InventorySlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public InventorySlot getSlot(int index) {
        if (index < 0 || index >= slots.size()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + slots.size()
            );
        }

        return slots.get(index);
    }

    private boolean isSameType(Item a, Item b) {
        if (a == null || b == null) return false;
        if (a.getClass() != b.getClass()) return false;

        if (a instanceof Seed s1 && b instanceof Seed s2) return s1.getType() == s2.getType();
        if (a instanceof Crop c1 && b instanceof Crop c2) return c1.getCropType() == c2.getCropType();
        if (a instanceof Block b1 && b instanceof Block b2) return b1.getType() == b2.getType();
        if (a instanceof Tool t1 && b instanceof Tool t2) return t1.getId() == t2.getId();

        return a.getName().equals(b.getName());
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < slots.size();
    }
}