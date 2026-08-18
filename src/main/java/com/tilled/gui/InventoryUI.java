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
    private HotbarUI hotbarUI;
    private GameMaster gameMaster;
    private float hotbarOriginalX, hotbarOriginalY;

    public InventoryUI(float x, float y) {
        super(x, y, getInventoryWidth(), getInventoryHeight());
        this.slotUIs = new InventorySlotUI[K.UI.INVENTORY_SLOTS];
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
        return Settings.getScaledPadding() * 2.0f +
                Settings.getScaledHeader() +
                K.UI.INVENTORY_ROWS * Settings.getScaledSlot() +
                (K.UI.INVENTORY_ROWS - 1) * Settings.getScaledSpacing();
    }

    private void createButtons() {
        float btnWidth = Settings.getScaledButton() * 1.5f;
        float btnHeight = Settings.getScaledHeader() - Settings.getScaledPadding();

        sortButton = new UIButton(Settings.getScaledPadding(), Settings.getScaledPadding(), btnWidth, btnHeight);
        groupButton = new UIButton(Settings.getScaledPadding() + btnWidth + Settings.getScaledSpacing(), Settings.getScaledPadding(), btnWidth, btnHeight);

        sortButton.setOnClick(this::sortInventory);
        groupButton.setOnClick(this::groupInventory);

        addChild(sortButton);
        addChild(groupButton);
    }

    private void createSlots() {
        for (int i = 0; i < K.UI.INVENTORY_SLOTS; i++) {
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
        if (player == null) return;

        boolean wasOpen = isActuallyVisible();
        boolean isOpen = gameMaster != null && gameMaster.isInventoryOpen();

        if (isOpen && !wasOpen) {
            onOpen();
        } else if (!isOpen && wasOpen) {
            onClose();
        }

        syncInventory();
        updateSlots();
        handleSlotInteractions();
    }

    private void onOpen() {
        if (hotbarUI != null) {
            hotbarOriginalX = hotbarUI.getX();
            hotbarOriginalY = hotbarUI.getY();

            if (hotbarUI.getParent() != null) {
                hotbarUI.getParent().removeChild(hotbarUI);
            }
            addChild(hotbarUI);

            float x = getWidth() / 2f - hotbarUI.getWidth() / 2f;
            float y = getHeight() - hotbarUI.getHeight() - Settings.getScaledPadding();
            hotbarUI.setPosition(x, y);
            hotbarUI.setInventoryMode(true);
        }
    }

    private void onClose() {
        if (hotbarUI != null) {
            if (getParent() != null) {
                removeChild(hotbarUI);
                getParent().addChild(hotbarUI);
            }

            hotbarUI.setPosition(hotbarOriginalX, hotbarOriginalY);
            hotbarUI.setInventoryMode(false);
        }

        if (carriedItem != null && player != null) {
            player.getInventory().add(carriedItem, carriedItem.getAmount());
            carriedItem = null;
        }
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
            return;
        }
        slotUI.setSpriteSheet(getItemSpritesheet(item));
        slotUI.setSpriteFrame(getItemIconColumn(item));
    }

    private SpriteSheet getItemSpritesheet(Item item) {
        if (item instanceof Crop) return cropIcons;
        if (item instanceof Seed) return seedIcons;
        if (item instanceof Block) return blockIcons;
        if (item instanceof Tool) return toolIcons;
        return null;
    }

    private static int getItemIconColumn(Item item) {
        if (item instanceof Seed seed && seed.getType() != null) return seed.getType().getId();
        if (item instanceof Crop crop && crop.getCropType() != null) return crop.getCropType().getId();
        if (item instanceof Block block && block.getType() != null) return block.getType().getId() - 1;
        if (item instanceof Tool tool) return tool.getId();
        return 0;
    }

    private void updateSlots() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        for (InventorySlotUI slotUI : slotUIs) {
            slotUI.setHovered(slotUI.contains(mouseX, mouseY));
        }

        if (hotbarUI != null) {
            for (InventorySlotUI slotUI : hotbarUI.getSlotUIs()) {
                slotUI.setHovered(slotUI.contains(mouseX, mouseY));
            }
        }
    }

    private void handleSlotInteractions() {
        InventorySlotUI[] allSlots;
        if (hotbarUI != null) {
            InventorySlotUI[] hotbarSlots = hotbarUI.getSlotUIs();
            allSlots = new InventorySlotUI[slotUIs.length + hotbarSlots.length];
            System.arraycopy(slotUIs, 0, allSlots, 0, slotUIs.length);
            System.arraycopy(hotbarSlots, 0, allSlots, slotUIs.length, hotbarSlots.length);
        } else {
            allSlots = slotUIs;
        }

        for (int i = 0; i < allSlots.length; i++) {
            InventorySlotUI slotUI = allSlots[i];
            if (!slotUI.isHovered()) continue;

            InventorySlot slot = slotUI.getSlot();
            if (slot == null) continue;

            if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                if (carriedItem == null) {
                    if (!slot.isEmpty()) {
                        carriedItem = slot.getItem();
                        slot.clear();
                    }
                } else {
                    if (slot.isEmpty()) {
                        slot.setItem(carriedItem);
                        carriedItem = null;
                    } else {
                        Item target = slot.getItem();
                        if (isSameType(carriedItem, target)) {
                            int space = K.World.MAX_STACK - target.getAmount();
                            int add = Math.min(space, carriedItem.getAmount());
                            target.setAmount(target.getAmount() + add);
                            carriedItem.setAmount(carriedItem.getAmount() - add);

                            if (carriedItem.getAmount() <= 0) {
                                carriedItem = null;
                            }
                        } else {
                            Item temp = slot.getItem();
                            slot.setItem(carriedItem);
                            carriedItem = temp;
                        }
                    }
                }
                break;
            }

            if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                if (carriedItem == null) {
                    if (!slot.isEmpty()) {
                        Item item = slot.getItem();
                        int splitAmount = (int) Math.ceil(item.getAmount() / 2.0);
                        carriedItem = item.copy(splitAmount);
                        item.setAmount(item.getAmount() - splitAmount);
                        if (item.getAmount() <= 0) {
                            slot.clear();
                        }
                    }
                } else {
                    if (slot.isEmpty()) {
                        slot.setItem(carriedItem.copy(1));
                        carriedItem.setAmount(carriedItem.getAmount() - 1);
                    } else if (isSameType(carriedItem, slot.getItem())) {
                        Item target = slot.getItem();
                        if (target.getAmount() < K.World.MAX_STACK) {
                            target.setAmount(target.getAmount() + 1);
                            carriedItem.setAmount(carriedItem.getAmount() - 1);
                        }
                    }

                    if (carriedItem.getAmount() <= 0) {
                        carriedItem = null;
                    }
                }
                break;
            }
        }
    }

    private boolean isSameType(Item a, Item b) {
        if (a == null || b == null) return false;
        if (a.getClass() != b.getClass()) return false;
        if (a instanceof Seed s1 && b instanceof Seed s2) return s1.getType() == s2.getType();
        if (a instanceof Crop c1 && b instanceof Crop c2) return c1.getCropType() == c2.getCropType();
        if (a instanceof Block b1 && b instanceof Block b2) return b1.getType() == b2.getType();
        if (a instanceof Tool t1 && b instanceof Tool t2) return t1.getId() == t2.getId();
        return a.getName().equals(b.getName());
    }

    @Override
    public void render() {
        renderChildren();
        renderCarriedItem();
    }

    private void renderCarriedItem() {
        if (carriedItem == null) return;

        SpriteSheet sheet = getItemSpritesheet(carriedItem);
        if (sheet == null) return;

        float iconSize = Settings.getScaledIcon();
        float x = Mouse.getX() - iconSize / 2f;
        float y = Mouse.getY() - iconSize / 2f;

        GUI.drawSprite(sheet, getItemIconColumn(carriedItem), x, y, iconSize, iconSize, K.UI.UI_ITEM_TINT);

        if (carriedItem.getAmount() > 1) {
            String amt = String.valueOf(carriedItem.getAmount());
            GUI.drawString(amt, x + iconSize - 10, y + iconSize - 10, GUI.getNormalFont(), K.UI.UI_TEXT_COLOR);
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
        if (hotbarUI != null) hotbarUI.setSeedIcons(seedIcons);
    }

    public void setCropIcons(SpriteSheet cropIcons) {
        this.cropIcons = cropIcons;
        if (hotbarUI != null) hotbarUI.setCropIcons(cropIcons);
    }

    public void setBlockIcons(SpriteSheet blockIcons) {
        this.blockIcons = blockIcons;
        if (hotbarUI != null) hotbarUI.setBlockIcons(blockIcons);
    }

    public void setToolIcons(SpriteSheet toolIcons) {
        this.toolIcons = toolIcons;
        if (hotbarUI != null) hotbarUI.setToolIcons(toolIcons);
    }

    public void setInventoryIcons(SpriteSheet inventoryIcons) {
        this.inventoryIcons = inventoryIcons;
        if (hotbarUI != null) hotbarUI.setInventoryIcons(inventoryIcons);
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