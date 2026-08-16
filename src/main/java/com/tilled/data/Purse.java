package com.tilled.data;

@DataClass
public class Purse {
    private final Inventory inventory;
    private final Item coinItem;

    public Purse(Inventory inventory, Item coinItem) {
        this.inventory = inventory;
        this.coinItem = coinItem;
    }

    public int getBalance() {
        return inventory.getAmount(coinItem);
    }

    public void add(int amount) {
        if (amount <= 0) return;
        inventory.add(coinItem, amount);
    }

    public void remove(int amount) {
        if (amount <= 0 || getBalance() < amount) return;
        inventory.remove(coinItem, amount);
    }

    public void clear() {
        Item coin = inventory.get(coinItem);
        if (coin != null) {
            inventory.remove(coin, inventory.getAmount(coin));
        }
    }
}
