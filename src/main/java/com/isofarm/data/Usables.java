package com.isofarm.data;

import com.isofarm.item.Craftable;
import com.isofarm.item.Item;

@DataClass
public enum Usables implements Craftable {
    BACKPACK((byte) 0, (byte) 0, (byte) 0, "Backpack", 500),
    BOOK((byte) 1, (byte) 1, (byte) 0, "Book", 100),
    CRAFTING_BOOK((byte) 2, (byte) 2, (byte) 0, "Crafting Book", 200),
    BUCKET((byte) 3, (byte) 3, (byte) 0, " Bucket", 10);

    private final byte id;
    private final byte col;
    private final byte row;
    private final String name;
    private final int value;

    Usables(byte id, byte col, byte row, String name, int value) {
        this.id = id;
        this.col = col;
        this.row = row;
        this.name = name;
        this.value = value;
    }

    @Override
    public byte getId() {
        return id;
    }

    public byte getCol() {
        return col;
    }

    public byte getRow() {
        return row;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public Item copy() {
        return this;
    }
}
