package com.isofarm.gui;

import com.isofarm.data.Inventory;
import com.isofarm.data.InventorySlot;
import com.isofarm.data.SlotType;
import com.isofarm.entity.Player;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.input.Keyboard;
import com.isofarm.input.Mouse;
import com.isofarm.item.Backpack;
import com.isofarm.item.Book;
import com.isofarm.item.Item;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class HotbarUI extends UIElement {
    private final InventorySlotUI[] slotUIs = new InventorySlotUI[K.UI.INVENTORY_COLUMNS];
    private Player player;
    private Inventory inventory;
    private InventorySlotUI backpackSlotUI;
    private InventorySlotUI bookSlotUI;
    private Item lastSelectedItem;

    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet toolIcons;
    private SpriteSheet materialIcons;
    private SpriteSheet inventoryIcons;

    private int selectedSlot = 0;
    private boolean inventoryMode = false;

    public HotbarUI(float x, float y) {
        super(x, y, getInitialHotbarWidth(), getHotbarHeight());
        setFocusable(true);
        createSlots();
        setLayer(50);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (player == null) {
            return;
        }

        syncInventory();
        syncExtraSlots();
        updateSlots();
        interact();
        updateSelectedItem();
    }

    @Override
    public void render() {
        renderChildren();
        renderSelector();
    }

    public Item getSelectedItem() {
        InventorySlot slot = getSelectedInventorySlot();
        return slot != null ? slot.getItem() : null;
    }

    private void updateSelectedItem() {
        InventorySlot selected = getSelectedInventorySlot();
        Item item = selected != null ? selected.getItem() : null;

        if (item != lastSelectedItem) {
            lastSelectedItem = item;
            Settings.selectedItem = item;
        }
    }

    private static float getInitialHotbarWidth() {
        float padding = Settings.getScaledPadding();
        float slot = Settings.getScaledSlot();
        float spacing = Settings.getScaledSpacing();

        return padding * 2.0f + K.UI.INVENTORY_COLUMNS * slot
                + (K.UI.INVENTORY_COLUMNS - 1) * spacing;
    }

    public float getHotbarWidth() {
        float padding = Settings.getScaledPadding();
        float slot = Settings.getScaledSlot();
        float spacing = Settings.getScaledSpacing();
        float width = padding * 2.0f
                + K.UI.INVENTORY_COLUMNS * slot
                + (K.UI.INVENTORY_COLUMNS - 1) * spacing;

        if (inventory != null && inventory.hasBackpackEquipped()) {
            width += spacing * 2.0f;
            width += slot;
        }

        if (inventory != null && inventory.hasBookEquipped()) {
            width += spacing * 2.0f;
            width += slot;
        }

        return width;
    }

    private static float getHotbarHeight() {
        return Settings.getScaledPadding() * 2.0f +
                Settings.getScaledSlot();
    }

    public int getMaxSelectableSlots() {
        int max = K.UI.INVENTORY_COLUMNS;
        if (inventory != null && inventory.hasBackpackEquipped()) {
            max++;
        }
        if (inventory != null && inventory.hasBookEquipped()) {
            max++;
        }
        return max;
    }

    public void selectSlot(int index) {
        if (index < 0 || index >= getMaxSelectableSlots()) {
            return;
        }
        selectedSlot = index;
    }

    public void selectNext() {
        selectSlot((selectedSlot + 1) % getMaxSelectableSlots());
    }

    public void selectPrevious() {
        int max = getMaxSelectableSlots();
        selectSlot((selectedSlot - 1 + max) % max);
    }

    public void refreshSize() {
        setWidth(getHotbarWidth());
    }

    private int getExtraSlotIndex(Item item) {
        if (inventory == null || item == null) {
            return -1;
        }
        List<InventorySlot> extras = inventory.getEquippedExtraItems();
        for (int i = 0; i < extras.size(); i++) {
            if (extras.get(i).getItem().equals(item)) {
                return K.UI.INVENTORY_COLUMNS + i;
            }
        }

        return -1;
    }

    private InventorySlotUI getExtraSlotUI(int index) {
        if (inventory == null) {
            return null;
        }

        int extraIndex = index - K.UI.INVENTORY_COLUMNS;
        List<InventorySlot> extras = inventory.getEquippedExtraItems();
        if (extraIndex < 0 || extraIndex >= extras.size()) {
            return null;
        }

        Item item = extras.get(extraIndex).getItem();
        if (item instanceof Backpack) {
            return backpackSlotUI;
        }

        if (item instanceof Book) {
            return bookSlotUI;
        }
        return null;
    }

    private void createSlots() {
        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
            float x = Settings.getScaledPadding() + i * (Settings.getScaledSlot() +
                    Settings.getScaledSpacing());

            float y = Settings.getScaledPadding();
            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(),
                    Settings.getScaledSlot(),
                    SlotType.HOTBAR);
            slotUIs[i] = slotUI;
            addChild(slotUI);
        }

        float backpackX = Settings.getScaledPadding() +
                K.UI.INVENTORY_COLUMNS * (Settings.getScaledSlot() +
                        Settings.getScaledSpacing()) +
                Settings.getScaledSpacing() * 2.0f;

        backpackSlotUI = new InventorySlotUI(backpackX, Settings.getScaledPadding(),
                Settings.getScaledSlot(), Settings.getScaledSlot(),
                SlotType.HOTBAR);

        backpackSlotUI.hide();
        addChild(backpackSlotUI);

        float bookX = backpackX + Settings.getScaledSlot() + Settings.getScaledSpacing();

        bookSlotUI = new InventorySlotUI(bookX, Settings.getScaledPadding(),
                Settings.getScaledSlot(), Settings.getScaledSlot(),
                SlotType.HOTBAR);

        bookSlotUI.hide();
        addChild(bookSlotUI);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setInventoryMode(boolean inventoryMode) {
        this.inventoryMode = inventoryMode;
    }

    private void syncInventory() {
        if (player == null) {
            return;
        }

        int hotbarStart = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
            InventorySlotUI slotUI = slotUIs[i];
            int inventoryIndex = hotbarStart + i;
            slotUI.setSlot(inventoryIndex < inventory.getSlots().size() ?
                    inventory.getSlot(inventoryIndex) : null);
            updateItemSprite(slotUI);
        }
    }

    private void syncExtraSlots() {
        backpackSlotUI.hide();
        bookSlotUI.hide();

        if (inventory == null) {
            return;
        }

        List<InventorySlot> extras = inventory.getEquippedExtraItems();
        for (InventorySlot extra : extras) {
            Item item = extra.getItem();

            if (item instanceof Backpack) {
                backpackSlotUI.setSlot(inventory.getBackpackSlot());
                updateItemSprite(backpackSlotUI);
                backpackSlotUI.show();

            } else if (item instanceof Book) {
                bookSlotUI.setSlot(inventory.getBookSlot());
                updateItemSprite(bookSlotUI);
                bookSlotUI.show();
            }
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

        SpriteSheet spriteSheet = ResourceManager.getItemSpriteSheet(item);
        if (spriteSheet == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setSpriteFrame(0);
            slotUI.setTooltipText(null);
            return;
        }

        slotUI.setSpriteSheet(spriteSheet);
        slotUI.setSpriteFrame(ResourceManager.getItemFrame(item));
        slotUI.setTooltipText(item.getName());
    }

    private void updateSlots() {
        for (InventorySlotUI slotUI : slotUIs) {
            slotUI.setSelected(false);
            slotUI.setHovered(isSlotHovered(slotUI));
        }

        backpackSlotUI.setSelected(false);
        backpackSlotUI.setHovered(isSlotHovered(backpackSlotUI));

        bookSlotUI.setSelected(false);
        bookSlotUI.setHovered(isSlotHovered(bookSlotUI));

        if (player == null || inventory == null) {
            return;
        }

        if (selectedSlot >= 0 &&
                selectedSlot < K.UI.INVENTORY_COLUMNS) {
            slotUIs[selectedSlot].setSelected(true);
            return;
        }

        int extraIndex = selectedSlot - K.UI.INVENTORY_COLUMNS;

        if (extraIndex < 0) {
            return;
        }

        List<InventorySlot> extras = inventory.getEquippedExtraItems();
        if (extraIndex >= extras.size()) {
            return;
        }

        Item item = extras.get(extraIndex).getItem();
        if (item instanceof Backpack) {
            backpackSlotUI.setSelected(true);
        } else if (item instanceof Book) {
            bookSlotUI.setSelected(true);
        }
    }

    private void interact() {
        boolean isLeftClick = Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT);
        boolean isCtrlHeld = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL) ||
                Keyboard.isKeyDown(GLFW_KEY_RIGHT_CONTROL);

        if (isLeftClick) {
            if (backpackSlotUI.isVisible() && isSlotHovered(backpackSlotUI)) {
                int index = getExtraSlotIndex(backpackSlotUI.getItem());
                if (index >= 0) {
                    selectSlot(index);
                }
                if (backpackSlotUI.getItem() instanceof Backpack backpack) {
                    backpack.use(player.getGameMaster(), isCtrlHeld);
                }
                return;
            }

            if (bookSlotUI.isVisible() && isSlotHovered(bookSlotUI)) {
                int index = getExtraSlotIndex(bookSlotUI.getItem());
                if (index >= 0) {
                    selectSlot(index);
                }
                if (bookSlotUI.getItem() instanceof Book book) {
                    book.use(player.getGameMaster(), isCtrlHeld);
                }
                return;
            }
        }

        if (inventoryMode) return;
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

    public InventorySlot getSelectedInventorySlot() {
        if (player == null || inventory == null) {
            return null;
        }

        int extraIndex = selectedSlot - K.UI.INVENTORY_COLUMNS;
        if (extraIndex >= 0) {
            List<InventorySlot> extras = inventory.getEquippedExtraItems();
            if (extraIndex >= extras.size()) {
                return null;
            }

            Item item = extras.get(extraIndex).getItem();
            if (item instanceof Backpack) {
                return inventory.getBackpackSlot();
            }

            if (item instanceof Book) {
                return inventory.getBookSlot();
            }
            return null;
        }

        int hotbarStart = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        int inventoryIndex = hotbarStart + selectedSlot;
        if (inventoryIndex < 0 ||
                inventoryIndex >= inventory.getSlots().size()) {
            return null;
        }
        return inventory.getSlot(inventoryIndex);
    }

    private void renderSelector() {
        InventorySlotUI slot;
        if (selectedSlot < K.UI.INVENTORY_COLUMNS) {
            if (selectedSlot < 0 || selectedSlot >= slotUIs.length) {
                return;
            }
            slot = slotUIs[selectedSlot];
        } else {
            slot = getExtraSlotUI(selectedSlot);
            if (slot == null || !slot.isVisible()) {
                return;
            }
        }

        float thickness = Settings.getScaledThickness() * 2;
        float x = slot.getAbsoluteX() - thickness;
        float y = slot.getAbsoluteY() - thickness;
        float size = slot.getAbsoluteWidth() + thickness * 2.0f;
        GUI.drawBorder(x, y, size, size, K.UI.UI_HOTBAR_SELECTED_COLOR, thickness);
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

    public InventorySlotUI[] getSlotUIs() {
        return slotUIs.clone();
    }
}