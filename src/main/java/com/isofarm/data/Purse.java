package com.isofarm.data;

/**
 * Provides purse behavior.
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
     * @return the balance
     */
    public int getBalance() {
        return coins;
    }

    /**
     * Adds add.
     * @param amount the amount value
     */
    public void add(int amount) {
        if (amount <= 0) return;
        coins += amount;
    }

    /**
     * Removes remove.
     * @param amount the amount value
     */
    public void remove(int amount) {
        if (amount <= 0 || getBalance() < amount) return;
        coins -= amount;
    }

    /**
     * Performs the empty operation.
     */
    public void empty() {
        coins = 0;
    }
}
