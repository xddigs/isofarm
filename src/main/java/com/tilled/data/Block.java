package com.tilled.data;

@DataClass
public class Block extends Item {
    private final BlockData type;
    private int x, y, z;
    private boolean unlocked = false;
    private boolean hasCrop = false;

    public Block(BlockData type, int x, int y, int z) {
        super(type.getId(), type.getName(), 1, type.getValue());
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block(BlockData type, int x, int z) {
        this(type, x, 0, z);
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

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean hasCrop() {
        return hasCrop;
    }

    public void setCrop(boolean hasCrop) {
        this.hasCrop = hasCrop;
    }
}