package com.isofarm.data;

import com.isofarm.item.Item;

/**
 * Defines plantable behavior.
 * Extensions of this interface can be planted
 */
public interface Plantable extends Item {
    /**
     * Returns the crop type.
     * @return {@code CropType}
     */
    CropType getType();
}
