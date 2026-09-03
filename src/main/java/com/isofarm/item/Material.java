package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.utils.Local;

/**
 * Provides material behavior.
 */
@DataClass
public class Material implements Craftable {
    private final Tier tier;
    private final MaterialID materialID;

    /**
     * Creates a new {@code Material} instance.
     * @param tier the tier value
     * @param materialID the material id value
     */
    public Material(Tier tier, MaterialID materialID) {
        this.tier = tier;
        this.materialID = materialID;
    }

    /**
     * Returns the id.
     * @return the id
     */
    @Override
    public byte getId() {
        return materialID.getId();
    }

    /**
     * Returns the name.
     * @return the name
     */
    @Override
    public String getName() {
        return materialID.getName();
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return Local.lang.item(materialID.getDisplayName(),
                tier.getDisplayName());
    }

    /**
     * Returns the value.
     * @return the value
     */
    @Override
    public int getValue() {
        return materialID.getValue();
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Material(tier, materialID);
    }

    /**
     * Returns the tier.
     * @return the tier
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * Returns the material id.
     * @return the material id
     */
    public MaterialID getMaterialID() {
        return materialID;
    }
}