package com.isofarm.data;

import com.isofarm.item.*;
import com.isofarm.service.TimeService;
import com.isofarm.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the state and operations required by shop within the game runtime.
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
     * @return the {@link String} representing the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns the stock.
     * @return the {@link Inventory} representing the stock
     */
    public Inventory getStock() {
        return stock;
    }

    /**
     * Returns the purse.
     * @return the {@link Purse} representing the purse
     */
    public Purse getPurse() {
        return purse;
    }

    /**
     * Returns the purse associated with this character.
     * @return {@code int}; the purse result
     */
    public int purse() {
        return purse.getBalance();
    }

    /**
     * Processes earn and updates the affected inventory or currency balances.
     * @param amount the {@code int} supplied as {@code amount}
     */
    public void earn(int amount) {
        purse.add(amount);
    }

    /**
     * Updates the current state.
     * @param timeService the {@link TimeService} supplied as {@code timeService}
     */
    public void update(TimeService timeService) {
        if (timeService.getDay() % 10 == 0) {
            reset();
        }
    }

    /**
     * Adds add.
     * @param item the {@link Item} supplied as {@code item}
     * @param amount the {@code int} supplied as {@code amount}
     */
    public void add(Item item, int amount) {
        if (amount <= 0) return;
        stock.add(item, amount);
        log.info("Added x{} of {} to shop stock", amount, item.getName());
    }

    /**
     * Processes sell and updates the affected inventory or currency balances.
     * @param item the {@link Item} supplied as {@code item}
     * @param amount the {@code int} supplied as {@code amount}
     */
    public void sell(Item item, int amount) {
        if (amount <= 0) return;
        stock.remove(item, amount);
        earn(amount);
        log.info("Sold x{} of {} to player", amount, item.getName());
    }

    /**
     * Processes buy and updates the affected inventory or currency balances.
     * @param item the {@link Item} supplied as {@code item}
     * @param amount the {@code int} supplied as {@code amount}
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
     * Returns the number or extent represented by size.
     * @return {@code int}; the size result
     */
    public int size() {
        return stock.size();
    }

    /**
     * Returns get.
     * @param index the {@code int} supplied as {@code index}
     * @return the {@link Item} representing the get result
     */
    public Item get(int index) {
        return stock.get(index);
    }

    /**
     * Returns the amount.
     * @param item the {@link Item} supplied as {@code item}
     * @return {@code int}; the amount
     */
    public int getAmount(Item item) {
        return stock.getAmount(item);
    }

    /**
     * Resets this object to its initial runtime state.
     */
    public void reset() {
        setUpStock();
        this.purse.add(K.World.STARTING_COINS);
    }

    /**
     * Returns the random name.
     * @return the {@link String} representing the random name
     */
    private String getRandomName() {
        String[] names = {"John", "Michael", "Jeffrey", "Hugh",
                          "Angela", "Malenia", "Carol", "Ashley"};
        return names[(int) (Math.random() * names.length)];
    }

}
