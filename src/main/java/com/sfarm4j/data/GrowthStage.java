package com.sfarm4j.data;

public enum GrowthStage {
    SEED(1),
    BUD(2),
    GROWING(3),
    MATURE(4),
    HARVESTABLE(5);

    private final int frameIndex;

    GrowthStage(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public GrowthStage next(int steps) {
        int next = this.ordinal() + steps;
        if (next >= values().length) {
            return values()[values().length - 1];
        }
        return values()[next];
    }
}