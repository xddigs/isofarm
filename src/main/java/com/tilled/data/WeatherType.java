package com.tilled.data;

public enum WeatherType {
    CLEAR("Sunny", 1.0f, 0, 0.0f),
    RAIN("Rain", 1.25f, 2, 0.0f),
    HEAVY_STORM("Thunderstorm", 0.5f, 5, 0.05f),
    DROUGHT("Draught", 0.0f, -1, 0.0f),
    FROST("Ice Age", 0.0f, 0, 0.10f);

    private final String name;
    private final float growthMultiplier;
    private final int waterDPS;
    private final float cropDamageChance;

    WeatherType(String name, float growthMultiplier,
                int waterDPS, float cropDamageChance) {
        this.name = name;
        this.growthMultiplier = growthMultiplier;
        this.waterDPS = waterDPS;
        this.cropDamageChance = cropDamageChance;
    }

    public String getName() { return name; }
    public float getGrowthMultiplier() { return growthMultiplier; }
    public int getWaterDPS() { return waterDPS; }
    public float getCropDamageChance() { return cropDamageChance; }
}