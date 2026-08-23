package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

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
