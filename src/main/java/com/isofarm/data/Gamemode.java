package com.isofarm.data;

@DataClass
public enum Gamemode {
    SURVIVAL, GODMODE, NO_CLIP;

    private final byte id;

    Gamemode() {
        this.id = (byte) ordinal();
    }

    public byte getId() {
        return id;
    }

    public boolean isSurvival() {
        return this.equals(SURVIVAL);
    }

    public boolean isGodmode() {
        return this.equals(GODMODE);
    }

    public boolean isNoClip() {
        return this.equals(NO_CLIP);
    }

    public static Gamemode fromString(String text) {
        if (text == null) return null;
        for (Gamemode mode : values()) {
            if (mode.name().replace("_", "")
                    .equalsIgnoreCase(text.replace("_", ""))) {
                return mode;
            }
        }
        return null;
    }
}
