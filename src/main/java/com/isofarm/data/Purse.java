package com.isofarm.data;

@DataClass
public class Purse {
    private int coins;

    public Purse() {
        this.coins = 0;
    }

    public int getBalance() {
        return coins;
    }

    public void add(int amount) {
        if (amount <= 0) return;
        coins += amount;
    }

    public void remove(int amount) {
        if (amount <= 0 || getBalance() < amount) return;
        coins -= amount;
    }

    public void empty() {
        coins = 0;
    }
}
