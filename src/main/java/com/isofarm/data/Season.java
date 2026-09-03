package com.isofarm.data;

import com.isofarm.utils.Local;

/**
 * Enumerates the supported season values.
 */
public enum Season {
    WINTER((byte) 0, "Winter", 0.5),
    SPRING((byte) 1, "Spring", 2.0),
    SUMMER((byte) 2, "Summer", 1.0),
    AUTUMN((byte) 3, "Autumn", 1.5);

    private final byte id;
    private final String name;
    private final double valueMultiplier;

    /**
     * Creates a new {@code Season} instance.
     * @param id the id value
     * @param name the name value
     * @param valueMultiplier the value multiplier value
     */
    Season(byte id, String name, double valueMultiplier) {
        this.id = id;
        this.name = name;
        this.valueMultiplier = valueMultiplier;
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
        return Local.lang.t("season." + name().toLowerCase());
    }

    /**
     * Returns the value multiplier.
     * @return the value multiplier
     */
    public double getValueMultiplier() {
        return valueMultiplier;
    }

    /**
     * Performs the from id operation.
     * @param id the id value
     * @return the from id result
     */
    public static Season fromId(byte id) {
        for (Season season : Season.values()) {
            if (season.getId() == id) {
                return season;
            }
        }
        return SPRING;
    }
}
