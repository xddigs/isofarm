package com.sfarm4j.data;

public class CellExpansion extends Item {
    private final Block type;

    public CellExpansion(int value) {
        super((byte) 999, "Cell Expansion", 1, value);
        this.type = Block.DIRT;
    }

    public Block getType() {
        return type;
    }
}