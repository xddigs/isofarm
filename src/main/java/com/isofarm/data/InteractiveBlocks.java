package com.isofarm.data;

import com.isofarm.utils.Local;

import java.util.Locale;

/**
 * Enumerates the supported interactive blocks values.
 */
@DataClass
public enum InteractiveBlocks implements Blockable {
    EMPTY(null, (byte) 0, (byte) -1, (byte) -1, -1),
    CHEST("assets/models/blocks/chest.gltf", (byte) 1, (byte) 0, (byte) 2, 10),;

    private final String texturePath;
    private final byte id;
    private final byte col;
    private final byte row;
    private final int value;

    InteractiveBlocks(String texturePath, byte id, byte col, byte row, int value) {
        this.texturePath = texturePath;
        this.id = id;
        this.col = col;
        this.row = row;
        this.value = value;
    }

    /**
     * Returns the name of the block.
     * @return {@link String} the name of the block
     */
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the display name of the block.
     * @return {@link String} the display of the block
     */
    public String getDisplayName() {
        return Local.lang.t("block." + name().toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the texture path of the block.
     * @return {@link String} the texture path of the block
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Returns the id of the block.
     * @return {@link Byte} the id of the block
     */
    public byte getId() {
        return id;
    }

    /**
     * Returns the col of the block.
     * @return {@link Byte} the col of the block
     */
    public byte getCol() {
        return col;
    }

    /**
     * Returns the row of the block.
     * @return {@link Byte} the row of the block
     */
    public byte getRow() {
        return row;
    }

    /**
     * Returns the value of the block.
     * @return {@link Integer} the value of the block
     */
    public int getValue() {
        return value;
    }
}
