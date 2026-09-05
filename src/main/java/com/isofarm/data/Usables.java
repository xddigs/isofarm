package com.isofarm.data;

import com.isofarm.utils.Local;

/**
 * Enumerates the supported usables values.
 */
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

    /**
     * Creates a new {@code Usables} instance.
     * @param id the {@code byte} supplied as {@code id}
     * @param col the {@code byte} supplied as {@code col}
     * @param row the {@code byte} supplied as {@code row}
     * @param name the {@link String} supplied as {@code name}
     * @param displayName the {@link String} supplied as {@code displayName}
     * @param value the {@code int} supplied as {@code value}
     */
    Usables(byte id, byte col, byte row, String name, String displayName, int value) {
        this.id = id;
        this.col = col;
        this.row = row;
        this.name = name;
        this.displayName = displayName;
        this.value = value;
    }

    /**
     * Returns the id.
     * @return {@code byte}; the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Returns the col.
     * @return {@code byte}; the col
     */
    public byte getCol() {
        return col;
    }

    /**
     * Returns the row.
     * @return {@code byte}; the row
     */
    public byte getRow() {
        return row;
    }

    /**
     * Returns the name.
     * @return the {@link String} representing the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the {@link String} representing the display name
     */
    public String getDisplayName() {
        return Local.lang.t(displayName);
    }

    /**
     * Returns the value.
     * @return {@code int}; the value
     */
    public int getValue() {
        return value;
    }
}
