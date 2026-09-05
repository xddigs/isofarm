package com.isofarm.data;

import com.isofarm.item.*;
import com.isofarm.service.SoundService;
import com.isofarm.utils.K;

import java.util.*;

/**
 * Provides inventory behavior.
 */
@DataClass
public class Inventory {
    private final List<InventorySlot> slots;
    private final List<InventorySlot> equippedExtraItems = new ArrayList<>();
    private final InventorySlot backpackSlot;
    private final InventorySlot bookSlot;

    /**
     * Creates a new {@code Inventory} instance.
     */
    public Inventory() {
        this.slots = new ArrayList<>();
        this.backpackSlot = new InventorySlot();
        this.bookSlot = new InventorySlot();

        for (int i = 0; i < K.UI.INVENTORY_SLOTS; i++) {
            this.slots.add(new InventorySlot());
        }
    }

    /**
     * Returns the items.
     * @return the items
     */
    public Map<Item, Integer> getItems() {
        Map<Item, Integer> result = new LinkedHashMap<>();

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                result.merge(slot.getItem(), slot.getAmount(), Integer::sum);
            }
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns the equipped extra items.
     * @return the equipped extra items
     */
    public List<InventorySlot> getEquippedExtraItems() {
        return equippedExtraItems;
    }

    /**
     * Returns the backpack slot.
     * @return the backpack slot
     */
    public InventorySlot getBackpackSlot() {
        return backpackSlot;
    }

    /**
     * Returns the book slot.
     * @return the book slot
     */
    public InventorySlot getBookSlot() {
        return bookSlot;
    }

    /**
     * Checks whether the backpack equipped condition is met.
     * @return {@code true} if backpack equipped; otherwise {@code false}
     */
    public boolean hasBackpackEquipped() {
        return !backpackSlot.isEmpty() && backpackSlot.getItem() instanceof Backpack;
    }

    /**
     * Checks whether the book equipped condition is met.
     * @return {@code true} if book equipped; otherwise {@code false}
     */
    public boolean hasBookEquipped() {
        return !bookSlot.isEmpty() && bookSlot.getItem() instanceof CraftingBook;
    }

    /**
     * Returns the backpack.
     * @return the backpack
     */
    public Backpack getBackpack() {
        return backpackSlot.getItem() instanceof Backpack backpack ? backpack : null;
    }

    /**
     * Returns the book.
     * @return the book
     */
    public CraftingBook getBook() {
        return bookSlot.getItem() instanceof CraftingBook book ? book : null;
    }

    /**
     * Performs the equip backpack operation.
     * @param backpack the backpack value
     */
    public void equipBackpack(Backpack backpack) {
        remove(backpack, 1);
        backpackSlot.setItem(backpack);
        equippedExtraItems.add(backpackSlot);
        SoundService.fx.playUseSound(SoundGroup.ITEMS);
    }

    /**
     * Performs the equip book operation.
     * @param book the book value
     */
    public void equipBook(Book book) {
        remove(book, 1);
        bookSlot.setItem(book);
        equippedExtraItems.add(bookSlot);
        SoundService.fx.playUseSound(SoundGroup.ITEMS);
    }

    /**
     * Performs the unequip backpack operation.
     */
    public void unequipBackpack() {
        Item backpack = backpackSlot.getItem();
        equippedExtraItems.remove(backpack);
        add(backpack, 1);
        backpackSlot.clear();
        SoundService.fx.playUseSound(SoundGroup.ITEMS);
    }

    /**
     * Performs the unequip book operation.
     */
    public void unequipBook() {
        Item book = bookSlot.getItem();
        equippedExtraItems.remove(book);
        add(book, 1);
        bookSlot.clear();
        SoundService.fx.playUseSound(SoundGroup.ITEMS);
    }

