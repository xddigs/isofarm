package com.isofarm.data;

/**
 * Encapsulates the state and operations required by purse within the game runtime.
 */
@DataClass
public class Purse {
    private int coins;

    /**
     * Creates a new {@code Purse} instance.
     */
    public Purse() {
        this.coins = 0;
    }

    /**
     * Returns the balance.
     * @return {@code int}; the balance
     */
    public int getBalance() {
        return coins;
    }

    /**
     * Adds add.
     * @param amount the {@code int} supplied as {@code amount}
     */
    public void add(int amount) {
        if (amount <= 0) return;
        coins += amount;
    }

    /**
     * Removes remove.
     * @param amount the {@code int} supplied as {@code amount}
     */
    public void remove(int amount) {
        if (amount <= 0 || getBalance() < amount) return;
        coins -= amount;
    }

    /**
     * Determines whether this object contains no elements or active content.
     */
    public void empty() {
        coins = 0;
    }
}
