package com.isofarm.data;

import com.isofarm.item.Craftable;
import com.isofarm.item.Item;
import com.isofarm.utils.Local;

import java.util.Locale;

/**
 * Enumerates the supported material id values.
 */
@DataClass
public enum MaterialID implements Craftable {
    RAW_ORE((byte) 0, (byte) 0, 15),
    INGOT((byte) 1, (byte) 0, 50),
    STICK((byte) 0, (byte) 1, 1),
    PAPER((byte) 1, (byte) 1, 10),
    LEATHER((byte) 2, (byte) 1, 40),
    SUGAR_CANE((byte) 3, (byte) 1, 10),
    SUGAR((byte) 4, (byte) 1, 20);

    private final byte id;
    private final byte row;
    private final int value;

    /**
     * Creates a new {@code MaterialID} instance.
     * @param id the id value
     * @param row the row value
     * @param value the value value
     */
    MaterialID(byte id, byte row, int value) {
        this.id = id;
        this.row = row;
        this.value = value;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public byte getId() { return id; }
    /**
     * Returns the row.
     * @return the row
     */
    public byte getRow() { return row; }

    /**
     * Returns the name.
     * @return the name
     */
    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return Local.lang.t("item.material." + name().toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the value.
     * @return the value
     */
    @Override
    public int getValue() { return value; }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return this;
    }
}