package com.tilled.data;

import com.tilled.utils.K;

import java.util.LinkedHashMap;
import java.util.Map;

public class Purse {
    private final Map<Coin, Integer> coins;

    public Purse() {
        this.coins = new LinkedHashMap<>();
        for (int i = 0; i < K.World.STARTING_COINS; i++) {
            add(new Coin());
        }
    }

    public Map<Coin, Integer> getCoins() {
        return coins;
    }

    public void add(Coin coin) {
        coins.merge(coin, coin.getAmount(), Integer::sum);
    }

    public void remove(Coin coin) {
        coins.merge(coin, -coin.getAmount(), Integer::sum);
    }

    public Coin getCoin(int id) {
        for (Coin coin : coins.keySet()) {
            if (coin.getId() == id) {
                return coin;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return coins.isEmpty();
    }

    public int getSize() {
        return coins.size();
    }

    public int getAmount() {
        int amount = 0;
        for (Coin coin : coins.keySet()) {
            if (coin != null) {
                amount += coin.getAmount();
            }
        }
        return amount;
    }

    public void clear() {
        coins.clear();
    }
}
