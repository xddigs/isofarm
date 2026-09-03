package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.utils.Local;

@DataClass
public class MiningComponent extends Material implements Craftable {
    private final Tier tier;
    private final MaterialID materialID;
    private final byte id;
    private final String name;
    private final int value;

    public MiningComponent(Tier tier, MaterialID materialID) {
        super(tier, materialID);
        this.tier = tier;
        this.materialID = materialID;
        this.id = materialID.getId();
        // TODO Localization order for languages other than English
        this.name = Local.lang.f(tier.getDisplayName(), materialID.getDisplayName());
        this.value = materialID.getValue();
    }

    @Override
    public byte getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public Item copy() {
        return this;
    }

    public Tier getTier() {
        return tier;
    }

    public MaterialID getMaterialID() {
        return materialID;
    }
}
