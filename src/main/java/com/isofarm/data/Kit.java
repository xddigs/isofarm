package com.isofarm.data;

import com.isofarm.item.*;

/**
 * Provides kit behavior.
 */
public abstract class Kit {
    private Item[] items;

    /**
     * Returns the items.
     * @return the items
     */
    public Item[] getItems() {
        return items;
    }

    /**
     * Sets the items.
     * @param items the items value
     */
    public void setItems(Item[] items) {
        this.items = items;
    }
}
