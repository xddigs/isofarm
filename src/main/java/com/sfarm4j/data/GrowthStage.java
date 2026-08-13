package com.sfarm4j.data;

public enum GrowthStage {
    SEED(0),
    BUD(1),
    GROWING(2),
    MATURE(3),
    HARVESTABLE(4);

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