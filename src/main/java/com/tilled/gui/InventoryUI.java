package com.tilled.gui;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import com.tilled.input.Mouse;
import com.tilled.utils.K;
import com.tilled.utils.Settings;
import com.tilled.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

@SuppressWarnings("all")
public class InventoryUI extends UIElement {
    private final InventorySlotUI[] slotUIs;
    private UIButton sortButton;
    private UIButton groupButton;

    private Player player;

    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet toolIcons;
    private SpriteSheet inventoryIcons;

    private Item carriedItem;
    private int carriedAmount;

    private HotbarUI hotbarUI;
    private GameMaster gameMaster;

    public InventoryUI(float x, float y) {
        super(x, y, getInventoryWidth(), getInventoryHeight());
        int totalVisualSlots = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        this.slotUIs = new InventorySlotUI[totalVisualSlots];

        setFocusable(true);
        createButtons();
        createSlots();
    }

    private static float getInventoryWidth() {
        return Settings.getScaledPadding() * 2.0f +
                K.UI.INVENTORY_COLUMNS * Settings.getScaledSlot() +
                (K.UI.INVENTORY_COLUMNS - 1) * Settings.getScaledSpacing();
    }

    private static float getInventoryHeight() {
        return Settings.getScaledPadding() * 2.0f + Settings.getScaledHeader() +
                K.UI.INVENTORY_ROWS * Settings.getScaledSlot() +
                (K.UI.INVENTORY_ROWS - 1) * Settings.getScaledSpacing();
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

    private void createButtons() {
        float btnWidth = Settings.getScaledButton() * 1.5f;
        float btnHeight = Settings.getScaledHeader() - Settings.getScaledPadding();
        sortButton = new UIButton(Settings.getScaledPadding(), Settings.getScaledPadding(), btnWidth, btnHeight);
        groupButton = new UIButton(Settings.getScaledPadding() + btnWidth + Settings.getScaledSpacing(), Settings.getScaledPadding(), btnWidth, btnHeight);

        sortButton.setOnClick(this::sortInventory);
        groupButton.setOnClick(this::groupInventory);

        sortButton.setTooltipText("Sort Inventory");
        groupButton.setTooltipText("Group Items");

        addChild(sortButton);
        addChild(groupButton);
    }

    private void createSlots() {
        for (int i = 0; i < slotUIs.length; i++) {
            int column = i % K.UI.INVENTORY_COLUMNS;
            int row = i / K.UI.INVENTORY_COLUMNS;
            float x = Settings.getScaledPadding() + column * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            float y = Settings.getScaledPadding() + Settings.getScaledHeader() + row * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(), Settings.getScaledSlot());

            slotUIs[i] = slotUI;
            addChild(slotUI);
        }
    }

    public void sortInventory() {
        if (player != null && player.getInventory() != null) {
            player.getInventory().sort();
        }
    }

    public void groupInventory() {
        if (player != null && player.getInventory() != null) {
            player.getInventory().group();
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (player == null) {
            return;
        }

        boolean wasOpen = isVisible();

        boolean isOpen = gameMaster != null && gameMaster.isInventoryOpen();

        if (isOpen && !wasOpen) {
            show();
            onOpen();
        } else if (!isOpen && wasOpen) {
            hide();
            onClose();
        }

        syncInventory();
        updateSlots();
        slotInteract();
    }

    private void onOpen() {
        if (hotbarUI == null) {
            return;
        }

        float hotbarX = getAbsoluteX() + getWidth() / 2f - hotbarUI.getWidth() / 2f;
        float hotbarY = getAbsoluteY() + getHeight() + K.UI.HOTBAR_OFFSET;
        hotbarUI.setPosition(hotbarX, hotbarY);
        hotbarUI.setInventoryMode(true);
    }

    private void onClose() {
        if (hotbarUI != null) {
            if (gameMaster != null && gameMaster.getGameUIService() != null) {
                gameMaster.getGameUIService().resetHotbarPosition();
            }
            hotbarUI.setInventoryMode(false);
        }
        returnCarriedItem();
    }

    private void returnCarriedItem() {
        if (carriedItem == null || carriedAmount <= 0 || player == null) {

            clearCarriedItem();
            return;
        }

        int remaining = player.getInventory().add(carriedItem, carriedAmount);

        if (remaining <= 0) {
            clearCarriedItem();
        } else {
            carriedAmount = remaining;
        }
    }

    private void clearCarriedItem() {
        carriedItem = null;
        carriedAmount = 0;
    }

    private void syncInventory() {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < slotUIs.length; i++) {
            InventorySlotUI slotUI = slotUIs[i];

            if (i < inventory.getSlots().size()) {
                slotUI.setSlot(inventory.getSlot(i));
            } else {
                slotUI.setSlot(null);
            }

            updateItemSprite(slotUI);
        }

        if (sortButton.getSpriteSheet() == null && inventoryIcons != null) {

            sortButton.setSpriteSheet(inventoryIcons);
            sortButton.setSpriteFrame(0);

            groupButton.setSpriteSheet(inventoryIcons);
            groupButton.setSpriteFrame(1);
        }
    }