    /**
     * Adds add.
     * @param item the item value
     * @param amount the amount value
     * @return the add result
     */
    public int add(Item item, int amount) {
        if (item == null || amount <= 0) {
            return amount;
        }

        int remaining = amount;
        int hotbarStart = getHotbarStart();

        remaining = addToExistingStacks(item, remaining, hotbarStart, slots.size());
        remaining = addToEmptySlots(item, remaining, hotbarStart, slots.size());

        if (remaining > 0) {
            remaining = addToExistingStacks(item, remaining, 0, hotbarStart);
            remaining = addToEmptySlots(item, remaining, 0, hotbarStart);
        }

        return remaining;
    }

    /**
     * Adds the to existing stacks.
     * @param item the item value
     * @param amount the amount value
     * @param start the start value
     * @param end the end value
     * @return the add to existing stacks result
     */
    private int addToExistingStacks(Item item, int amount, int start, int end) {
        int remaining = amount;

        for (int i = start; i < end && remaining > 0; i++) {
            InventorySlot slot = slots.get(i);

            if (slot.isEmpty()) {
                continue;
            }

            if (!isSameType(slot.getItem(), item)) {
                continue;
            }

            int maxStack = getMaxStack(item);
            int space = maxStack - slot.getAmount();

            if (space <= 0) {
                continue;
            }

            int added = Math.min(remaining, space);
            slot.addAmount(added);
            remaining -= added;
        }

        return remaining;
    }

    /**
     * Adds the to empty slots.
     * @param item the item value
     * @param amount the amount value
     * @param start the start value
     * @param end the end value
     * @return the add to empty slots result
     */
    private int addToEmptySlots(Item item, int amount, int start, int end) {
        int remaining = amount;

        for (int i = start; i < end && remaining > 0; i++) {
            InventorySlot slot = slots.get(i);

            if (!slot.isEmpty()) {
                continue;
            }

            int added = Math.min(remaining, getMaxStack(item));

            slot.setItem(item);
            slot.setAmount(added);

            remaining -= added;
        }

        return remaining;
    }

    /**
     * Removes remove.
     * @param item the item value
     * @param amount the amount value
     */
    public void remove(Item item, int amount) {
        if (item == null || amount <= 0) {
            return;
        }

        int remaining = amount;
        for (InventorySlot slot : slots) {
            if (remaining <= 0) {
                break;
            }

            if (slot.isEmpty() || !isSameType(slot.getItem(), item)) {
                continue;
            }

            int current = slot.getAmount();
            if (current <= remaining) {
                remaining -= current;
                slot.clear();
            } else {
                slot.setAmount(current - remaining);
                remaining = 0;
            }
        }
    }

