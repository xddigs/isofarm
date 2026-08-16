package com.tilled.data;

import com.tilled.service.ToastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataClass
public class Player {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final String name;
    private final Inventory inventory;
    private final Purse purse;
    private int experience;
    private int level;

    private final ToastService toastService;

    public Player(String name, ToastService toastService) {
        this.name = name;
        this.toastService = toastService;
        this.inventory = new Inventory();
        this.purse = new Purse(inventory, new Coin());
        setUpInventory();
    }

    private void setUpInventory() {
        add(new Seed(), 4);
    }

    public String getName() {
        return name;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Purse getPurse() {
        return purse;
    }

    public void gain(int amount) {
        experience += amount;
        if (isLevelUpAvailable()) {
            level++;
            experience = 0;
            log.info("Level up! New level: {}", level);
            toastService.success("Level up! New level: " + level);
        }
    }

    private boolean isLevelUpAvailable() {
        return experience >= (10 * level);
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
        toastService.sell("You successfully sold " + item.getName() + " for " + earnings + " coins");
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
        log.info("Cleared inventory");
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
        purse.add(amount);
    }

    public void spend(int amount) {
        if (amount <= 0) return;
        log.info("Spent ${}", amount);
        purse.remove(amount);
    }

    public int purse() {
        return purse.getBalance();
    }

    public boolean hasSeeds() {
        return inventory.hasItemOfType(Seed.class);
    }

    public int getSeedCount() {
        return inventory.getTotalAmountOfType(Seed.class);
    }
}
