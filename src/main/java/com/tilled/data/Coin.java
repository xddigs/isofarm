package com.tilled.data;

@DataClass
public class Coin extends Item {

    public Coin() {
        super((byte) 0, "Coin", 1, 1);
    }
}
