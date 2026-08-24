package com.isofarm.gui;

import com.isofarm.data.Inventory;
import com.isofarm.entity.Player;

public class BackpackInventoryUI extends InventoryUI {
    private Inventory backpack;

    public BackpackInventoryUI(float x, float y) {
        super(x, y);
        getButtons().forEach(this::removeChild);
        setBackpackUI(this);

        setWidth(getBackpackWidth());
        setHeight(getBackpackHeight());
    }

    @Override
    public void setPlayer(Player player) {
        super.setPlayer(player);

        if (player != null) {
            backpack = player.getBackpack();
            setInventory(backpack);
        } else {
            backpack = null;
            setInventory(null);
        }
    }

    public Inventory getBackpack() {
        return backpack;
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
}