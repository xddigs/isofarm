package com.isofarm.gui;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.input.Mouse;
import com.isofarm.item.Block;
import com.isofarm.item.CraftingKit;
import com.isofarm.item.Item;
import com.isofarm.item.Tool;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

@SuppressWarnings("all")
public class InventoryUI extends UIElement {
    private static final int BACKPACK_SLOTS = 16;
    private static final int BACKPACK_COLUMNS = 4;
    private static final int BACKPACK_ROWS = 4;

    private final InventorySlotUI[] slotUIs;
    private final InventorySlotUI[] backpackSlotUIs;

    private final List<UIButton> buttons;
    private UIButton sortButton;
    private UIButton groupButton;
    private UIButton backpackButton;

    private Player player;
    private Inventory inventory;

    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private SpriteSheet blockIcons;
    private SpriteSheet toolIcons;
    private SpriteSheet materialIcons;
    private SpriteSheet inventoryIcons;

    private Item carriedItem;
    private int carriedAmount;

    private HotbarUI hotbarUI;
    private BackpackInventoryUI backpackUI;
    private UIProgressBar healthBar;
    private UIProgressBar staminaBar;
    private GameMaster gameMaster;

    private float defaultX;
    private float targetX;
    private float animationSpeed = 800.0f;
    private boolean isBackpackOpen = false;

    private boolean showingCraftingMenu = false;
    private float craftingMenuX, craftingMenuY;
    private List<Recipe> availableRecipes = new ArrayList<>();
    private CraftingKit activeCraftingKit;

