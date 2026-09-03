package com.isofarm.data;

/**
 * Enumerates the supported growth stage values.
 */
public enum GrowthStage {
    SEED("Seed", 0),
    BUD("Bud", 1),
    GROWING("Growing", 2),
    MATURE("Mature", 3),
    HARVESTABLE("Harvestable", 4);

    private final String name;
    private final int frameIndex;

    /**
     * Creates a new {@code GrowthStage} instance.
     * @param name the name value
     * @param frameIndex the frame index value
     */
    GrowthStage(String name, int frameIndex) {
        this.name = name;
        this.frameIndex = frameIndex;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the frame index.
     * @return the frame index
     */
    public int getFrameIndex() {
        return frameIndex;
    }
}