package com.isofarm.item;

import com.isofarm.data.MaterialID;

public class Material extends Item {
    private final MaterialID materialID;

    public Material(MaterialID materialID) {
        super(materialID.getId(), materialID.getName(), materialID.getValue());
        this.materialID = materialID;
    }

    public Material() {
        this(MaterialID.STICK);
    }

    public MaterialID getMaterialID() {
        return materialID;
    }

    @Override
    public Item copy() {
        return new Material(materialID);
    }
}
