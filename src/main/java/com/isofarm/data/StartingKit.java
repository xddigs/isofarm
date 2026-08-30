package com.isofarm.data;

import com.isofarm.entity.Player;
import com.isofarm.item.*;

public class StartingKit extends Kit {
    private final Player player;

    public StartingKit(Player player) {
        this.player = player;
        setItems(new Item[]{
                new CraftingBook(player)
        });
    }
}
