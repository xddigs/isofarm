package com.isofarm.gui;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.input.Mouse;
import com.isofarm.item.Block;
import com.isofarm.item.Item;
import com.isofarm.item.Tool;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides inventory ui behavior.
 */
@SuppressWarnings("all")
public class InventoryUI extends UIElement {
    private static final int BACKPACK_COLUMNS = 4;
    private static final int BACKPACK_ROWS = 4;
    private static final Logger log = LoggerFactory.getLogger(InventoryUI.class);

    private final InventorySlotUI[] slotUIs;

    private final List<UIButton> buttons;
    private UIButton sortButton;
    private UIButton groupButton;
    private UIButton backpackButton;
    private final Player player = Player.plyr;
    private Inventory inventory;
    private GameMaster gameMaster;
    private Tab currentTab;
    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet toolIcons;
    private SpriteSheet materialIcons;
    private SpriteSheet inventoryIcons;
    private Item carriedItem;
    private HotbarUI hotbarUI;
    private BackpackInventoryUI backpackUI;
    private int carriedAmount;

    private float defaultX;
    private float targetX;
    private float defaultY;
    private float targetY;
    private boolean isClosing = false;

    private boolean isBackpackOpen = false;
    private boolean isBackpackClosing = false;
    private float backpackTargetY;
    private float backpackCurrentY;

    /**
     * Creates a new {@code InventoryUI} instance.
     * @param x the x value
     * @param y the y value
     */
    public InventoryUI(float x, float y) {
        super(x, y, getInventoryWidth(), getInventoryHeight());
        defaultX = x;
        targetX = x;
        defaultY = y;
        targetY = y + 1000.0f;
        setPosition(x, targetY);

        int totalVisualSlots = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        this.slotUIs = new InventorySlotUI[totalVisualSlots];
        this.buttons = new ArrayList<>();
        this.currentTab = Tab.INVENTORY;
        setFocusable(true);
        createButtons();

        setLayer(150);
        hide();
    }

    /**
     * Returns the inventory width.
     * @return the inventory width
     */
    private static float getInventoryWidth() {
        return Settings.getScaledPadding() * 2.0f +
                K.UI.INVENTORY_COLUMNS * Settings.getScaledSlot() +
                (K.UI.INVENTORY_COLUMNS - 1) * Settings.getScaledSpacing();
    }

    /**
     * Returns the inventory height.
     * @return the inventory height
     */
    private static float getInventoryHeight() {
        return Settings.getScaledPadding() * 2.0f + Settings.getScaledHeader() +
                K.UI.INVENTORY_ROWS * Settings.getScaledSlot() +
                (K.UI.INVENTORY_ROWS - 1) * Settings.getScaledSpacing();
    }

    /**
     * Returns the backpack width.
     * @return the backpack width
     */
    public static float getBackpackWidth() {
        return Settings.getScaledPadding() * 2.0f +
                BACKPACK_COLUMNS * Settings.getScaledSlot() +
                (BACKPACK_COLUMNS - 1) * Settings.getScaledSpacing();
    }

    /**
     * Returns the backpack height.
     * @return the backpack height
     */
    public static float getBackpackHeight() {
        return Settings.getScaledPadding() * 2.0f + Settings.getScaledHeader() +
                BACKPACK_ROWS * Settings.getScaledSlot() +
                (BACKPACK_ROWS - 1) * Settings.getScaledSpacing();
    }

    /**
     * Returns the slot uis.
     * @return the slot uis
     */
    public InventorySlotUI[] getSlotUIs() {
        return slotUIs;
    }

