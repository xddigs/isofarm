package com.tilled.data;

@DataClass
public class Coin extends Tool {

    public Coin() {
        super((byte) 0, "Coin", 1, -1);
    }

    @Override
    public Item copy(int newAmount) {
        Coin coin = new Coin();
        coin.addAmount(newAmount);
        return coin;
    }
}
