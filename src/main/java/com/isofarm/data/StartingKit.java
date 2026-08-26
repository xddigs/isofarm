package com.isofarm.data;

import com.isofarm.item.*;

public class StartingKit extends Kit {

    public StartingKit() {
        setItems(new Item[]{
                new Backpack(),
                new CraftingKit(),
                new Backpack(ToolType.BACKPACK, Tier.PLATINUM),
        });
    }
}
