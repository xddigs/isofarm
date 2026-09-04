package com.isofarm.gui;

import com.isofarm.data.Inventory;
import com.isofarm.data.SlotType;
import com.isofarm.entity.Player;
import com.isofarm.utils.Settings;

/**
 * Provides backpack inventory ui behavior.
 */
public class BackpackInventoryUI extends InventoryUI {
    private static final int BACKPACK_SLOTS = 16;
    private final InventorySlotUI[] backpackSlots = new InventorySlotUI[BACKPACK_SLOTS];
    private Inventory backpack;

    /**
     * Creates a new {@code BackpackInventoryUI} instance.
     * @param x the x value
     * @param y the y value
     */
    public BackpackInventoryUI(float x, float y) {
        super(x, y);
        backpack = Player.plyr.getBackpack();
        setInventory(backpack);
        setBackpackUI(this);
        getButtons().forEach(this::removeChild);

        hide();
        setWidth(getBackpackWidth());
        setHeight(getBackpackHeight());
        setLayer(50);
        createBackpackSlots();
    }

    /**
     * Creates and returns the backpack slots.
     */
    private void createBackpackSlots() {
        for (int i = 0; i < BACKPACK_SLOTS; i++) {
            int column = i % 4;
            int row = i / 4;
            float x = Settings.getScaledPadding() + column * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            float y = Settings.getScaledPadding() + Settings.getScaledHeader() + row * (Settings.getScaledSlot() + Settings.getScaledSpacing());

            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(), Settings.getScaledSlot(),
                    SlotType.BACKPACK);

            backpackSlots[i] = slotUI;
            addChild(slotUI);
        }
    }

    /**
     * Returns the slot uis.
     * @return the slot uis
     */
    @Override
    public InventorySlotUI[] getSlotUIs() {
        return backpackSlots;
    }

    /**
     * Returns the backpack.
     * @return the backpack
     */
    public Inventory getBackpack() {
        return backpack;
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    @Override
    public void update(float delta) {
        if (getBackpack() == null) return;
        syncInventory();
        getChildren().forEach(child -> child.update(delta));
    }

    /**
     * Renders render.
     */
    @Override
    public void render() {
        if (!isVisible()) return;
        renderChildren();
    }
}