    public InventoryUI(float x, float y) {
        super(x, y, getInventoryWidth(), getInventoryHeight());
        defaultX = x;
        targetX = x;
        int totalVisualSlots = (K.UI.INVENTORY_ROWS - 1) * K.UI.INVENTORY_COLUMNS;
        this.slotUIs = new InventorySlotUI[totalVisualSlots];
        this.backpackSlotUIs = new InventorySlotUI[BACKPACK_SLOTS];
        this.buttons = new ArrayList<>();
        setFocusable(true);
        createButtons();
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

    public static float getBackpackWidth() {
        return Settings.getScaledPadding() * 2.0f +
                BACKPACK_COLUMNS * Settings.getScaledSlot() +
                (BACKPACK_COLUMNS - 1) * Settings.getScaledSpacing();
    }

    public static float getBackpackHeight() {
        return Settings.getScaledPadding() * 2.0f + Settings.getScaledHeader() +
                BACKPACK_ROWS * Settings.getScaledSlot() +
                (BACKPACK_ROWS - 1) * Settings.getScaledSpacing();
    }

    public InventorySlotUI[] getSlotUIs() {
        return slotUIs;
    }

    public InventorySlotUI[] getBackpackSlotUIs() {
        return backpackSlotUIs;
    }

    private void createButtons() {
        float btnWidth = Settings.getScaledSlot(), btnHeight = Settings.getScaledSlot();
        sortButton = new UIButton(Settings.getScaledPadding(),
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        groupButton = new UIButton(Settings.getScaledPadding() + btnWidth + Settings.getScaledSpacing(),
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        backpackButton = new UIButton(Settings.getScaledPadding() + btnWidth * 2 + Settings.getScaledSpacing() * 2,
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        sortButton.setOnClick(this::sortInventory);
        groupButton.setOnClick(this::groupInventory);
        backpackButton.setOnClick(() -> {
            if (isBackpackOpen) {
                closeBackpack();
            } else {
                openBackpack(gameMaster.getGameUIService()
                        .getBackpackInventoryUI());
            }
        });

        sortButton.setTooltipText("Sort");
        groupButton.setTooltipText("Group");
        backpackButton.setTooltipText("Backpack");
        backpackButton.hide();

        buttons.add(sortButton);
        buttons.add(groupButton);
        buttons.add(backpackButton);

        addChild(sortButton);
        addChild(groupButton);
        addChild(backpackButton);
    }

    public void createSlots() {
        for (int i = 0; i < slotUIs.length; i++) {
            int column = i % K.UI.INVENTORY_COLUMNS;
            int row = i / K.UI.INVENTORY_COLUMNS;
            float x = Settings.getScaledPadding() + column * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            float y = Settings.getScaledPadding() + Settings.getScaledHeader() + row * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(), Settings.getScaledSlot(),
                    InventorySlotUI.SlotType.INVENTORY);

            slotUIs[i] = slotUI;
            addChild(slotUI);
        }
    }

    public void createBackpackSlots() {
        for (int i = 0; i < BACKPACK_SLOTS; i++) {
            int column = i % BACKPACK_COLUMNS;
            int row = i / BACKPACK_COLUMNS;
            float x = Settings.getScaledPadding() + column * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            float y = Settings.getScaledPadding() + Settings.getScaledHeader() + row * (Settings.getScaledSlot() + Settings.getScaledSpacing());
            InventorySlotUI slotUI = new InventorySlotUI(x, y, Settings.getScaledSlot(), Settings.getScaledSlot(),
                    InventorySlotUI.SlotType.BACKPACK);

            getBackpackSlotUIs()[i] = slotUI;
            backpackUI.addChild(slotUI);
        }
    }

    public List<UIButton> getButtons() {
        return buttons;
    }

    public void sortInventory() {
        if (player != null && inventory != null) {
            inventory.sort();
        }
    }

    public void groupInventory() {
        if (player != null && inventory != null) {
            inventory.group();
        }
    }

    private void updatePosition(float delta) {
        float currentX = getX();
        if (Math.abs(targetX - currentX) < 0.5f) {
            setPosition(targetX, getY());
        } else {
            float direction = Math.signum(targetX - currentX);
            float movement = animationSpeed * delta;

            float newX = currentX + direction * movement;

            if ((direction > 0 && newX > targetX) ||
                    (direction < 0 && newX < targetX)) {
                newX = targetX;
            }
            setPosition(newX, getY());
        }

        if (backpackUI != null && isBackpackOpen) {
            float spacing = Settings.getScaledSpacing();
            backpackUI.setPosition(getX() + getWidth() + spacing, getY());
        }
    }

    public void openBackpack(BackpackInventoryUI backpackUI) {
        if (backpackUI == null) return;
        backpackUI.show();
        isBackpackOpen = true;
        float spacing = Settings.getScaledSpacing();
        float shift = (backpackUI.getAbsoluteWidth() + spacing) / 3.0f;
        targetX = defaultX - shift;
    }

    public void closeBackpack() {
        isBackpackOpen = false;
        targetX = defaultX;
        if (gameMaster == null || gameMaster.getGameUIService() == null) {
            if (backpackUI != null) {
                backpackUI.hide();
            }
            return;
        }

        BackpackInventoryUI backpackUI =
                gameMaster.getGameUIService().getBackpackInventoryUI();

        if (backpackUI != null) {
            backpackUI.hide();
        }
    }

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
            hide();
            onClose();
        }

        syncInventory();
        updateSlots();
        slotInteract();
    }

    private void onOpen() {
        closeBackpack();
        if (hotbarUI == null || healthBar == null || staminaBar == null) {
            return;
        }

        if (gameMaster != null && !isBackpackOpen) {
            defaultX = (gameMaster.getWindowWidth() - getWidth()) / 2.0f;
            targetX = defaultX;
            setPosition(targetX, getY());
        }

        float hotbarX = getAbsoluteX() + (getWidth() - hotbarUI.getWidth()) / 2.0f;
        float hotbarY = getAbsoluteY() + getHeight() + K.UI.HOTBAR_OFFSET;
        hotbarUI.setPosition(hotbarX, hotbarY);
        hotbarUI.setInventoryMode(true);

        float barWidth = healthBar.getWidth();
        float barHeight = healthBar.getHeight();
        float gapBetweenBars = 12.0f;
        float offsetAboveHotbar = 10.0f;

        float totalBarsWidth = (barWidth * 2.0f) + gapBetweenBars;
        float startX = hotbarX + (hotbarUI.getWidth() - totalBarsWidth) / 2.0f;
        float barY = hotbarY - barHeight - offsetAboveHotbar;
        healthBar.setPosition(startX, barY);
        staminaBar.setPosition(startX + barWidth + gapBetweenBars, barY);
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

        int remaining = inventory.add(carriedItem, carriedAmount);

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

        for (int i = 0; i < backpackSlotUIs.length; i++) {
            InventorySlotUI slotUI = backpackSlotUIs[i];
            if (slotUI == null) continue;

            if (i < (inventory.getSlots().size())) {
                slotUI.setSlot((inventory.getSlot(i)));
            } else {
                slotUI.setSlot(null);
            }

            updateItemSprite(slotUI);
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

    private void updateItemSprite(InventorySlotUI slotUI) {
        Item item = slotUI.getItem();
        if (item == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setSpriteColumn(0);
            slotUI.setSpriteRow(0);
            slotUI.setTooltipText(null);
            return;
        }

        SpriteSheet spriteSheet = ResourceManager.getItemSpriteSheet(item);

        if (spriteSheet == null) {
            slotUI.setSpriteSheet(null);
            slotUI.setSpriteColumn(0);
            slotUI.setSpriteRow(0);
            slotUI.setTooltipText(null);
            return;
        }

        slotUI.setSpriteSheet(spriteSheet);
        slotUI.setSpriteColumn(ResourceManager.getItemIconColumn(item));
        slotUI.setSpriteRow(ResourceManager.getItemIconRow(item));
        slotUI.setTooltipText(item.getName());
    }

    private void updateSlots() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        for (InventorySlotUI slotUI : slotUIs) {
            if (slotUI != null) {
                slotUI.setHovered(slotUI.contains(mouseX, mouseY));
            }
        }

        for (InventorySlotUI slotUI : getBackpackSlotUIs()) {
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

    public void slotInteract() {
        if (hotbarUI == null) return;
        InventorySlotUI[] hotbarSlots = hotbarUI.getSlotUIs();
        InventorySlotUI[] backpackSlots = getBackpackSlotUIs();
        InventorySlotUI[] allSlots = new InventorySlotUI[slotUIs.length + hotbarSlots.length + backpackSlots.length];
        System.arraycopy(slotUIs, 0, allSlots, 0, slotUIs.length);
        System.arraycopy(hotbarSlots, 0, allSlots, slotUIs.length, hotbarSlots.length);
        System.arraycopy(backpackSlots, 0, allSlots, slotUIs.length + hotbarSlots.length, backpackSlots.length);

        for (InventorySlotUI slotUI : allSlots) {
            if (slotUI == null || !slotUI.isHovered()) continue;
            InventorySlot slot = slotUI.getSlotType();
            if (slot == null) continue;

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
        int maxStack = inventory.getMaxStack(carriedItem);
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
        if (slot != null && slot.getItem() instanceof CraftingKit kit) {
            this.availableRecipes = gameMaster.getRecipes().stream()
                    .filter(r -> r.tier() == kit.getTier())
                    .toList();

            if (!availableRecipes.isEmpty()) {
                this.activeCraftingKit = kit;
                this.craftingMenuX = Mouse.getX();
                this.craftingMenuY = Mouse.getY();
                this.showingCraftingMenu = true;
            } else {
                gameMaster.getToastService().info("There's no recipe for this kit yet");
            }
            return;
        }

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

    public void renderRecipes() {
        if (!showingCraftingMenu || availableRecipes.isEmpty()) return;
        final int COLS = availableRecipes.size();
        final float SLOT_SIZE = Settings.getScaledSlot();
        final float SPACING = Settings.getScaledSpacing();

        int total = availableRecipes.size();
        int cols = Math.min(total, COLS);
        int rows = (int) Math.ceil((double) total / cols);

        float width = cols * SLOT_SIZE + (cols + 1) * SPACING;
        float height = rows * SLOT_SIZE + (rows + 1) * SPACING;

        float x = Math.clamp(craftingMenuX, 0, GUI.getScreenWidth() - width);
        float y = Math.clamp(craftingMenuY, 0, GUI.getScreenHeight() - height);

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        boolean isMouseInsideMenu = mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
        if (!isMouseInsideMenu) {
            showingCraftingMenu = false;
            return;
        }

        Player player = gameMaster.getPlayer();

        for (int i = 0; i < total; i++) {
            Recipe recipe = availableRecipes.get(i);
            int col = i % cols;
            int row = i / cols;

            float slotX = x + SPACING + col * (SLOT_SIZE + SPACING);
            float slotY = y + SPACING + row * (SLOT_SIZE + SPACING);

            boolean hovered = mouseX >= slotX && mouseX <= slotX + SLOT_SIZE &&
                    mouseY >= slotY && mouseY <= slotY + SLOT_SIZE;

            boolean canCraft = player.hasIngredients(recipe);
            boolean isSelected = activeCraftingKit != null && recipe.equals(activeCraftingKit.getSelectedRecipe());

            Vector4f slotBg = isSelected ? K.UI.UI_SELECTED_COLOR : K.UI.UI_BACKGROUND_COLOR_SLOT;
            Vector4f slotBorder = isSelected ? K.UI.UI_SELECTED_BORDER_COLOR : K.UI.UI_BORDER_COLOR;

            float borderWidth = isSelected ? 2.0f : 1.0f;
            GUI.drawRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE, slotBg, 4.0f, slotBorder, borderWidth);

            Item resultItem = recipe.result();
            SpriteSheet iconSheet = ResourceManager.getItemSpriteSheet(resultItem);

            if (iconSheet != null) {
                int iconCol = ResourceManager.getItemIconColumn(resultItem);
                int iconRow = ResourceManager.getItemIconRow(resultItem);
                final float iconSize = Settings.getScaledIcon();

                Vector4f tint = canCraft ? new Vector4f(1.0f) : new Vector4f(0.4f, 0.4f, 0.4f, 0.5f);
                GUI.drawSprite(iconSheet, iconCol, iconRow, slotX + 2f,
                        slotY + 2f, iconSize, iconSize, tint);
            }

            if (recipe.resultAmount() > 1) {
                String qty = String.valueOf(recipe.resultAmount());
                GUI.drawSmallString(qty, slotX + SLOT_SIZE - 12, slotY + SLOT_SIZE - 16, new Vector4f(1.0f));
            }

            if (hovered && Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                activeCraftingKit.setSelectedRecipe(recipe);
                showingCraftingMenu = false;
                break;
            }
        }
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
        int maxStack = inventory.getMaxStack(carriedItem);
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
        int maxStack = inventory.getMaxStack(slot.getItem());

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
        if (inventory != null && inventory.getBackpackSlot() != null
                && inventory.getBackpackSlot().getItem() != null) {
            backpackButton.show();
        } else {
            backpackButton.hide();
        }

        renderChildren();
        renderRecipes();
        renderCarriedItem();
    }

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
        int column = ResourceManager.getItemIconColumn(carriedItem);
        int row = ResourceManager.getItemIconRow(carriedItem);

        GUI.drawSprite(sheet, column, row, x, y,
                iconSize, iconSize, K.UI.UI_ITEM_TINT);

        if (carriedAmount > 1) {
            String amount = String.valueOf(carriedAmount);

            GUI.drawString(amount, x + iconSize - 10, y + iconSize - 10,
                    GUI.getNormalFont(), K.UI.UI_TEXT_COLOR);
        }
    }

    public UIProgressBar getHealthBar() {
        return healthBar;
    }

    public void setHealthBar(UIProgressBar healthBar) {
        this.healthBar = healthBar;
    }

    public UIProgressBar getStaminaBar() {
        return staminaBar;
    }

    public void setStaminaBar(UIProgressBar staminaBar) {
        this.staminaBar = staminaBar;
    }

    public void setIcons(SpriteSheet seed, SpriteSheet crop,
                         SpriteSheet block, SpriteSheet tool, SpriteSheet inv) {
        this.seedIcons = seed;
        this.cropIcons = crop;
        this.blockIcons = block;
        this.toolIcons = tool;
        this.inventoryIcons = inv;
    }

    public void setPlayer(Player player) {
        this.player = player;

        if (player != null) {
            this.inventory = player.getInventory();
        } else {
            this.inventory = null;
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
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

    public void setMaterialIcons(SpriteSheet materialIcons) {
        this.materialIcons = materialIcons;

        if (hotbarUI != null) {
            hotbarUI.setMaterialIcons(materialIcons);
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

    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    public BackpackInventoryUI getBackpackUI() {
        return backpackUI;
    }

    public void setBackpackUI(BackpackInventoryUI backpackUI) {
        this.backpackUI = backpackUI;
    }

    public Item getSelectedItem() {
        if (hotbarUI != null) {
            return hotbarUI.getSelectedItem();
        }

        return null;
    }
}