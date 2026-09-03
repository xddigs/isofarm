package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;

@DataClass
public class Material implements Craftable {
    private final Tier tier;
    private final MaterialID materialID;

    public Material(Tier tier, MaterialID materialID) {
        this.tier = tier;
        this.materialID = materialID;
    }

    @Override
    public byte getId() {
        return materialID.getId();
    }

    @Override
    public String getName() {
        return materialID.getName();
    }

    @Override
    public String getDisplayName() {
        String name = materialID.getDisplayName();
        // TODO localization order for languaages other than English #2
        name = tier.equals(Tier.NONE) ? name : tier.getName() + name;
        return name;
    }

    @Override
    public int getValue() {
        return materialID.getValue();
    }

    @Override
    public Item copy() {
        return new Material(tier, materialID);
    }

    public Tier getTier() {
        return tier;
    }

    public MaterialID getMaterialID() {
        return materialID;
    }
}