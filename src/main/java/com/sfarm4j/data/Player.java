package com.sfarm4j.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataClass
public class Player {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final String name;
    private final Inventory inventory;
    private int money;

    public Player(String name) {
        this.name = name;
        this.inventory = new Inventory();
        this.money = 0;
    }

    public String getName() {
        return name;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getMoney() {
        return money;
    }

    public void add(Item item, int amount) {
        inventory.add(item, amount);
        log.info("Added x{} of {} to inventory",
                item.getName(), item.getAmount());
    }

    public void add(Item item) {
        inventory.add(item, 1);
        log.info("Added x1 of {} to inventory",
                item.getName());
    }

    public void remove(Item item, int amount) {
        inventory.remove(item, amount);
        log.info("Removed x{} of {} from inventory",
                item.getName(), item.getAmount());
    }

    public void remove(Item item) {
        inventory.remove(item, 1);
        log.info("Removed x1 of {} from inventory",
                item.getName());
    }

    public void clear() {
        inventory.clear();
    }

    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    public int size() {
        return inventory.size();
    }

    public Item get(int index) {
        return inventory.get(index);
    }

    public Item get(Item item) {
        return inventory.get(item);
    }

    public int getAmount(Item item) {
        return inventory.getAmount(item);
    }

    public void earn(int amount) {
        this.money += amount;
    }
}
