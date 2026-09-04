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
    RAW_ORE((byte) 0, (byte) 0, "Raw Ore", 15),
    INGOT((byte) 1, (byte) 0, "Ingot", 50),
    STICK((byte) 0, (byte) 1, "Stick", 1),
    PAPER((byte) 1, (byte) 1, "Paper", 10),
    LEATHER((byte) 2, (byte) 1, "Leather", 40),
    SUGAR_CANE((byte) 3, (byte) 1, "Sugar Cane", 10),
    SUGAR((byte) 4, (byte) 1, "Sugar", 10);

    private final byte id;
    private final byte row;
    private final String name;
    private final int value;

    /**
     * Creates a new {@code MaterialID} instance.
     * @param id the id value
     * @param row the row value
     * @param name the name value
     * @param value the value value
     */
    MaterialID(byte id, byte row, String name, int value) {
        this.id = id;
        this.row = row;
        this.name = name;
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
        return name;
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