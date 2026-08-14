package com.sfarm4j.data;

@DataClass
public abstract class Item {
    private final byte id;
    private final String name;
    private final int value;
    private int amount;

    public Item(byte id, String name, int amount, int value) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.value = value;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public int getValue() {
        return value;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }
}
