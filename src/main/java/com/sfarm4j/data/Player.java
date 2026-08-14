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

        add(new Seed(), 4);
        add(new Seed(CropType.CARROT));
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

    public void setMoney(int money) {
        this.money = money;
    }

    public void sell(Item item, int amount) {
        if (item == null || amount <= 0) return;

        int current = inventory.getAmount(item);
        if (current <= 0) {
            log.warn("No {} in inventory to sell", item.getName());
            return;
        }

        int toSell = Math.min(current, amount);
        inventory.remove(item, toSell);
        int earnings = toSell * item.getValue();
        earn(earnings);
    }

    public void add(Item item, int amount) {
        inventory.add(item, amount);
        log.info("Added x{} of {} to inventory",
                amount, item.getName());
    }

    public void add(Item item) {
        inventory.add(item, 1);
        log.info("Added x1 of {} to inventory",
                item.getName());
    }

    public void remove(Item item, int amount) {
        inventory.remove(item, amount);
        log.info("Removed x{} of {} to inventory",
                amount, item.getName());
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
        log.info("Earned ${}", amount);
        this.money += amount;
    }

    public boolean hasSeeds() {
        return inventory.hasItemOfType(Seed.class);
    }

    public int getSeedCount() {
        return inventory.getTotalAmountOfType(Seed.class);
    }
}
