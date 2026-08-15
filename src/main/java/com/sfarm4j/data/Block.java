package com.sfarm4j.data;

@DataClass
public class Block extends Item {
    private final BlockData type;
    private int x, y, z;

    public Block(BlockData type, int x, int y, int z) {
        super(type.getId(), type.getName(), 1, type.getValue());
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block(BlockData type) {
        super(type.getId(), type.getName(), 1, type.getValue());
        this.type = type;
    }

    public BlockData getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
