package com.isofarm.data;

import com.isofarm.utils.Local;

public enum Difficulty {
    EASY((byte) 0, "difficulty.easy", 0.5f),
    NORMAL((byte) 1, "difficulty.normal", 1.0f),
    HARD((byte) 2, "difficulty.hard", 2.0f),
    NIGHTMARE((byte) 3, "difficulty.nightmare", 5.0f),;

    private final byte id;
    private final String name;
    private final float multiplier;

    Difficulty(byte id, String name, float multiplier) {
        this.id = id;
        this.name = name;
        this.multiplier = multiplier;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return Local.lang.t(name);
    }

    public float getMultiplier() {
        return multiplier;
    }

    public static Difficulty fromId(byte id) {
        for (Difficulty difficulty : values()) {
            if (difficulty.getId() == id) {
                return difficulty;
            }
        }
        return null;
    }
}
