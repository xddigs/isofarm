package com.isofarm.data;

import com.isofarm.item.*;

/**
 * Provides starting kit behavior.
 */
public class StartingKit extends Kit {
    /**
     * Creates a new {@code StartingKit} instance.
     */
    public StartingKit() {
        setItems(new Item[]{
                new CraftingBook(),
                new Block(BlockData.SAND),
                new Material(Tier.NONE, MaterialID.SUGAR_CANE),
                new Produce(CropType.SUGAR_CANE_CROP)
        });
    }
}
