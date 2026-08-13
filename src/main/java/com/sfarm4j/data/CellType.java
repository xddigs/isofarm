package com.sfarm4j.data;

public enum CellType {
    DIRT((byte) 0, "Dirt", 0.4f),
    TILLED((byte) 1, "Tilled Dirt", 0.5f),
    WATERED((byte) 2, "Watered Dirt", 1.0f);

    private final byte id;
    private final String name;
    private final float waterLevel;

    CellType(byte id, String name, float waterLevel) {
        this.id = id;
        this.name = name;
        this.waterLevel = waterLevel;
    }

    public byte getId() { return id; }
    public String getName() { return name; }
    public float getWaterLevel() { return waterLevel; }
}
