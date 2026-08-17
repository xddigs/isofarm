package com.tilled.data;

@DataClass
public class InventorySlot {
    private Item item;

    public InventorySlot() {
        this.item = null;
    }

    public InventorySlot(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public boolean isEmpty() {
        return item == null || item.getAmount() <= 0;
    }

    public void clear() {
        item = null;
    }
}