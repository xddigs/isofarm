package com.isofarm.data;

import com.isofarm.utils.Local;

/**
 * Enumerates the supported weather type values.
 */
public enum WeatherType {
    CLEAR("Sunny", 1.0f, 0, 0.0f),
    RAIN("Rain", 1.25f, 2, 0.0f),
    THUNDERSTORM("Thunderstorm", 0.5f, 5, 0.05f),
    DROUGHT("Draught", 0.0f, -1, 0.0f),
    FROST("Ice Age", 0.0f, 0, 0.10f);

    private final String name;
    private final float growthMultiplier;
    private final int waterDPS;
    private final float cropDamageChance;

    /**
     * Creates a new {@code WeatherType} instance.
     * @param name the name value
     * @param growthMultiplier the growth multiplier value
     * @param waterDPS the water dps value
     * @param cropDamageChance the crop damage chance value
     */
    WeatherType(String name, float growthMultiplier,
                int waterDPS, float cropDamageChance) {
        this.name = name;
        this.growthMultiplier = growthMultiplier;
        this.waterDPS = waterDPS;
        this.cropDamageChance = cropDamageChance;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() { return name; }
    /**
     * Returns the display name.
     * @return the display name
     */
    public String getDisplayName() { return Local.lang.t("weather." + name().toLowerCase()); }
    /**
     * Returns the growth multiplier.
     * @return the growth multiplier
     */
    public float getGrowthMultiplier() { return growthMultiplier; }
    /**
     * Returns the water dps.
     * @return the water dps
     */
    public int getWaterDPS() { return waterDPS; }
    /**
     * Returns the crop damage chance.
     * @return the crop damage chance
     */
    public float getCropDamageChance() { return cropDamageChance; }
}