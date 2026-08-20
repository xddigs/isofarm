package com.soilcraft.data;

public enum GrowthStage {
    SEED("Seed", 0),
    BUD("Bud", 1),
    GROWING("Growing", 2),
    MATURE("Mature", 3),
    HARVESTABLE("Harvestable", 4);

    private final String name;
    private final int frameIndex;

    GrowthStage(String name, int frameIndex) {
        this.name = name;
        this.frameIndex = frameIndex;
    }

    public String getName() {
        return name;
    }

    public int getFrameIndex() {
        return frameIndex;
    }
}