package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.utils.Local;

/**
 * Provides mining component behavior.
 */
@DataClass
public class MiningComponent extends Material implements Craftable {
    private final byte id;
    private final int value;

    /**
     * Creates a new {@code MiningComponent} instance.
     * @param tier the tier value
     * @param materialID the material id value
     */
    public MiningComponent(Tier tier, MaterialID materialID) {
        super(tier, materialID);
        this.id = materialID.getId();
        this.value = materialID.getValue();
    }

    /**
     * Returns the id.
     * @return the id
     */
    @Override
    public byte getId() {
        return id;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return Local.lang.item(getMaterialID().getDisplayName(),
                getTier().getDisplayName());
    }

    /**
     * Returns the value.
     * @return the value
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return this;
    }
}