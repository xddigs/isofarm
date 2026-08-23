package com.isofarm.data;

import com.isofarm.item.*;

public abstract class Kit {
    private Item[] items;

    public Item[] getItems() {
        return items;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }
}
