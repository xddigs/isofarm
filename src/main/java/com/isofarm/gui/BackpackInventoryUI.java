package com.isofarm.gui;

import com.isofarm.entity.Player;
import com.isofarm.utils.Settings;

import java.util.Arrays;

public class BackpackInventoryUI extends InventoryUI {
    private static final int BACKPACK_SLOTS = 16;
    private static final int BACKPACK_COLUMNS = 4;
    private static final int BACKPACK_ROWS = 4;

    private final InventorySlotUI[] slotUIs;
    private Player player;

    public BackpackInventoryUI(float x, float y, Player player) {
        super(x, y);
        this.player = player;
        this.slotUIs = new InventorySlotUI[BACKPACK_SLOTS];

        getButtons().forEach(super::removeChild);

        createBackpackSlots();
        setBackpackUI(this);

        setWidth(getBackpackWidth());
        setHeight(getBackpackHeight());
    }

    private static float getBackpackWidth() {
        return Settings.getScaledPadding() * 2.0f +
                BACKPACK_COLUMNS * Settings.getScaledSlot() +
                (BACKPACK_COLUMNS - 1) * Settings.getScaledSpacing();
    }

    private static float getBackpackHeight() {
        return Settings.getScaledPadding() * 2.0f + Settings.getScaledHeader() +
                BACKPACK_ROWS * Settings.getScaledSlot() +
                (BACKPACK_ROWS - 1) * Settings.getScaledSpacing();
    }

    public void createBackpackSlots() {
        for (int i = 0; i < BACKPACK_SLOTS; i++) {
            int column = i % BACKPACK_COLUMNS;
            int row = i / BACKPACK_COLUMNS;
            float x = Settings.getScaledPadding() + column * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            float y = Settings.getScaledPadding() + Settings.getScaledHeader() + row * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(), Settings.getScaledSlot());

            slotUIs[i] = slotUI;
            addChild(slotUI);
        }
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

    public InventorySlotUI[] getSlotUIs() {
        return slotUIs;
    }

    public Player getPlayer() { 
        return player; 
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}