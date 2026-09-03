package com.isofarm.data;

import com.isofarm.utils.Local;

/**
 * Enumerates the supported crop type values.
 */
public enum CropType {
    WHEAT((byte) 0, "Wheat", "crop.wheat", 4, 5, 4),
    CARROT((byte) 1, "Carrot", "crop.carrot", 3, 8, 6),
    POTATO((byte) 2, "Potato", "crop.potato", 6, 12, 8),
    BEETROOT((byte) 3, "Beetroot", "crop.beetroot", 4, 16, 2);

    private final byte id;
    private final String name;
    private final String displayName;
    private final int yield;
    private final int value;
    private final int seeds;

    /**
     * Creates a new {@code CropType} instance.
     * @param id the id value
     * @param name the name value
     * @param displayName the display name value
     * @param yield the yield value
     * @param value the value value
     * @param seeds the seeds value
     */
    CropType(byte id, String name, String displayName,
             int yield, int value, int seeds) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.yield = yield;
        this.value = value;
        this.seeds = seeds;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    public String getDisplayName() {
        return Local.lang.t(displayName);
    }

    /**
     * Returns the yield.
     * @return the yield
     */
    public int getYield() {
        return yield;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the seeds.
     * @return the seeds
     */
    public int getSeeds() {
        return seeds;
    }

    /**
     * Performs the from id operation.
     * @param id the id value
     * @return the from id result
     */
    public static CropType fromId(byte id) {
        for (CropType crop : CropType.values()) {
            if (crop.getId() == id) {
                return crop;
            }
        }
        return null;
    }
}