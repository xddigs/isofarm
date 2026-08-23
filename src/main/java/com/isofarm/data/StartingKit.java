package com.isofarm.data;

import com.isofarm.item.*;

public class StartingKit extends Kit {

    public StartingKit() {
        setItems(new Item[]{
                new Hoe(), new Pickaxe(), new Shovel(), new Axe(), new Sword()
        });
    }
}
