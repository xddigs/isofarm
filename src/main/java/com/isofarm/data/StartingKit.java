package com.isofarm.data;

import com.isofarm.item.*;

public class StartingKit {
    private final Item[] items;

    public StartingKit() {
        this.items = new Item[]{
                new Hoe(), new Pickaxe(), new Shovel(), new Axe(), new Sword()
        };
    }

    public Item[] getItems() {
        return items;
    }
}
