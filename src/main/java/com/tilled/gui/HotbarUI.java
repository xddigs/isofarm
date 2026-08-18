package com.tilled.gui;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import com.tilled.input.Mouse;
import com.tilled.utils.K;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@SuppressWarnings("all")
public class HotbarUI extends UIElement {
    private static final float SLOT_SIZE = 48.0f;
    private static final float SLOT_SPACING = 4.0f;
    private static final float PADDING = 6.0f;

    private final InventorySlotUI[] slotUIs = new InventorySlotUI[K.UI.INVENTORY_COLUMNS];

    private final Vector4f backgroundColor = new Vector4f(0.06f, 0.06f, 0.06f, 1.0f);
    private Player player;

    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet toolIcons;

    private int selectedSlot = 0;

    public HotbarUI(float x, float y) {
        super(x, y, getHotbarWidth(), getHotbarHeight());

        setFocusable(true);
        createSlots();
    }

    private static float getHotbarWidth() {
        return PADDING * 2.0f + K.UI.INVENTORY_COLUMNS * SLOT_SIZE + (K.UI.INVENTORY_COLUMNS - 1) * SLOT_SPACING;
    }

    private static float getHotbarHeight() {
        return PADDING * 2.0f + SLOT_SIZE;
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
        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
            float x = PADDING + i * (SLOT_SIZE + SLOT_SPACING);
            float y = PADDING;
            InventorySlotUI slotUI = new InventorySlotUI(x, y, SLOT_SIZE, SLOT_SIZE);
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
        interact();
    }

    private void syncInventory() {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < K.UI.INVENTORY_COLUMNS; i++) {
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

            slotUI.setSelected(selectedSlot == i);
            slotUI.setHovered(isSlotHovered(slotUI));
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

        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
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

        if (selectedSlot < 0 || selectedSlot >= inventory.getSlots().size()) {
            return null;
        }

        InventorySlot slot = inventory.getSlot(selectedSlot);

        return slot.isEmpty() ? null : slot.getItem();
    }

    public InventorySlot getSelectedInventorySlot() {
        if (player == null) {
            return null;
        }

        Inventory inventory = player.getInventory();

        if (selectedSlot < 0 || selectedSlot >= inventory.getSlots().size()) {
            return null;
        }

        return inventory.getSlot(selectedSlot);
    }

    private SpriteSheet getItemSpritesheet(Item item) {
        if (item instanceof Crop) return cropIcons;
        if (item instanceof Seed) return seedIcons;
        if (item instanceof Block) return blockIcons;
        if (item instanceof Tool) return toolIcons;
        return null;
    }

    @Override
    public void render() {
        float x = getAbsoluteX();
        float y = getAbsoluteY();

        GUI.drawRect(x, y, getAbsoluteWidth(), getAbsoluteHeight(),
                new Vector4f(backgroundColor.x, backgroundColor.y, backgroundColor.z,
                        backgroundColor.w * getWorldOpacity()));

        renderChildren();
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