package com.isofarm.data;

import java.util.Objects;

@DataClass
public abstract class Item {
    private final byte id;
    private final String name;
    private final int value;

    public Item(byte id, String name, int value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
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
    public abstract Item copy();
}
