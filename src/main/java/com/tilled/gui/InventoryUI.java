package com.tilled.gui;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import com.tilled.input.Mouse;
import com.tilled.utils.K;
import com.tilled.utils.Settings;
import org.joml.Vector4f;

@SuppressWarnings("unused")
public class InventoryUI extends UIElement {
    private final InventorySlotUI[] slotUIs = new InventorySlotUI[K.UI.INVENTORY_SLOTS];
    private final Vector4f backgroundColor = new Vector4f(0.06f, 0.06f, 0.06f, 1.0f);
    private final Vector4f textColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private Player player;

    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet toolIcons;

    private Item carriedItem;
    private SpriteSheet carriedSpriteSheet;
    private int carriedSpriteFrame;

    private int selectedSlot = -1;

    public InventoryUI(float x, float y) {
        super(x, y, getInventoryWidth(), getInventoryHeight());
        setFocusable(true);
        createSlots();
    }

    private static float getInventoryWidth() {
        return Settings.getScaledPadding() * 2.0f +
                K.UI.INVENTORY_COLUMNS * Settings.getScaledSlot() +
                (K.UI.INVENTORY_COLUMNS - 1) * Settings.getScaledSpacing();
    }

    private static float getInventoryHeight() {
        return Settings.getScaledPadding() * 2.0f +
                Settings.getScaledHeader() +
                K.UI.INVENTORY_ROWS * Settings.getScaledSlot() +
                (K.UI.INVENTORY_ROWS - 1) * Settings.getScaledSpacing();
    }

    private static float getSlotSize() {
        return Settings.getScaledGUI();
    }

    private static int getItemIconColumn(Item item) {
        if (item instanceof Seed seed && seed.getType() != null) {
            return seed.getType().getId();
        }

        if (item instanceof Crop crop && crop.getCropType() != null) {
            return crop.getCropType().getId();
        }

        if (item instanceof Block block && block.getType() != null) {
            return block.getType().getId() - 1;
        }

        if (item instanceof Tool tool) {
            return tool.getId();
        }

        return 0;
    }

    private void createSlots() {
        for (int i = 0; i < K.UI.INVENTORY_SLOTS; i++) {
            int column = i % K.UI.INVENTORY_COLUMNS;
            int row = i / K.UI.INVENTORY_COLUMNS;

            float x = Settings.getScaledPadding() +
                    column * (Settings.getScaledSlot() + Settings.getScaledSpacing());

            float y = Settings.getScaledPadding() +
                    Settings.getScaledHeader() +
                    row * (Settings.getScaledSlot() + Settings.getScaledSpacing());

            InventorySlotUI slotUI = new InventorySlotUI(
                    x,
                    y,
                    Settings.getScaledSlot(),
                    Settings.getScaledSlot()
            );

            slotUIs[i] = slotUI;
            addChild(slotUI);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (player == null) return;
        syncInventory();
        updateSlots();
        interact();
    }

    private void syncInventory() {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < K.UI.INVENTORY_SLOTS; i++) {
            InventorySlotUI slotUI = slotUIs[i];

            if (i < inventory.getSlots().size()) {
                slotUI.setSlot(inventory.getSlot(i));
            } else {
                slotUI.setSlot(null);
            }

            updateItemSprite(slotUI);
        }
    }

    private void updateItemSprite(InventorySlotUI slotUI) {
        Item item = slotUI.getItem();

        if (item == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setSpriteFrame(0);
            return;
        }

        SpriteSheet spriteSheet = getItemSpritesheet(item);

        if (spriteSheet == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setSpriteFrame(0);
            return;
        }

        slotUI.setSpriteSheet(spriteSheet);
        slotUI.setSpriteFrame(getItemIconColumn(item));
    }

    private void updateSlots() {
        for (int i = 0; i < slotUIs.length; i++) {
            InventorySlotUI slotUI = slotUIs[i];
            boolean hovered = isSlotHovered(slotUI);
            slotUI.setSelected(selectedSlot == i);
            slotUI.setHovered(hovered);
        }
    }

    private boolean isSlotHovered(InventorySlotUI slotUI) {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        float x = slotUI.getAbsoluteX();
        float y = slotUI.getAbsoluteY();
        float width = slotUI.getAbsoluteWidth();
        float height = slotUI.getAbsoluteHeight();

        Item item = slotUI.getItem();
        if (item != null) {
            slotUI.tooltip(item.getName());
        } else {
            slotUI.tooltip(null);
        }

        return mouseX >= x &&
                mouseX <= x + width &&
                mouseY >= y &&
                mouseY <= y + height;
    }

