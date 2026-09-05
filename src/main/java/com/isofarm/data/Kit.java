package com.isofarm.data;

import com.isofarm.item.*;

/**
 * Encapsulates the state and operations required by kit within the game runtime.
 */
public abstract class Kit {
    private Item[] items;

    /**
     * Returns the items.
     * @return an array of {@link Item} values; the items
     */
    public Item[] getItems() {
        return items;
    }

    /**
     * Sets the items.
     * @param items an array of {@link Item} values supplied as {@code items}
     */
    public void setItems(Item[] items) {
        this.items = items;
    }
}
