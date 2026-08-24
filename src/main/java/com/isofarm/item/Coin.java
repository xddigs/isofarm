package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;

@DataClass
public class Coin extends Tool {

    public Coin() {
        super((byte) 0, ToolType.COIN.getName(), 1,
                ToolType.COIN, Tier.COPPER, ToolType.COIN.getBaseDurability());
    }

    @Override
    public Item copy() {
        return new Coin();
    }
}