    private void interact() {
        if (!Mouse.isButtonPressed(0)) {
            return;
        }

        for (int i = 0; i < slotUIs.length; i++) {
            InventorySlotUI slotUI = slotUIs[i];
            if (!slotUI.isHovered()) {
                continue;
            }

            clickSlot(i);
            break;
        }
    }

    private void clickSlot(int slotIndex) {
        Inventory inventory = player.getInventory();
        if (carriedItem == null) {
            InventorySlot slot = inventory.getSlot(slotIndex);

            if (slot.isEmpty()) {
                return;
            }

            carriedItem = slot.getItem();
            carriedSpriteSheet = getItemSpritesheet(carriedItem);
            carriedSpriteFrame = getItemIconColumn(carriedItem);
            selectedSlot = slotIndex;
            return;
        }

        if (selectedSlot == slotIndex) {
            carriedItem = null;
            carriedSpriteSheet = null;
            carriedSpriteFrame = 0;
            selectedSlot = -1;
            return;
        }

        inventory.pickAndDrop(selectedSlot, slotIndex);
        carriedItem = null;
        carriedSpriteSheet = null;
        carriedSpriteFrame = 0;
        selectedSlot = -1;
    }

    @Override
    public void render() {
        float x = getAbsoluteX();
        float y = getAbsoluteY();

        GUI.drawRect(x, y, getAbsoluteWidth(), getAbsoluteHeight(),
                new Vector4f(backgroundColor.x, backgroundColor.y, backgroundColor.z,
                        backgroundColor.w * getWorldOpacity()));

        renderChildren();
        renderItem();
    }

    private void renderItem() {
        if (carriedItem == null || carriedSpriteSheet == null) return;
        float size = Settings.getScaledIcon();
        float x = Mouse.getX() - size * 0.5f;
        float y = Mouse.getY() - size * 0.5f;

        GUI.drawSprite(carriedSpriteSheet, carriedSpriteFrame,
                x, y, size, size, new Vector4f(1.0f, 1.0f, 1.0f, 0.85f));
    }

    private SpriteSheet getItemSpritesheet(Item item) {
        if (item instanceof Crop) {
            return cropIcons;
        }

        if (item instanceof Seed) {
            return seedIcons;
        }

        if (item instanceof Block) {
            return blockIcons;
        }

        if (item instanceof Tool) {
            return toolIcons;
        }

        return null;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public SpriteSheet getSeedIcons() {
        return seedIcons;
    }

    public void setSeedIcons(SpriteSheet seedIcons) {
        this.seedIcons = seedIcons;
    }

    public SpriteSheet getCropIcons() {
        return cropIcons;
    }

    public void setCropIcons(SpriteSheet cropIcons) {
        this.cropIcons = cropIcons;
    }

    public SpriteSheet getBlockIcons() {
        return blockIcons;
    }

    public void setBlockIcons(SpriteSheet blockIcons) {
        this.blockIcons = blockIcons;
    }

    public SpriteSheet getToolIcons() {
        return toolIcons;
    }

    public void setToolIcons(SpriteSheet toolIcons) {
        this.toolIcons = toolIcons;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        if (selectedSlot < -1 || selectedSlot >= K.UI.INVENTORY_SLOTS) {
            return;
        }

        this.selectedSlot = selectedSlot;
    }

    public InventorySlotUI getSlotUI(int index) {
        if (index < 0 || index >= K.UI.INVENTORY_SLOTS) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + K.UI.INVENTORY_SLOTS);
        }

        return slotUIs[index];
    }

    public InventorySlotUI[] getSlotUIs() {
        return slotUIs.clone();
    }

    public Item getSelectedItem() {
        if (player == null || selectedSlot < 0) {
            return null;
        }

        Inventory inventory = player.getInventory();
        if (selectedSlot >= inventory.getSlots().size()) {
            return null;
        }

        InventorySlot slot = inventory.getSlot(selectedSlot);
        return slot.isEmpty() ? null : slot.getItem();
    }

    public void setSelectedItem(Item item) {
        if (player == null || selectedSlot < 0) {
            return;
        }

        Inventory inventory = player.getInventory();
        inventory.getSlot(selectedSlot).setItem(item);
    }

    public void clearSelection() {
        selectedSlot = -1;
    }
}