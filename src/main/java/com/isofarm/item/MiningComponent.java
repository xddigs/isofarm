package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.utils.Local;

@DataClass
public class MiningComponent extends Material implements Craftable {
    private final byte id;
    private final int value;

    public MiningComponent(Tier tier, MaterialID materialID) {
        super(tier, materialID);
        this.id = materialID.getId();
        this.value = materialID.getValue();
    }

    @Override
    public byte getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return Local.lang.item(getMaterialID().getDisplayName(), getTier());
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public Item copy() {
        return this;
    }
}