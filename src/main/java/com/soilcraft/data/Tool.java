package com.soilcraft.data;

@DataClass
public abstract class Tool extends Item {
    private int durability;

    public Tool(byte id, String name, int value, int durability) {
        super(id, name, value);
        this.durability = durability;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public void use() {
        if (canBeUsed()) {
            durability--;
        }
    }

    public boolean canBeUsed() {
        return durability > 0;
    }
}
