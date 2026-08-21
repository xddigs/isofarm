package com.isofarm.data;

@DataClass
public enum Gamemode {
    SURVIVAL, GODMODE;

    private final byte id;

    Gamemode() {
        this.id = (byte) ordinal();
    }

    public byte getId() {
        return id;
    }

    public boolean isSurvival() {
        return this == SURVIVAL;
    }

    public boolean isGodmode() {
        return this == GODMODE;
    }
}
