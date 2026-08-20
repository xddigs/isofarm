package com.soilcraft.data;

@DataClass
public class Coin extends Tool {

    public Coin() {
        super((byte) 0, "Coin", 1,
                ToolType.ELSE, Tier.COPPER, ToolType.ELSE.getBaseDurability());
    }

    @Override
    public Item copy() {
        return new Coin();
    }
}
