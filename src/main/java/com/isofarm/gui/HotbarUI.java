package com.isofarm.gui;

import com.isofarm.data.Inventory;
import com.isofarm.data.InventorySlot;
import com.isofarm.entity.Player;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.input.Keyboard;
import com.isofarm.input.Mouse;
import com.isofarm.item.Backpack;
import com.isofarm.item.Item;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("all")
public class HotbarUI extends UIElement {
    private final InventorySlotUI[] slotUIs = new InventorySlotUI[K.UI.INVENTORY_COLUMNS];
    private Player player;
    private InventorySlotUI backpackSlotUI;

    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet toolIcons;
    private SpriteSheet materialIcons;
    private SpriteSheet inventoryIcons;

    private int selectedSlot = 0;
    private boolean inventoryMode = false;

    private static final int TOTAL_SELECTABLE_SLOTS = K.UI.INVENTORY_COLUMNS + 1;

    public HotbarUI(float x, float y) {
        super(x, y, getHotbarWidth(), getHotbarHeight());
        setFocusable(true);
        createSlots();
    }

    private static float getHotbarWidth() {
        return Settings.getScaledPadding() * 2.0f +
                (K.UI.INVENTORY_COLUMNS + 1) * Settings.getScaledSlot() +
                (K.UI.INVENTORY_COLUMNS) * Settings.getScaledSpacing() +
                Settings.getScaledSpacing() * 2.0f;
    }

    private static float getHotbarHeight() {
        return Settings.getScaledPadding() * 2.0f +
                Settings.getScaledSlot();
    }

    private void createSlots() {
        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
            float x = Settings.getScaledPadding() + i * (Settings.getScaledSlot() +
                    Settings.getScaledSpacing());

            float y = Settings.getScaledPadding();
            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(),
                    Settings.getScaledSlot());
            slotUIs[i] = slotUI;
            addChild(slotUI);
        }

        float backpackX = Settings.getScaledPadding() +
                K.UI.INVENTORY_COLUMNS * (Settings.getScaledSlot() +
                        Settings.getScaledSpacing()) +
                Settings.getScaledSpacing() * 2.0f;

        backpackSlotUI = new InventorySlotUI(backpackX, Settings.getScaledPadding(),
                Settings.getScaledSlot(), Settings.getScaledSlot());
        addChild(backpackSlotUI);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (player == null) {
            return;
        }

        syncInventory();
        updateSlots();
        interact();
    }

    public void setInventoryMode(boolean inventoryMode) {
        this.inventoryMode = inventoryMode;
    }

    private void syncInventory() {
        if (player == null) return;
        Inventory inventory = player.getInventory();
        int hotbarStart = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
            InventorySlotUI slotUI = slotUIs[i];
            int inventoryIndex = hotbarStart + i;
            slotUI.setSlot(inventoryIndex < inventory.getSlots().size() ?
                    inventory.getSlot(inventoryIndex) : null);
            updateItemSprite(slotUI);
        }

        InventorySlot backpackSlot = player.getInventory().getBackpackSlot();
        backpackSlotUI.setSlot(backpackSlot);
        updateItemSprite(backpackSlotUI);
    }

    private void updateItemSprite(InventorySlotUI slotUI) {
        Item item = slotUI.getItem();

        if (item == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setSpriteFrame(0);
            slotUI.setTooltipText(null);
            return;
        }

        SpriteSheet spriteSheet = ResourceManager.getItemSpriteSheet(item);

        if (spriteSheet == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setSpriteFrame(0);
            slotUI.setTooltipText(null);
            return;
        }

        slotUI.setSpriteSheet(spriteSheet);
        slotUI.setSpriteFrame(ResourceManager.getItemIconColumn(item));
        slotUI.setTooltipText(item.getName());
    }

    private void updateSlots() {
        for (int i = 0; i < slotUIs.length; i++) {
            InventorySlotUI slotUI = slotUIs[i];
            slotUI.setSelected(false);
            slotUI.setHovered(isSlotHovered(slotUI));
        }
        backpackSlotUI.setSelected(false);
        backpackSlotUI.setHovered(isSlotHovered(backpackSlotUI));

        if (player != null) {
            if (selectedSlot >= 0 && selectedSlot < K.UI.INVENTORY_COLUMNS) {
                slotUIs[selectedSlot].setSelected(true);
            } else if (selectedSlot == K.UI.INVENTORY_COLUMNS) {
                backpackSlotUI.setSelected(true);
            }
        }
    }

    private void interact() {
        boolean isLeftClick = Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT);

        if (isLeftClick && isSlotHovered(backpackSlotUI)) {
            selectSlot(K.UI.INVENTORY_COLUMNS);
            if (backpackSlotUI.getItem() instanceof Backpack backpack) {
                backpack.use(player.getGameMaster());
            }
            return;
        }

        if (inventoryMode) {
            return;
        }

        if (!isLeftClick) return;
        for (int i = 0; i < slotUIs.length; i++) {
            if (slotUIs[i].isHovered()) {
                selectSlot(i);
                break;
            }
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
        if (slot < 0 || slot >= TOTAL_SELECTABLE_SLOTS) {
            return;
        }

        selectedSlot = slot;
    }

    public void selectNext() {
        selectSlot((selectedSlot + 1) % TOTAL_SELECTABLE_SLOTS);
    }

    public void selectPrevious() {
        selectSlot((selectedSlot - 1 + TOTAL_SELECTABLE_SLOTS) % TOTAL_SELECTABLE_SLOTS);
    }

    public Item getSelectedItem() {
        InventorySlot slot = getSelectedInventorySlot();
        return (slot == null || slot.isEmpty()) ? null : slot.getItem();
    }

    public InventorySlot getSelectedInventorySlot() {
        if (player == null) {
            return null;
        }

        if (selectedSlot == K.UI.INVENTORY_COLUMNS) {
            return player.getInventory().getBackpackSlot();
        }

        Inventory inventory = player.getInventory();
        int hotbarStart = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        int inventoryIndex = hotbarStart + selectedSlot;
        if (inventoryIndex < 0 || inventoryIndex >= inventory.getSlots().size()) {
            return null;
        }

        return inventory.getSlot(inventoryIndex);
    }

    @Override
    public void render() {
        renderChildren();
        renderSelector();
    }

    private void renderSelector() {
        InventorySlotUI slot = (selectedSlot == K.UI.INVENTORY_COLUMNS) ?
                backpackSlotUI : slotUIs[selectedSlot];

        float thickness = Settings.getScaledThickness() * 2;
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

    public SpriteSheet getMaterialIcons() {
        return materialIcons;
    }

    public void setMaterialIcons(SpriteSheet materialIcons) {
        this.materialIcons = materialIcons;
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
        if (index < 0 || index >= TOTAL_SELECTABLE_SLOTS) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + TOTAL_SELECTABLE_SLOTS);
        }

        if (index == K.UI.INVENTORY_COLUMNS) {
            return backpackSlotUI;
        }

        return slotUIs[index];
    }

    public InventorySlotUI[] getSlotUIs() {
        return slotUIs.clone();
    }
}