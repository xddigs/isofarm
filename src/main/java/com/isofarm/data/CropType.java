package com.isofarm.data;

import com.isofarm.utils.Local;

import java.util.Locale;

/**
 * Enumerates the supported crop type values.
 */
public enum CropType {
    WHEAT((byte) 0, "crop.wheat", 4, 5, 4),
    CARROT((byte) 1, "crop.carrot", 3, 8, 6),
    POTATO((byte) 2, "crop.potato", 6, 12, 8),
    BEETROOT((byte) 3, "crop.beetroot", 4, 16, 2),
    SUGAR_CANE_CROP((byte) 4, "crop.sugar_cane", 3, 10, 2);

    private final byte id;
    private final String displayName;
    private final int yield;
    private final int value;
    private final int seeds;

    /**
     * Creates a new {@code CropType} instance.
     * @param id the id value
     * @param displayName the display name value
     * @param yield the yield value
     * @param value the value value
     * @param seeds the seeds value
     */
    CropType(byte id, String displayName,
             int yield, int value, int seeds) {
        this.id = id;
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
        return name().toLowerCase(Locale.ROOT);
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
     * Checks whether this crop may grow in a vertical stack.
     *
     * @return {@code true} only for sugar cane
     */
    public boolean isStackable() {
        return this == SUGAR_CANE_CROP;
    }

    /**
     * Checks whether this crop uses the crossed plant mesh.
     *
     * @return {@code true} when the plant mesh should be used
     */
    public boolean usesPlantMesh() {
        return this == SUGAR_CANE_CROP;
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
