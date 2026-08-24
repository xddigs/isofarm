package com.isofarm.gui;

import com.isofarm.data.Inventory;
import com.isofarm.entity.Player;

public class BackpackInventoryUI extends InventoryUI {

    private final Inventory backpackInventory;

    public BackpackInventoryUI(float x, float y, Player player) {
        super(x, y);
        getButtons().forEach(this::removeChild);
        setBackpackUI(this);

        setWidth(getBackpackWidth());
        setHeight(getBackpackHeight());
        setPlayer(player);

        backpackInventory = new Inventory();
        setInventory(backpackInventory);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void render() {
        if (!isVisible()) return;

        renderChildren();
    }

    public Inventory getBackpackInventory() {
        return backpackInventory;
    }
}