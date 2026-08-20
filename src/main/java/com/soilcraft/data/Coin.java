package com.soilcraft.data;

@DataClass
public class Coin extends Tool {

    public Coin() {
        super((byte) 0, "Coin", 1, -1);
    }

    @Override
    public Item copy() {
        return new Coin();
    }
}