    private void updateItemSprite(InventorySlotUI slotUI) {
        Item item = slotUI.getItem();

        if (item == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setTooltipText(null);
            return;
        }

        slotUI.setSpriteSheet(getItemSpritesheet(item));
        slotUI.setSpriteFrame(getItemIconColumn(item));
        slotUI.setTooltipText(item.getName());
    }

    private SpriteSheet getItemSpritesheet(Item item) {
        if (item instanceof Produce) {
            return cropIcons;
        }

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

    private void updateSlots() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        for (InventorySlotUI slotUI : slotUIs) {
            if (slotUI != null) {
                slotUI.setHovered(slotUI.contains(mouseX, mouseY));
            }
        }

        if (hotbarUI != null) {
            for (InventorySlotUI slotUI : hotbarUI.getSlotUIs()) {

                if (slotUI != null) {
                    slotUI.setHovered(slotUI.contains(mouseX, mouseY));
                }
            }
        }
    }

    private void slotInteract() {
        if (hotbarUI == null) {
            return;
        }

        InventorySlotUI[] hotbarSlots = hotbarUI.getSlotUIs();
        InventorySlotUI[] allSlots = new InventorySlotUI[slotUIs.length + hotbarSlots.length];
        System.arraycopy(slotUIs, 0, allSlots, 0, slotUIs.length);
        System.arraycopy(hotbarSlots, 0, allSlots, slotUIs.length, hotbarSlots.length);

        for (InventorySlotUI slotUI : allSlots) {
            if (slotUI == null || !slotUI.isHovered()) {
                continue;
            }

            InventorySlot slot = slotUI.getSlot();
            if (slot == null) {
                continue;
            }

            if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                leftClick(slot);
                break;
            }

            if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                rightClick(slot);
                break;
            }
        }
    }

    private void leftClick(InventorySlot slot) {
        if (carriedItem == null) {
            pickEntireStack(slot);
            return;
        }

        if (slot.isEmpty()) {
            placeEntireStack(slot);
            return;
        }

        if (isSameType(carriedItem, slot.getItem())) {

            mergeCarriedStack(slot);
        } else {
            swapStacks(slot);
        }
    }

    private void pickEntireStack(InventorySlot slot) {
        if (slot.isEmpty()) {
            return;
        }

        carriedItem = slot.getItem();
        carriedAmount = slot.getAmount();

        slot.clear();
    }

    private void placeEntireStack(InventorySlot slot) {
        slot.setItem(carriedItem);
        slot.setAmount(carriedAmount);

        clearCarriedItem();
    }

    private void mergeCarriedStack(InventorySlot slot) {
        int maxStack = player.getInventory().getMaxStack(carriedItem);

        int space = maxStack - slot.getAmount();

        if (space <= 0) {
            return;
        }

        int moved = Math.min(space, carriedAmount);

        slot.addAmount(moved);
        carriedAmount -= moved;

        if (carriedAmount <= 0) {
            clearCarriedItem();
        }
    }

    private void swapStacks(InventorySlot slot) {
        Item tempItem = slot.getItem();
        int tempAmount = slot.getAmount();

        slot.setItem(carriedItem);
        slot.setAmount(carriedAmount);

        carriedItem = tempItem;
        carriedAmount = tempAmount;
    }

    private void rightClick(InventorySlot slot) {
        if (carriedItem == null) {
            takeHalf(slot);
            return;
        }

        if (slot.isEmpty()) {
            placeOne(slot);
            return;
        }

        if (!isSameType(carriedItem, slot.getItem())) {
            return;
        }

        addOneToSlot(slot);
    }

    private void takeHalf(InventorySlot slot) {
        if (slot.isEmpty()) {
            return;
        }

        int splitAmount = (int) Math.ceil(slot.getAmount() / 2.0);

        carriedItem = slot.getItem();
        carriedAmount = splitAmount;

        slot.setAmount(slot.getAmount() - splitAmount);
    }

    private void placeOne(InventorySlot slot) {
        int maxStack = player.getInventory().getMaxStack(carriedItem);

        if (maxStack <= 0) {
            return;
        }

        slot.setItem(carriedItem);
        slot.setAmount(1);

        carriedAmount--;

        if (carriedAmount <= 0) {
            clearCarriedItem();
        }
    }

    private void addOneToSlot(InventorySlot slot) {
        int maxStack = player.getInventory().getMaxStack(slot.getItem());

        if (slot.getAmount() >= maxStack) {
            return;
        }

        slot.addAmount(1);
        carriedAmount--;

        if (carriedAmount <= 0) {
            clearCarriedItem();
        }
    }

    private boolean isSameType(Item a, Item b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.getClass() != b.getClass()) {
            return false;
        }

        if (a instanceof Produce p1 && b instanceof Produce p2) {
            return p1.getType() == p2.getType();
        }

        if (a instanceof Seed s1 && b instanceof Seed s2) {
            return s1.getType() == s2.getType();
        }

        if (a instanceof Crop c1 && b instanceof Crop c2) {
            return c1.getCropType() == c2.getCropType();
        }

        if (a instanceof Block b1 && b instanceof Block b2) {
            return b1.getType() == b2.getType();
        }

        if (a instanceof Tool t1 && b instanceof Tool t2) {
            return t1.getId() == t2.getId();
        }

        return a.getName().equals(b.getName());
    }

    @Override
    public void render() {
        GUI.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteWidth(),
                getAbsoluteHeight(), K.UI.UI_BACKGROUND_COLOR, Settings.getScaledCornerRadius(),
                K.UI.UI_BORDER_COLOR, Settings.getScaledThickness());

        renderChildren();
        renderCarriedItem();
    }

    private void renderCarriedItem() {
        if (carriedItem == null || carriedAmount <= 0) {
            return;
        }

        SpriteSheet sheet = getItemSpritesheet(carriedItem);
        if (sheet == null) {
            return;
        }

        float iconSize = Settings.getScaledIcon();
        float x = Mouse.getX() - iconSize / 2f;
        float y = Mouse.getY() - iconSize / 2f;
        GUI.drawSprite(sheet, getItemIconColumn(carriedItem), x, y,
                iconSize, iconSize, K.UI.UI_ITEM_TINT);

        if (carriedAmount > 1) {
            String amount = String.valueOf(carriedAmount);

            GUI.drawString(amount, x + iconSize - 10, y + iconSize - 10,
                    GUI.getNormalFont(), K.UI.UI_TEXT_COLOR);
        }
    }

    public void setIcons(SpriteSheet seed, SpriteSheet crop, SpriteSheet block, SpriteSheet tool, SpriteSheet inv) {
        this.seedIcons = seed;
        this.cropIcons = crop;
        this.blockIcons = block;
        this.toolIcons = tool;
        this.inventoryIcons = inv;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setSeedIcons(SpriteSheet seedIcons) {
        this.seedIcons = seedIcons;

        if (hotbarUI != null) {
            hotbarUI.setSeedIcons(seedIcons);
        }
    }

    public void setCropIcons(SpriteSheet cropIcons) {
        this.cropIcons = cropIcons;

        if (hotbarUI != null) {
            hotbarUI.setCropIcons(cropIcons);
        }
    }

    public void setBlockIcons(SpriteSheet blockIcons) {
        this.blockIcons = blockIcons;

        if (hotbarUI != null) {
            hotbarUI.setBlockIcons(blockIcons);
        }
    }

    public void setToolIcons(SpriteSheet toolIcons) {
        this.toolIcons = toolIcons;

        if (hotbarUI != null) {
            hotbarUI.setToolIcons(toolIcons);
        }
    }

    public void setInventoryIcons(SpriteSheet inventoryIcons) {
        this.inventoryIcons = inventoryIcons;

        if (hotbarUI != null) {
            hotbarUI.setInventoryIcons(inventoryIcons);
        }
    }

    public void setHotbarUI(GameMaster gameMaster, HotbarUI hotbarUI) {
        this.gameMaster = gameMaster;
        this.hotbarUI = hotbarUI;
    }

    public Item getSelectedItem() {
        if (hotbarUI != null) {
            return hotbarUI.getSelectedItem();
        }

        return null;
    }
}