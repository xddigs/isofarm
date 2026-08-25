package com.isofarm.data;

import com.isofarm.item.Craftable;
import com.isofarm.item.Item;

@DataClass
public enum MaterialID implements Craftable {
    STICK((byte) 0, "Stick", 1),
    WOOD((byte) 1, "Wood", 2),
    STONE((byte) 2, "Stone", 3);

    private final byte id;
    private final String name;
    private final int value;

    MaterialID(byte id, String name, int value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public byte getId() { return id; }
    public String getName() { return name; }
    public int getValue() { return value; }

    @Override
    public Item copy() {
        return this;
    }
}