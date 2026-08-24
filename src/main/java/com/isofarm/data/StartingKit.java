package com.isofarm.data;

import com.isofarm.item.*;

public class StartingKit extends Kit {

    public StartingKit() {
        setItems(new Item[]{
                new Axe(), new Pickaxe(), new Shovel(Tier.COPPER)
        });
    }
}
