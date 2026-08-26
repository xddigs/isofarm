package com.isofarm.data;

import com.isofarm.item.*;

public class StartingKit extends Kit {

    public StartingKit() {
        setItems(new Item[]{
                new Backpack(),
                new CraftingKit(),
                new Bucket(),
                new Material(Tier.NONE, MaterialID.STICK),
                new MiningComponent(Tier.DIAMOND, MaterialID.RAW_ORE)
        });
    }
}