    /**
     * Performs the sort operation.
     */
    public void sort() {
        group();
        List<Stack> stacks = new ArrayList<>();
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                stacks.add(new Stack(slot.getItem(), slot.getAmount()));
                slot.clear();
            }
        }

        stacks.sort(Comparator.comparing((Stack stack) -> stack.item().getClass().getSimpleName())
                .thenComparing(stack -> stack.item().getName(), Comparator.nullsLast(String::compareTo))
                .thenComparingInt(Stack::amount).reversed());

        int index = 0;
        for (Stack stack : stacks) {
            int remaining = stack.amount();

            while (remaining > 0 && index < slots.size()) {
                int amount = Math.min(remaining, getMaxStack(stack.item()));

                InventorySlot slot = slots.get(index++);
                slot.setItem(stack.item());
                slot.setAmount(amount);

                remaining -= amount;
            }
        }
    }

    /**
     * Performs the group operation.
     */
    public void group() {
        for (int i = 0; i < slots.size(); i++) {
            InventorySlot currentSlot = slots.get(i);

            if (currentSlot.isEmpty()) {
                continue;
            }

            Item currentItem = currentSlot.getItem();

            for (int j = i + 1; j < slots.size(); j++) {
                InventorySlot targetSlot = slots.get(j);

                if (targetSlot.isEmpty()) {
                    continue;
                }

                if (!isSameType(currentItem, targetSlot.getItem())) {
                    continue;
                }

                int maxStack = getMaxStack(currentItem);
                int spaceLeft = maxStack - currentSlot.getAmount();

                if (spaceLeft <= 0) {
                    continue;
                }

                int transfer = Math.min(spaceLeft, targetSlot.getAmount());

                currentSlot.addAmount(transfer);
                targetSlot.setAmount(targetSlot.getAmount() - transfer);
            }
        }
    }

    /**
     * Performs the take operation.
     * @param index the index value
     * @param amount the amount value
     * @return the take result
     */
    public int take(int index, int amount) {
        if (!isValidIndex(index) || amount <= 0) {
            return 0;
        }

        InventorySlot slot = slots.get(index);

        if (slot.isEmpty()) {
            return 0;
        }

        int taken = Math.min(amount, slot.getAmount());

        slot.setAmount(slot.getAmount() - taken);

        return taken;
    }

    /**
     * Adds the to stack.
     * @param targetIndex the target index value
     * @param item the item value
     * @param amount the amount value
     * @return the add to stack result
     */
    public int addToStack(int targetIndex, Item item, int amount) {
        if (!isValidIndex(targetIndex) || item == null || amount <= 0) {
            return 0;
        }

        InventorySlot target = slots.get(targetIndex);

        if (target.isEmpty()) {
            int added = Math.min(amount, getMaxStack(item));

            target.setItem(item);
            target.setAmount(added);

            return added;
        }

        if (!isSameType(target.getItem(), item)) {
            return 0;
        }

        int space = getMaxStack(item) - target.getAmount();

        if (space <= 0) {
            return 0;
        }

        int added = Math.min(amount, space);
        target.addAmount(added);

        return added;
    }

    /**
     * Adds the one.
     * @param targetIndex the target index value
     * @param item the item value
     * @return the add one result
     */
    public int addOne(int targetIndex, Item item) {
        return addToStack(targetIndex, item, 1);
    }

    /**
     * Returns the hotbar items.
     * @return the hotbar items
     */
    public List<Item> getHotbarItems() {
        List<Item> hotbar = new ArrayList<>();

        int hotbarStart = getHotbarStart();

        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
            int index = hotbarStart + i;

            if (index >= slots.size()) {
                break;
            }

            InventorySlot slot = slots.get(index);

            if (!slot.isEmpty()) {
                hotbar.add(slot.getItem());
            }
        }

        return hotbar;
    }

    /**
     * Removes clear.
     */
    public void clear() {
        for (InventorySlot slot : slots) {
            slot.clear();
        }
    }

    /**
     * Checks whether the full condition is met.
     * @return {@code true} if full; otherwise {@code false}
     */
    public boolean isFull() {
        return slots.stream().noneMatch(InventorySlot::isEmpty);
    }

    /**
     * Checks whether the empty condition is met.
     * @return {@code true} if empty; otherwise {@code false}
     */
    public boolean isEmpty() {
        return slots.stream().allMatch(
                slot -> slot.isEmpty() || slot.getAmount() <= 0);
    }

    /**
     * Performs the size operation.
     * @return the size result
     */
    public int size() {
        return (int) slots.stream().filter(slot -> !slot.isEmpty()).count();
    }

    /**
     * Returns get.
     * @param index the index value
     * @return the get result
     */
    public Item get(int index) {
        InventorySlot slot = getSlot(index);

        if (slot.isEmpty()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return slot.getItem();
    }

    /**
     * Returns get.
     * @param item the item value
     * @return the get result
     */
    public Item get(Item item) {
        if (item == null) {
            return null;
        }

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && isSameType(slot.getItem(), item)) {
                return slot.getItem();
            }
        }

        return null;
    }

    /**
     * Returns the amount.
     * @param item the item value
     * @return the amount
     */
    public int getAmount(Item item) {
        if (item == null) {
            return 0;
        }

        int amount = 0;

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && isSameType(slot.getItem(), item)) {
                amount += slot.getAmount();
            }
        }

        return amount;
    }

    /**
     * Checks whether the item of type condition is met.
     * @param <T> the generic type
     * @param type the type value
     * @return {@code true} if item of type; otherwise {@code false}
     */
    public <T extends Item> boolean hasItemOfType(Class<T> type) {
        return slots.stream().filter(slot -> !slot.isEmpty()).anyMatch(
                slot -> type.isInstance(slot.getItem()) && slot.getAmount() > 0);
    }

    /**
     * Returns the item of type.
     * @param <T> the generic type
     * @param type the type value
     * @return the item of type
     */
    public <T extends Item> Optional<T> getItemOfType(Class<T> type) {
        return slots.stream().filter(slot -> !slot.isEmpty()).filter(
                slot -> type.isInstance(slot.getItem()) && slot.getAmount() > 0).map(
                        slot -> type.cast(slot.getItem())).findFirst();
    }

    /**
     * Returns the first item id of type.
     * @param <T> the generic type
     * @param type the type value
     * @return the first item id of type
     */
    public <T extends Item> Optional<Byte> getFirstItemIdOfType(Class<T> type) {
        return slots.stream().filter(slot -> !slot.isEmpty()).filter(
                slot -> type.isInstance(slot.getItem()) && slot.getAmount() > 0).map(
                        slot -> slot.getItem().getId()).findFirst();
    }

    /**
     * Checks whether the item with id condition is met.
     * @param <T> the generic type
     * @param type the type value
     * @param id the id value
     * @return {@code true} if item with id; otherwise {@code false}
     */
    public <T extends Item> boolean hasItemWithId(Class<T> type, byte id) {
        return slots.stream().filter(slot -> !slot.isEmpty()).anyMatch(
                slot -> type.isInstance(slot.getItem())
                        && slot.getItem().getId() == id && slot.getAmount() > 0);
    }

    /**
     * Returns the amount of material.
     * @param id the id value
     * @return the amount of material
     */
    public int getAmountOfMaterial(MaterialID id) {
        return slots.stream()
                .filter(slot -> !slot.isEmpty())
                .filter(slot -> slot.getItem()
                        instanceof Material mid && mid.getMaterialID() == id)
                .mapToInt(InventorySlot::getAmount)
                .sum();
    }

    /**
     * Returns the slots.
     * @return the slots
     */
    public List<InventorySlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    /**
     * Returns the slot.
     * @param index the index value
     * @return the slot
     */
    public InventorySlot getSlot(int index) {
        if (!isValidIndex(index)) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + slots.size());
        }

        return slots.get(index);
    }

    /**
     * Returns the slot amount.
     * @param index the index value
     * @return the slot amount
     */
    public int getSlotAmount(int index) {
        return getSlot(index).getAmount();
    }

    /**
     * Returns the hotbar start.
     * @return the hotbar start
     */
    public int getHotbarStart() {
        return (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
    }

    /**
     * Returns the max stack.
     * @param item the item value
     * @return the max stack
     */
    public int getMaxStack(Item item) {
        if (item == null) {
            return 0;
        }

        return switch (item) {
            case Tool ignored -> 1;
            case Usable usable -> switch (usable) {
                case Bucket bucket -> bucket.isFull() ? 1 : 16;
                default -> 1;
            };
            case Seed ignored -> K.World.MAX_STACK * 2;
            default -> K.World.MAX_STACK;
        };
    }

    /**
     * Checks whether the same type condition is met.
     * @param a the a value
     * @param b the b value
     * @return {@code true} if same type; otherwise {@code false}
     */
    private boolean isSameType(Item a, Item b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.getClass() != b.getClass()) {
            return false;
        }

        return switch (a) {
            case Seed s1 when b instanceof Seed s2 -> s1.getType() == s2.getType();
            case Crop c1 when b instanceof Crop c2 -> c1.getCropType() == c2.getCropType();
            case Block b1 when b instanceof Block b2 -> b1.getType() == b2.getType();
            case Tool t1 when b instanceof Tool t2 -> t1.getId() == t2.getId() && t1.getTier() == t2.getTier();
            default -> Objects.equals(a.getName(), b.getName());
        };

    }

    /**
     * Checks whether the valid index condition is met.
     * @param index the index value
     * @return {@code true} if valid index; otherwise {@code false}
     */
    private boolean isValidIndex(int index) {
        return index >= 0 && index < slots.size();
    }
}