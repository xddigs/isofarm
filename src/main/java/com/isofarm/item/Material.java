package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;

@DataClass
public record Material(MaterialID materialID, Tier tier) implements Craftable {
    @Override
    public byte getId() {
        return materialID.getId();
    }

    @Override
    public String getName() {
        String name = materialID.getName();
        name += tier.equals(Tier.NONE) ? "" : tier.getName();
        return name;
    }

    @Override
    public int getValue() {
        return materialID.getValue();
    }

    @Override
    public Item copy() {
        return new Material(materialID, tier);
    }
}