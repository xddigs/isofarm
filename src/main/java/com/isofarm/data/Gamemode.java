package com.isofarm.data;

/**
 * Enumerates the supported gamemode values.
 */
@DataClass
public enum Gamemode {
    SURVIVAL, GODMODE, NO_CLIP;

    private final byte id;

    /**
     * Creates a new {@code Gamemode} instance.
     */
    Gamemode() {
        this.id = (byte) ordinal();
    }

    /**
     * Returns the id.
     * @return the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Checks whether the survival condition is met.
     * @return {@code true} if survival; otherwise {@code false}
     */
    public boolean isSurvival() {
        return this.equals(SURVIVAL);
    }

    /**
     * Checks whether the godmode condition is met.
     * @return {@code true} if godmode; otherwise {@code false}
     */
    public boolean isGodmode() {
        return this.equals(GODMODE);
    }

    /**
     * Checks whether the no clip condition is met.
     * @return {@code true} if no clip; otherwise {@code false}
     */
    public boolean isNoClip() {
        return this.equals(NO_CLIP);
    }

    /**
     * Performs the from string operation.
     * @param text the text value
     * @return the from string result
     */
    public static Gamemode fromString(String text) {
        if (text == null) return null;
        for (Gamemode mode : values()) {
            if (mode.name().toLowerCase().replace("_", "")
                    .equalsIgnoreCase(text.replace("_", ""))) {
                return mode;
            }
        }
        return null;
    }
}
