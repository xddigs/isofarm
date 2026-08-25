package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.MaterialID;

@DataClass
public record Material(MaterialID materialID) implements Craftable {
    @Override
    public byte getId() {
        return materialID.getId();
    }

    @Override
    public String getName() {
        return materialID.getName();
    }

    @Override
    public int getValue() {
        return materialID.getValue();
    }

    @Override
    public Item copy() {
        return new Material(materialID);
    }
}