    /**
     * Creates and returns the buttons.
     */
    private void createButtons() {
        float btnWidth = Settings.getScaledSlot(), btnHeight = Settings.getScaledSlot();
        sortButton = new UIButton(Settings.getScaledPadding(),
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        groupButton = new UIButton(Settings.getScaledPadding() + btnWidth + Settings.getScaledSpacing(),
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        backpackButton = new UIButton(Settings.getScaledPadding() + btnWidth * 2 + Settings.getScaledSpacing() * 3,
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        sortButton.setOnClick(this::sortInventory);
        groupButton.setOnClick(this::groupInventory);
        backpackButton.setOnClick(() -> {
            if (isBackpackOpen && !isBackpackClosing) {
                closeBackpack();
            } else if (!isBackpackOpen) {
                openBackpack(gameMaster.getGameUIService().getBackpackInventoryUI());
            }
        });

        sortButton.setTooltipText("inventory.sort");
        groupButton.setTooltipText("inventory.group");
        backpackButton.setTooltipText("inventory.backpack");
        backpackButton.hide();

        buttons.add(sortButton);
        buttons.add(groupButton);
        buttons.add(backpackButton);

        addChild(sortButton);
        addChild(groupButton);
        addChild(backpackButton);
    }

    /**
     * Creates and returns the slots.
     */
    public void createSlots() {
        for (int i = 0; i < slotUIs.length; i++) {
            int column = i % K.UI.INVENTORY_COLUMNS;
            int row = i / K.UI.INVENTORY_COLUMNS;
            float x = Settings.getScaledPadding() + column * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            float y = Settings.getScaledPadding() + Settings.getScaledHeader() + row * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(), Settings.getScaledSlot(),
                    SlotType.INVENTORY);

            slotUIs[i] = slotUI;
            addChild(slotUI);
        }
    }

    /**
     * Returns the buttons.
     * @return the buttons
     */
    public List<UIButton> getButtons() {
        return buttons;
    }

    /**
     * Performs the sort inventory operation.
     */
    public void sortInventory() {
        if (player != null && inventory != null) {
            inventory.sort();
        }
    }

    /**
     * Performs the group inventory operation.
     */
    public void groupInventory() {
        if (player != null && inventory != null) {
            inventory.group();
        }
    }

    /**
     * Updates the position.
     * @param delta the delta value
     */
    private void updatePosition(float delta) {
        float currentX = getX();
        float currentY = getY();

        if (Math.abs(targetX - currentX) > 0.1f) {
            float newX = currentX + (targetX - currentX) * Math.min(1.0f, delta * 15.0f);
            setPosition(newX, getY());
        } else {
            setPosition(targetX, getY());
        }

        if (Math.abs(targetY - currentY) > 0.1f) {
            float newY = currentY + (targetY - currentY) * Math.min(1.0f, delta * 15.0f);
            setPosition(getX(), newY);
        } else {
            setPosition(getX(), targetY);
            if (isClosing) {
                super.hide();
                isClosing = false;
            }
        }

        if (backpackUI != null && backpackUI.isVisible()) {
            float bpX = getX() + (getWidth() - backpackUI.getWidth()) / 2.0f;
            if (Math.abs(backpackTargetY - backpackCurrentY) > 0.1f) {
                backpackCurrentY += (backpackTargetY - backpackCurrentY) * Math.min(1.0f, delta * 15.0f);
            } else {
                backpackCurrentY = backpackTargetY;
                if (isBackpackClosing) {
                    backpackUI.hide();
                    isBackpackOpen = false;
                    isBackpackClosing = false;
                }
            }
            backpackUI.setPosition(bpX, backpackCurrentY);
        }
    }

    /**
     * Performs the open backpack operation.
     * @param backpackUI the backpack ui value
     */
    public void openBackpack(BackpackInventoryUI backpackUI) {
        if (backpackUI == null) return;
        this.backpackUI = backpackUI;
        this.isBackpackOpen = true;
        this.isBackpackClosing = false;
        this.backpackUI.show();

        float spacing = Settings.getScaledSpacing() * 2.0f;
        float pushOffset = (backpackUI.getHeight() + spacing) / 2.0f;
        this.targetY = this.defaultY + pushOffset;
        this.backpackCurrentY = -backpackUI.getHeight();
        this.backpackTargetY = this.targetY - backpackUI.getHeight() - spacing;

        this.backpackUI.setPosition(getX() + (getWidth() - backpackUI.getWidth()) / 2.0f, backpackCurrentY);
    }

    /**
     * Performs the close backpack operation.
     */
    public void closeBackpack() {
        if (!isBackpackOpen || isBackpackClosing) return;
        this.isBackpackClosing = true;
        this.targetY = this.defaultY;
        if (backpackUI != null) {
            this.backpackTargetY = this.defaultY - backpackUI.getHeight() - Settings.getScaledSpacing() * 2.0f;
        }
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    @Override
    public void update(float delta) {
        super.update(delta);
        updatePosition(delta);

        if (player == null) return;
        boolean wasOpen = isVisible();
        boolean isOpen = gameMaster != null && gameMaster.isInventoryOpen();

        if (isOpen && !wasOpen) {
            show();
            onOpen();
        } else if (!isOpen && wasOpen) {
            onClose();
        }

        syncInventory();
        updateSlots();
        slotInteract();
    }

    /**
     * Performs the on open operation.
     */
    private void onOpen() {
        isClosing = false;
        closeBackpack();

        if (gameMaster != null) {
            this.defaultX = (gameMaster.getWindowWidth() - getWidth()) / 2.0f;
            this.targetX = defaultX;
            this.defaultY = (gameMaster.getWindowHeight() - getHeight()) / 2.0f;
            this.targetY = defaultY;
            setPosition(defaultX, gameMaster.getWindowHeight());
        }

        if (hotbarUI != null) {
            hotbarUI.setInventoryMode(true);
        }
    }

    /**
     * Performs the on close operation.
     */
    private void onClose() {
        if (backpackUI != null) {
            backpackUI.hide();
            isBackpackOpen = false;
            isBackpackClosing = false;
        }

        if (gameMaster != null) {
            this.targetY = gameMaster.getWindowHeight();
            this.isClosing = true;
        }

        if (hotbarUI != null) {
            hotbarUI.setInventoryMode(false);
        }
        returnCarriedItem();
    }

    /**
     * Performs the hide operation.
     * @return the hide result
     */
    @Override
    public UIElement hide() {
        super.hide();
        this.isClosing = false;
        return this;
    }

    /**
     * Performs the return carried item operation.
     */
    private void returnCarriedItem() {
        if (carriedItem == null || carriedAmount <= 0 || player == null) {
            clearCarriedItem();
            return;
        }

        int remaining = inventory.add(carriedItem, carriedAmount);

        if (remaining <= 0) {
            clearCarriedItem();
        } else {
            carriedAmount = remaining;
        }
    }

    /**
     * Clears the carried item.
     */
    private void clearCarriedItem() {
        carriedItem = null;
        carriedAmount = 0;
    }

    /**
     * Performs the sync inventory operation.
     */
    protected void syncInventory() {
        if (inventory == null) return;

        for (int i = 0; i < slotUIs.length; i++) {
            InventorySlotUI slotUI = slotUIs[i];
            if (slotUI == null) continue;

            if (i < (inventory.getSlots().size())) {
                slotUI.setSlot((inventory.getSlot(i)));
            } else {
                slotUI.setSlot(null);
            }

            updateItemSprite(slotUI);
        }

        if (backpackUI != null && backpackUI.getSlotUIs() != null) {
            Inventory backpackInv = player.getBackpack();
            for (int i = 0; i < backpackUI.getSlotUIs().length; i++) {
                InventorySlotUI slotUI = backpackUI.getSlotUIs()[i];
                if (slotUI == null) continue;

                if (backpackInv != null && i < backpackInv.getSlots().size()) {
                    slotUI.setSlot(backpackInv.getSlot(i));
                } else {
                    slotUI.setSlot(null);
                }
                updateItemSprite(slotUI);
            }
        }

        if (sortButton.getSpriteSheet() == null && inventoryIcons != null) {
            sortButton.setSpriteSheet(inventoryIcons);
            sortButton.setSpriteColumn(0);

            groupButton.setSpriteSheet(inventoryIcons);
            groupButton.setSpriteColumn(1);

            backpackButton.setSpriteSheet(inventoryIcons);
            backpackButton.setSpriteColumn(2);
        }
    }

    /**
     * Updates the item sprite.
     * @param slotUI the slot ui value
     */
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
        slotUI.setTooltipText(item.getDisplayName());
    }

    /**
     * Updates the slots.
     */
    private void updateSlots() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        for (InventorySlotUI slotUI : slotUIs) {
            if (slotUI != null) {
                slotUI.setHovered(slotUI.contains(mouseX, mouseY));
            }
        }

        if (backpackUI != null && backpackUI.isVisible()) {
            for (InventorySlotUI slotUI : backpackUI.getSlotUIs()) {
                if (slotUI != null) {
                    slotUI.setHovered(slotUI.contains(mouseX, mouseY));
                }
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

    /**
     * Performs the slot interact operation.
     */
    public void slotInteract() {
        if (hotbarUI == null) return;

        InventorySlotUI[] hotbarSlots = hotbarUI.getSlotUIs();
        InventorySlotUI[] backpackSlots = (backpackUI != null && backpackUI.isVisible()) ?
                backpackUI.getSlotUIs() : new InventorySlotUI[0];

        InventorySlotUI[] allSlots = new InventorySlotUI[slotUIs.length + hotbarSlots.length + backpackSlots.length];
        System.arraycopy(slotUIs, 0, allSlots, 0, slotUIs.length);
        System.arraycopy(hotbarSlots, 0, allSlots, slotUIs.length, hotbarSlots.length);
        if (backpackSlots.length > 0) {
            System.arraycopy(backpackSlots, 0, allSlots, slotUIs.length + hotbarSlots.length, backpackSlots.length);
        }

        for (InventorySlotUI slotUI : allSlots) {
            if (slotUI == null || !slotUI.isHovered()) continue;
            InventorySlot slot = slotUI.getSlotType();
            if (slot == null) continue;

            if (Mouse.isButtonPressed(Mouse.BUTTON_LEFT)) {
                leftClick(slot);
                break;
            }

            if (Mouse.isButtonPressed(Mouse.BUTTON_RIGHT)) {
                rightClick(slot);
                break;
            }
        }
    }

    /**
     * Performs the left click operation.
     * @param slot the slot value
     */
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

    /**
     * Performs the pick entire stack operation.
     * @param slot the slot value
     */
    private void pickEntireStack(InventorySlot slot) {
        if (slot.isEmpty()) {
            return;
        }

        carriedItem = slot.getItem();
        carriedAmount = slot.getAmount();

        slot.clear();
    }

    /**
     * Performs the place entire stack operation.
     * @param slot the slot value
     */
    private void placeEntireStack(InventorySlot slot) {
        slot.setItem(carriedItem);
        slot.setAmount(carriedAmount);
        clearCarriedItem();
    }

    /**
     * Performs the merge carried stack operation.
     * @param slot the slot value
     */
    private void mergeCarriedStack(InventorySlot slot) {
        int maxStack = K.World.MAX_STACK;
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

    /**
     * Performs the swap stacks operation.
     * @param slot the slot value
     */
    private void swapStacks(InventorySlot slot) {
        Item tempItem = slot.getItem();
        int tempAmount = slot.getAmount();

        slot.setItem(carriedItem);
        slot.setAmount(carriedAmount);

        carriedItem = tempItem;
        carriedAmount = tempAmount;
    }

    /**
     * Performs the right click operation.
     * @param slot the slot value
     */
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

    /**
     * Performs the take half operation.
     * @param slot the slot value
     */
    private void takeHalf(InventorySlot slot) {
        if (slot.isEmpty()) {
            return;
        }

        int splitAmount = (int) Math.ceil(slot.getAmount() / 2.0);

        carriedItem = slot.getItem();
        carriedAmount = splitAmount;

        slot.setAmount(slot.getAmount() - splitAmount);
    }

    /**
     * Performs the place one operation.
     * @param slot the slot value
     */
    private void placeOne(InventorySlot slot) {
        int maxStack = K.World.MAX_STACK;
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

    /**
     * Adds the one to slot.
     * @param slot the slot value
     */
    private void addOneToSlot(InventorySlot slot) {
        int maxStack = K.World.MAX_STACK;

        if (slot.getAmount() >= maxStack) {
            return;
        }

        slot.addAmount(1);
        carriedAmount--;

        if (carriedAmount <= 0) {
            clearCarriedItem();
        }
    }

    /**
     * Checks whether the same type condition is met.
     * @param a the a value
     * @param b the b value
     * @return {@code true} if same type; otherwise {@code false}
     */
    private boolean isSameType(Item a, Item b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.getClass() != b.getClass()) {
            return false;
        }

        return switch (a) {
            case Produce p1 when b instanceof Produce p2 -> p1.getType() == p2.getType();
            case Seed s1 when b instanceof Seed s2 -> s1.getType() == s2.getType();
            case Crop c1 when b instanceof Crop c2 -> c1.getCropType() == c2.getCropType();
            case Block b1 when b instanceof Block b2 -> b1.getType() == b2.getType();
            case Tool t1 when b instanceof Tool t2 -> t1.getId() == t2.getId() && t1.getType() == t2.getType();
            default -> a.getName().equals(b.getName());
        };
    }

    /**
     * Renders render.
     */
    @Override
    public void render() {
        renderChildren();
        renderCarriedItem();

        if (inventory != null && inventory.getBackpackSlot() != null
                && inventory.getBackpackSlot().getItem() != null) {
            backpackButton.show();
        } else {
            backpackButton.hide();
        }
    }

    /**
     * Renders the carried item.
     */
    private void renderCarriedItem() {
        if (carriedItem == null || carriedAmount <= 0) {
            return;
        }

        SpriteSheet sheet = ResourceManager.getItemSpriteSheet(carriedItem);
        if (sheet == null) {
            return;
        }

        float iconSize = Settings.getScaledIcon();
        float x = Mouse.getX() - iconSize / 2f;
        float y = Mouse.getY() - iconSize / 2f;
        int frame = ResourceManager.getItemFrame(carriedItem);

        GUI.drawSprite(sheet, frame, x, y,
                iconSize, iconSize, K.UI.UI_ITEM_TINT);

        if (carriedAmount > 1) {
            String amount = String.valueOf(carriedAmount);

            GUI.drawString(amount, x + iconSize - 10, y + iconSize - 10,
                    GUI.getNormalFont(), K.UI.UI_TEXT_COLOR);
        }
    }

    /**
     * Sets the icons.
     * @param seed the seed value
     * @param crop the crop value
     * @param block the block value
     * @param tool the tool value
     * @param material the material value
     * @param inv the inv value
     */
    public void setIcons(SpriteSheet seed, SpriteSheet crop,
                         SpriteSheet block, SpriteSheet tool,
                         SpriteSheet material, SpriteSheet inv) {
        this.seedIcons = seed;
        this.cropIcons = crop;
        this.blockIcons = block;
        this.toolIcons = tool;
        this.materialIcons = material;
        this.inventoryIcons = inv;

        if (hotbarUI != null) {
            hotbarUI.setSeedIcons(seedIcons);
            hotbarUI.setCropIcons(cropIcons);
            hotbarUI.setBlockIcons(blockIcons);
            hotbarUI.setToolIcons(toolIcons);
            hotbarUI.setMaterialIcons(materialIcons);
            hotbarUI.setInventoryIcons(inventoryIcons);
        }
    }

    /**
     * Returns the inventory.
     * @return the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Sets the inventory.
     * @param inventory the inventory value
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Sets the hotbar ui.
     * @param gameMaster the game master value
     * @param hotbarUI the hotbar ui value
     */
    public void setHotbarUI(GameMaster gameMaster, HotbarUI hotbarUI) {
        this.gameMaster = gameMaster;
        this.hotbarUI = hotbarUI;
    }

    /**
     * Sets the game master.
     * @param gameMaster the game master value
     */
    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    /**
     * Returns the backpack ui.
     * @return the backpack ui
     */
    public BackpackInventoryUI getBackpackUI() {
        return backpackUI;
    }

    /**
     * Sets the backpack ui.
     * @param backpackUI the backpack ui value
     */
    public void setBackpackUI(BackpackInventoryUI backpackUI) {
        this.backpackUI = backpackUI;
    }

    /**
     * Enumerates the supported tab values.
     */
    public enum Tab {
        INVENTORY, CRAFTING
    }
}
