package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.utils.Local;

/**
 * Encapsulates the state and operations required by mining component within the game runtime.
 */
@DataClass
public class MiningComponent extends Material implements Craftable {
    private final byte id;
    private final int value;

    /**
     * Creates a new {@code MiningComponent} instance.
     * @param tier the {@link Tier} supplied as {@code tier}
     * @param materialID the {@link MaterialID} supplied as {@code materialID}
     */
    public MiningComponent(Tier tier, MaterialID materialID) {
        super(tier, materialID);
        this.id = materialID.getId();
        this.value = materialID.getValue();
    }

    /**
     * {@inheritDoc}
     * Returns the id.
     * @return {@code byte}; the id
     */
    @Override
    public byte getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     * Returns the display name.
     * @return the {@link String} representing the display name
     */
    @Override
    public String getDisplayName() {
        return Local.lang.item(getMaterialID().getDisplayName(),
                getTier().getDisplayName());
    }

    /**
     * {@inheritDoc}
     * Returns the value.
     * @return {@code int}; the value
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     * Creates an independent copy that preserves the relevant state of this object.
     * @return the {@link Item} representing the copy result
     */
    @Override
    public Item copy() {
        return this;
    }
}