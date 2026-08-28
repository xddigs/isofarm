package com.isofarm.gui;

import com.isofarm.data.Inventory;
import com.isofarm.entity.Player;
import com.isofarm.utils.Settings;

public class BackpackInventoryUI extends InventoryUI {
    private static final int BACKPACK_SLOTS = 16;

    private final InventorySlotUI[] slotUIs;
    private Inventory backpack;

    public BackpackInventoryUI(float x, float y) {
        super(x, y);
        setBackpackUI(this);
        getButtons().forEach(this::removeChild);
        this.slotUIs = new InventorySlotUI[BACKPACK_SLOTS];

        hide();
        setWidth(getBackpackWidth());
        setHeight(getBackpackHeight());
        setLayer(50);
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

    @Override
    public void createSlots() {
        for (int i = 0; i < 16; i++) {
            int column = i % 4;
            int row = i / 4;
            float x = Settings.getScaledPadding() + column * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            float y = Settings.getScaledPadding() + Settings.getScaledHeader() + row * (Settings.getScaledSlot() + Settings.getScaledSpacing());

            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(), Settings.getScaledSlot(),
                    InventorySlotUI.SlotType.BACKPACK);

            slotUIs[i] = slotUI;
            addChild(slotUI);
        }
    }

    public Inventory getBackpack() {
        return backpack;
    }

    @Override
    public void update(float delta) {
        if (getBackpack() == null) return;
        syncInventory();
        getChildren().forEach(child -> child.update(delta));
    }

    @Override
    public void render() {
        if (!isVisible()) return;
        renderChildren();
    }
}