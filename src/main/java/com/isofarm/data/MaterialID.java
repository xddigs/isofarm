package com.isofarm.data;

import com.isofarm.item.Craftable;
import com.isofarm.item.Item;

@DataClass
public enum MaterialID implements Craftable {
    RAW_ORE((byte) 0, (byte) 0, " Raw Ore", 15),
    INGOT((byte) 1, (byte) 0, " Ingot", 50),
    STICK((byte) 0, (byte) 1, "Stick", 1),
    PAPER((byte) 1, (byte) 1, "Paper", 10),
    LEATHER((byte) 2, (byte) 1, "Leather", 40);

    private final byte id;
    private final byte row;
    private final String name;
    private final int value;

    MaterialID(byte id, byte row, String name, int value) {
        this.id = id;
        this.row = row;
        this.name = name;
        this.value = value;
    }

    public byte getId() { return id; }
    public byte getRow() { return row; }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return "item.material." + name().toLowerCase();
    }

    @Override
    public int getValue() { return value; }

    @Override
    public Item copy() {
        return this;
    }
}