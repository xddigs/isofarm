package com.isofarm.data;

import com.isofarm.utils.Local;

import java.util.Locale;

/**
 * Enumerates the supported growth stage values.
 */
public enum GrowthStage {
    SEED(0),
    BUD(1),
    GROWING(2),
    MATURE(3),
    HARVESTABLE(4);

    private final int frameIndex;

    /**
     * Creates a new {@code GrowthStage} instance.
     * @param frameIndex the frame index value
     */
    GrowthStage(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    /**
     * Returns the name.
     * @return {@code String} the name
     */
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the display name.
     * @return {@code String} the display name
     */
    public String getDisplayName() {
        return Local.lang.t("growth." + name().toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the frame index.
     * @return the frame index
     */
    public int getFrameIndex() {
        return frameIndex;
    }
}