package com.tilled.data;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return id == item.id && Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
