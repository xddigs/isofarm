package com.isofarm.data;

import com.isofarm.entity.Player;
import com.isofarm.item.*;

/**
 * Provides starting kit behavior.
 */
public class StartingKit extends Kit {
    private final Player player;

    /**
     * Creates a new {@code StartingKit} instance.
     * @param player the player value
     */
    public StartingKit(Player player) {
        this.player = player;
        setItems(new Item[]{
                new CraftingBook(player)
        });
    }
}
