package com.sfarm4j.data;

public class CellExpansion extends Item {
    private final BlockData type;

    public CellExpansion(int value) {
        super((byte) 999, "Cell Expansion", 1, value);
        this.type = BlockData.DIRT;
    }

    public BlockData getType() {
        return type;
    }
}