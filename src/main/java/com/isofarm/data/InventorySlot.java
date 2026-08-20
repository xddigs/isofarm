package com.isofarm.data;

@DataClass
public class InventorySlot {
    private Item item;
    private int amount;

    public InventorySlot() {
        this.item = null;
        this.amount = 0;
    }

    public InventorySlot(Item item) {
        this(item, 1);
    }

    public InventorySlot(Item item, int amount) {
        if (item == null || amount <= 0) {
            this.item = null;
            this.amount = 0;
            return;
        }

        this.item = item;
        this.amount = amount;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;

        if (item == null) {
            this.amount = 0;
        } else if (this.amount <= 0) {
            this.amount = 1;
        }
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (item == null || amount <= 0) {
            clear();
            return;
        }

        this.amount = amount;
    }

    public void addAmount(int amount) {
        setAmount(this.amount + amount);
    }

    public boolean isEmpty() {
        return item == null || amount <= 0;
    }

    public void clear() {
        item = null;
        amount = 0;
    }
}