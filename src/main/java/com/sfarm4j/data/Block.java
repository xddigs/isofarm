package com.sfarm4j.data;

@DataClass
public class Block extends BlockItem {
    private final BlockData type;
    private float x, z;

    public Block(BlockData type) {
        super((byte) 1, type.getName(), type.getValue());
        this.type = type;
    }

    public BlockData getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public void update() {

    }
}
