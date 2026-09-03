package com.isofarm.data;

import com.isofarm.utils.Local;

@DataClass
public enum Usables {
    BACKPACK((byte) 0, (byte) 0, (byte) 0, "Backpack", "item.usable.backpack", 500),
    BOOK((byte) 1, (byte) 1, (byte) 0, "Book", "item.usable.book", 100),
    CRAFTING_BOOK((byte) 2, (byte) 2, (byte) 0, "Crafting Book", "item.usable.crafting_book", 200),
    BUCKET((byte) 3, (byte) 3, (byte) 0, "Bucket", "item.usable.bucket", 10);

    private final byte id;
    private final byte col;
    private final byte row;
    private final String name;
    private final String displayName;
    private final int value;

    Usables(byte id, byte col, byte row, String name, String displayName, int value) {
        this.id = id;
        this.col = col;
        this.row = row;
        this.name = name;
        this.displayName = displayName;
        this.value = value;
    }

    public byte getId() {
        return id;
    }

    public byte getCol() {
        return col;
    }

    public byte getRow() {
        return row;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return Local.lang.t(displayName);
    }

    public int getValue() {
        return value;
    }
}
