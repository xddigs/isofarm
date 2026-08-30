package com.isofarm.item;

import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;

public class CraftingBook extends Book {

    public CraftingBook() {
        super(Tier.NONE, MaterialID.CRAFTING_BOOK);
    }
}
