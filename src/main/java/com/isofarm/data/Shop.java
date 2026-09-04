package com.isofarm.data;

import com.isofarm.item.*;
import com.isofarm.service.TimeService;
import com.isofarm.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides shop behavior.
 */
@DataClass
public class Shop {
    private static final Logger log = LoggerFactory.getLogger(Shop.class);
    private final String owner;
    private final Inventory stock;
    private final Purse purse;

    /**
     * Creates a new {@code Shop} instance.
     */
    public Shop() {
        this.owner = getRandomName();
        this.stock = new Inventory();
        this.purse = new Purse();
        setUpStock();
        this.purse.add(K.World.STARTING_COINS);
    }

    /**
     * Sets the up stock.
     */
    private void setUpStock() {
        clear();
    }

    /**
     * Returns the owner.
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns the stock.
     * @return the stock
     */
    public Inventory getStock() {
        return stock;
    }

    /**
     * Returns the purse.
     * @return the purse
     */
    public Purse getPurse() {
        return purse;
    }

    /**
     * Performs the purse operation.
     * @return the purse result
     */
    public int purse() {
        return purse.getBalance();
    }

    /**
     * Performs the earn operation.
     * @param amount the amount value
     */
    public void earn(int amount) {
        purse.add(amount);
    }

    /**
     * Updates the current state.
     * @param timeService the time service value
     */
    public void update(TimeService timeService) {
        if (timeService.getDay() % 10 == 0) {
            reset();
        }
    }

    /**
     * Adds add.
     * @param item the item value
     * @param amount the amount value
     */
    public void add(Item item, int amount) {
        if (amount <= 0) return;
        stock.add(item, amount);
        log.info("Added x{} of {} to shop stock", amount, item.getName());
    }

    /**
     * Performs the sell operation.
     * @param item the item value
     * @param amount the amount value
     */
    public void sell(Item item, int amount) {
        if (amount <= 0) return;
        stock.remove(item, amount);
        earn(amount);
        log.info("Sold x{} of {} to player", amount, item.getName());
    }

    /**
     * Performs the buy operation.
     * @param item the item value
     * @param amount the amount value
     */
    public void buy(Item item, int amount) {
        int totalPrice = item.getValue() * amount;
        if (!hasMoney() || purse() < totalPrice) {
            log.warn("Not enough money to buy x{} of {}", amount, item.getName());
            return;
        }
        stock.add(item, amount);
        purse.remove(totalPrice);
        log.info("Bought x{} of {} from player", amount, item.getName());
    }

    /**
     * Removes clear.
     */
    public void clear() {
        stock.clear();
        log.info("Cleared shop stock");
    }

    /**
     * Checks whether the money condition is met.
     * @return {@code true} if money; otherwise {@code false}
     */
    public boolean hasMoney() {
        return purse() > 0;
    }

    /**
     * Checks whether the empty condition is met.
     * @return {@code true} if empty; otherwise {@code false}
     */
    public boolean isEmpty() {
        return stock.isEmpty();
    }

    /**
     * Performs the size operation.
     * @return the size result
     */
    public int size() {
        return stock.size();
    }

    /**
     * Returns get.
     * @param index the index value
     * @return the get result
     */
    public Item get(int index) {
        return stock.get(index);
    }

    /**
     * Returns the amount.
     * @param item the item value
     * @return the amount
     */
    public int getAmount(Item item) {
        return stock.getAmount(item);
    }

    /**
     * Performs the reset operation.
     */
    public void reset() {
        setUpStock();
        this.purse.add(K.World.STARTING_COINS);
    }

    /**
     * Returns the random name.
     * @return the random name
     */
    private String getRandomName() {
        String[] names = {"John", "Michael", "Jeffrey", "Hugh",
                          "Angela", "Malenia", "Carol", "Ashley"};
        return names[(int) (Math.random() * names.length)];
    }

}
