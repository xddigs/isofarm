package com.sfarm4j.data;

public enum GrowthStage {
    SEED, BUD, GROWING, MATURE, HARVESTABLE;

    public GrowthStage next(int steps) {
        int next = this.ordinal() + steps;
        if (next >= values().length) {
            return this;
        }
        return values()[next];
    }
}