package com.isofarm.gui;

import com.isofarm.data.Inventory;
import com.isofarm.entity.Player;
import com.isofarm.utils.Settings;

public class BackpackInventoryUI extends InventoryUI {
    private static final int BACKPACK_SLOTS = 16;
    private final InventorySlotUI[] backpackSlots = new InventorySlotUI[BACKPACK_SLOTS];
    private Inventory backpack;

    public BackpackInventoryUI(float x, float y) {
        super(x, y);
        setBackpackUI(this);
        getButtons().forEach(this::removeChild);

        hide();
        setWidth(getBackpackWidth());
        setHeight(getBackpackHeight());
        setLayer(50);
        createBackpackSlots();
    }

    private void createBackpackSlots() {
        for (int i = 0; i < BACKPACK_SLOTS; i++) {
            int column = i % 4;
            int row = i / 4;
            float x = Settings.getScaledPadding() + column * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            float y = Settings.getScaledPadding() + Settings.getScaledHeader() + row * (Settings.getScaledSlot() + Settings.getScaledSpacing());

            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(), Settings.getScaledSlot(),
                    InventorySlotUI.SlotType.BACKPACK);

            backpackSlots[i] = slotUI;
            addChild(slotUI);
        }
    }

    @Override
    public InventorySlotUI[] getSlotUIs() {
        return backpackSlots;
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