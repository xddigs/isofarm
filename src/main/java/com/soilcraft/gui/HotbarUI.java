package com.soilcraft.gui;

import com.soilcraft.data.*;
import com.soilcraft.entity.Player;
import com.soilcraft.graphics.SpriteSheet;
import com.soilcraft.input.Mouse;
import com.soilcraft.utils.K;
import com.soilcraft.utils.Settings;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@SuppressWarnings("all")
public class HotbarUI extends UIElement {
    private final InventorySlotUI[] slotUIs = new InventorySlotUI[K.UI.INVENTORY_COLUMNS];
    private Player player;

    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet toolIcons;
    private SpriteSheet inventoryIcons;

    private int selectedSlot = 0;
    private boolean inventoryMode = false;

    public HotbarUI(float x, float y) {
        super(x, y, getHotbarWidth(), getHotbarHeight());
        setFocusable(true);
        createSlots();
    }

    private static float getHotbarWidth() {
        return Settings.getScaledPadding() * 2.0f +
                K.UI.INVENTORY_COLUMNS * Settings.getScaledSlot() +
                (K.UI.INVENTORY_COLUMNS - 1) * Settings.getScaledSpacing();
    }

    private static float getHotbarHeight() {
        return Settings.getScaledPadding() * 2.0f +
                Settings.getScaledSlot();
    }

    private static int getItemIconColumn(Item item) {
        if (item instanceof Produce produce && produce.getType() != null) {
            return produce.getType().getId();
        }

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
        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
            float x = Settings.getScaledPadding() + i * (
                    Settings.getScaledSlot() +
                    Settings.getScaledSpacing());

            float y = Settings.getScaledPadding();

            InventorySlotUI slotUI = new InventorySlotUI(x, y,
                    Settings.getScaledSlot(), Settings.getScaledSlot());
            slotUIs[i] = slotUI;
            addChild(slotUI);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (player == null) {
            return;
        }

        syncInventory();
        updateSlots();

        if (!inventoryMode) {
            interact();
        }
    }

    public void setInventoryMode(boolean inventoryMode) {
        this.inventoryMode = inventoryMode;
    }

    private void syncInventory() {
        Inventory inventory = player.getInventory();
        int hotbarStart = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;

        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
            InventorySlotUI slotUI = slotUIs[i];
            int inventoryIndex = hotbarStart + i;

            if (inventoryIndex < inventory.getSlots().size()) {
                slotUI.setSlot(inventory.getSlot(inventoryIndex));
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
            slotUI.setTooltipText(null);
            return;
        }

        SpriteSheet spriteSheet = getItemSpritesheet(item);

        if (spriteSheet == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setSpriteFrame(0);
            slotUI.setTooltipText(null);
            return;
        }

        slotUI.setSpriteSheet(spriteSheet);
        slotUI.setSpriteFrame(getItemIconColumn(item));
        slotUI.setTooltipText(item.getName());
    }

    private void updateSlots() {
        for (int i = 0; i < slotUIs.length; i++) {
            InventorySlotUI slotUI = slotUIs[i];
            slotUI.setSelected(false);
            slotUI.setHovered(isSlotHovered(slotUI));
        }

        if (player != null) {
            int selected = selectedSlot;
            if (selected >= 0 && selected < slotUIs.length) {
                slotUIs[selected].setSelected(true);
            }
        }
    }

    private void interact() {
        if (!Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            return;
        }

        for (int i = 0; i < slotUIs.length; i++) {
            if (!slotUIs[i].isHovered()) {
                continue;
            }

            selectSlot(i);
            break;
        }
    }

    private boolean isSlotHovered(InventorySlotUI slotUI) {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        float x = slotUI.getAbsoluteX();
        float y = slotUI.getAbsoluteY();
        float width = slotUI.getAbsoluteWidth();
        float height = slotUI.getAbsoluteHeight();
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }

    public void selectSlot(int slot) {
        if (slot < 0 || slot >= K.UI.INVENTORY_COLUMNS) {
            return;
        }

        selectedSlot = slot;
    }

    public void selectNext() {
        selectSlot((selectedSlot + 1) % K.UI.INVENTORY_COLUMNS);
    }

    public void selectPrevious() {
        selectSlot((selectedSlot - 1 + K.UI.INVENTORY_COLUMNS) % K.UI.INVENTORY_COLUMNS);
    }

    public Item getSelectedItem() {
        if (player == null) {
            return null;
        }

        Inventory inventory = player.getInventory();
        int hotbarStart = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        int inventoryIndex = hotbarStart + selectedSlot;
        if (inventoryIndex < 0 || inventoryIndex >= inventory.getSlots().size()) {
            return null;
        }

        InventorySlot slot = inventory.getSlot(inventoryIndex);
        return slot.isEmpty() ? null : slot.getItem();
    }

    public InventorySlot getSelectedInventorySlot() {
        if (player == null) {
            return null;
        }

        Inventory inventory = player.getInventory();
        int hotbarStart = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        int inventoryIndex = hotbarStart + selectedSlot;
        if (inventoryIndex < 0 || inventoryIndex >= inventory.getSlots().size()) {
            return null;
        }

        return inventory.getSlot(inventoryIndex);
    }

    private SpriteSheet getItemSpritesheet(Item item) {
        if (item instanceof Produce) return cropIcons;
        if (item instanceof Crop) return cropIcons;
        if (item instanceof Seed) return seedIcons;
        if (item instanceof Block) return blockIcons;
        if (item instanceof Tool) return toolIcons;
        return null;
    }

    @Override
    public void render() {
        renderChildren();
        renderSelector();
    }

    private void renderSelector() {
        InventorySlotUI slot = slotUIs[selectedSlot];

        float thickness = Settings.getScaledThickness() + 1f;
        float x = slot.getAbsoluteX() - thickness;
        float y = slot.getAbsoluteY() - thickness;
        float size = slot.getAbsoluteWidth() + thickness * 2.0f;

        GUI.drawBorder(x, y, size, size,
                K.UI.UI_HOTBAR_SELECTED_COLOR,
                thickness);
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

    public SpriteSheet getInventoryIcons() {
        return inventoryIcons;
    }

    public void setInventoryIcons(SpriteSheet inventoryIcons) {
        this.inventoryIcons = inventoryIcons;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public InventorySlotUI getSlotUI(int index) {
        if (index < 0 || index >= K.UI.INVENTORY_COLUMNS) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + K.UI.INVENTORY_COLUMNS);
        }

        return slotUIs[index];
    }

    public InventorySlotUI[] getSlotUIs() {
        return slotUIs.clone();
    }
}