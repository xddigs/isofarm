package com.isofarm.gui;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.input.Mouse;
import com.isofarm.item.*;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

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
    private UIButton craftingButton;
    private Player player;
    private Inventory inventory;
    private Tab currentTab;
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
    private GameMaster gameMaster;

    private float defaultX;
    private float targetX;
    private float defaultY;
    private float targetY;
    private boolean isClosing = false;

    private boolean isBackpackOpen = false;
    private boolean isBackpackClosing = false;
    private float backpackTargetY;
    private float backpackCurrentY;

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

    private void createButtons() {
        float btnWidth = Settings.getScaledSlot(), btnHeight = Settings.getScaledSlot();
        sortButton = new UIButton(Settings.getScaledPadding(),
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        groupButton = new UIButton(Settings.getScaledPadding() + btnWidth + Settings.getScaledSpacing(),
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        backpackButton = new UIButton(Settings.getScaledPadding() + btnWidth * 2 + Settings.getScaledSpacing() * 2,
                Settings.getScaledPadding() - Settings.getScaledSpacing(), btnWidth, btnHeight);

        craftingButton = new UIButton(Settings.getScaledPadding() + btnWidth * 3 + Settings.getScaledSpacing() * 3,
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

        craftingButton.setOnClick(() -> {
            if (currentTab.equals(Tab.INVENTORY)) {
                currentTab = Tab.CRAFTING;
            } else {
                currentTab = Tab.INVENTORY;
            }
        });

        sortButton.setTooltipText("Sort");
        groupButton.setTooltipText("Group");
        backpackButton.setTooltipText("Backpack");
        backpackButton.hide();

        buttons.add(sortButton);
        buttons.add(groupButton);
        buttons.add(backpackButton);
        buttons.add(craftingButton);

        addChild(sortButton);
        addChild(groupButton);
        addChild(backpackButton);
        addChild(craftingButton);
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

    public void closeBackpack() {
        if (!isBackpackOpen || isBackpackClosing) return;
        this.isBackpackClosing = true;
        this.targetY = this.defaultY;
        if (backpackUI != null) {
            this.backpackTargetY = this.defaultY - backpackUI.getHeight() - Settings.getScaledSpacing() * 2.0f;
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
            onClose();
        }

        craftingButton.setTooltipText("Crafting Kit: " +
                (currentTab.equals(Tab.CRAFTING) ? "ON" : "OFF"));

        syncInventory();
        updateSlots();
        slotInteract();
    }

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

    @Override
    public UIElement hide() {
        super.hide();
        this.isClosing = false;
        return this;
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

        if (backpackUI != null && backpackUI.getSlotUIs() != null) {
            Inventory backpackInv = (player != null) ? player.getBackpack() : null;
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

            craftingButton.setSpriteSheet(inventoryIcons);
            craftingButton.setSpriteColumn(3);
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
        switch (currentTab) {
            case INVENTORY -> {
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

            case CRAFTING -> {
                if (carriedItem == null) {
                    pickEntireStack(slot);
                    return;
                }

                if (slot.isEmpty()) {
                    placeEntireStack(slot);
                    return;
                }

                attemptCraft(slot);
            }
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

    private void swapStacks(InventorySlot slot) {
        Item tempItem = slot.getItem();
        int tempAmount = slot.getAmount();

        slot.setItem(carriedItem);
        slot.setAmount(carriedAmount);

        carriedItem = tempItem;
        carriedAmount = tempAmount;
    }

    private void rightClick(InventorySlot slot) {
        switch (currentTab) {
            case INVENTORY -> {
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

            case CRAFTING -> {
                if (attemptCraftOne(slot)) {
                    return;
                }

                if (carriedItem == null) {
                    pickEntireStack(slot);
                    return;
                }

                if (slot.isEmpty()) {
                    placeEntireStack(slot);
                    return;
                }

                attemptCraft(slot);
            }
        }
    }

    private boolean attemptCraftOne(InventorySlot slot) {
        if (slot == null || slot.isEmpty()) {
            return false;
        }

        Item item = slot.getItem();
        Recipe recipe = findSingleIngredientRecipe(item);
        if (recipe == null) {
            return false;
        }

        Ingredient ingredient = recipe.ingredients().getFirst();
        if (slot.getAmount() < ingredient.amount()) {
            ToastFactory.info("Not enough materials");
            return true;
        }

        craftSingleIngredient(recipe, slot);
        return true;
    }

    private Recipe findSingleIngredientRecipe(Item item) {
        if (gameMaster == null || item == null) {
            return null;
        }

        for (Recipe recipe : gameMaster.getRecipes()) {
            if (recipe.ingredients().size() != 1) {
                continue;
            }

            Ingredient ingredient = recipe.ingredients().getFirst();
            if (matchesIngredient(ingredient, item)) {
                return recipe;
            }
        }

        return null;
    }

    private void craftSingleIngredient(Recipe recipe, InventorySlot slot) {
        Ingredient ingredient = recipe.ingredients().getFirst();
        int cost = ingredient.amount();
        if (slot.getAmount() < cost) {
            return;
        }

        slot.setAmount(slot.getAmount() - cost);
        if (slot.getAmount() <= 0) {
            slot.clear();
        }

        Item result = recipe.result().copy();
        int remaining = inventory.add(result, recipe.resultAmount());
        if (!player.hasSpace()) {
            player.addToBackpack(result, remaining);
        }
    }

    private void attemptCraft(InventorySlot targetSlot) {
        if (carriedItem == null || targetSlot == null || targetSlot.isEmpty()) {
            return;
        }

        Item targetItem = targetSlot.getItem();
        Recipe recipe = findRecipe(carriedItem, carriedAmount, targetItem, targetSlot.getAmount());
        if (recipe == null) {
            ToastFactory.error("These materials cannot be combined");
            return;
        }

        craft(recipe, targetSlot);
    }

    private Recipe findRecipe(Item first, int firstAmount,
                              Item second, int secondAmount) {
        if (gameMaster == null) {
            return null;
        }

        for (Recipe recipe : gameMaster.getRecipes()) {
            if (!recipeMatches(recipe, first, second)) {
                continue;
            }

            if (canCraft(recipe, first, firstAmount, second, secondAmount)) {
                return recipe;
            }
        }

        return null;
    }

    private boolean recipeMatches(Recipe recipe, Item first, Item second) {
        List<Ingredient> ingredients = recipe.ingredients();
        if (ingredients.size() == 1) {
            Ingredient single = ingredients.get(0);
            return matchesIngredient(single, first) && isSameType(first, second);
        }

        if (ingredients.size() == 2) {
            Ingredient a = ingredients.get(0);
            Ingredient b = ingredients.get(1);

            return (matchesIngredient(a, first) && matchesIngredient(b, second)) ||
                    (matchesIngredient(a, second) && matchesIngredient(b, first));
        }

        return false;
    }

    private boolean matchesIngredient(Ingredient ingredient, Item item) {
        if (ingredient == null || item == null) {
            return false;
        }

        Craftable craftable = ingredient.craftable();
        if (craftable instanceof BlockData bd
                && item instanceof Block b) {
            return b.getType() == bd;
        }

        if (craftable instanceof MaterialID mid
                && item instanceof Material m) {
            return m.getId() == mid.getId();
        }

        if (craftable instanceof MiningComponent mc
                && item instanceof MiningComponent itemMc) {
            return mc.getTier() == itemMc.getTier()
                    && mc.getId() == itemMc.getId();
        }

        if (craftable instanceof Item craftableItem) {
            return isSameType(craftableItem, item);
        }

        return false;
    }

    private boolean canCraft(Recipe recipe, Item first, int firstAmount,
                             Item second, int secondAmount) {
        List<Ingredient> ingredients = recipe.ingredients();

        if (ingredients.size() == 1) {
            Ingredient single = ingredients.get(0);
            if (matchesIngredient(single, first)) {
                return (firstAmount + secondAmount) >= single.amount();
            }
        }

        if (ingredients.size() == 2) {
            Ingredient ing0 = ingredients.get(0);
            Ingredient ing1 = ingredients.get(1);

            if (matchesIngredient(ing0, first) && matchesIngredient(ing1, second)) {
                return firstAmount >= ing0.amount() && secondAmount >= ing1.amount();
            }

            if (matchesIngredient(ing1, first) && matchesIngredient(ing0, second)) {
                return firstAmount >= ing1.amount() && secondAmount >= ing0.amount();
            }
        }

        return false;
    }

    private void craft(Recipe recipe, InventorySlot targetSlot) {
        List<Ingredient> ingredients = recipe.ingredients();

        if (ingredients.size() == 1) {
            Ingredient ingredient = ingredients.get(0);
            int cost = ingredient.amount();

            int takenFromCarried = Math.min(carriedAmount, cost);
            carriedAmount -= takenFromCarried;
            int remainingCost = cost - takenFromCarried;

            if (carriedAmount <= 0) {
                clearCarriedItem();
            }

            if (remainingCost > 0) {
                targetSlot.setAmount(targetSlot.getAmount() - remainingCost);
                if (targetSlot.getAmount() <= 0) {
                    targetSlot.clear();
                }
            }
        } else if (ingredients.size() == 2) {
            Ingredient ing0 = ingredients.get(0);
            Ingredient ing1 = ingredients.get(1);

            Item firstItem = carriedItem;
            Item secondItem = targetSlot.getItem();

            int carriedCost;
            int targetCost;

            if (matchesIngredient(ing0, firstItem) && matchesIngredient(ing1, secondItem)) {
                carriedCost = ing0.amount();
                targetCost = ing1.amount();
            } else {
                carriedCost = ing1.amount();
                targetCost = ing0.amount();
            }

            carriedAmount -= carriedCost;
            if (carriedAmount <= 0) {
                clearCarriedItem();
            }

            targetSlot.setAmount(targetSlot.getAmount() - targetCost);
            if (targetSlot.getAmount() <= 0) {
                targetSlot.clear();
            }
        }

        Item result = recipe.result().copy();
        int resultAmount = recipe.resultAmount();

        int remaining = inventory.add(result, resultAmount);
        if (remaining > 0 && player != null) {
            player.addToBackpack(result, remaining);
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

        renderCharacter();
    }

    private void renderCharacter() {
        if (player == null) {
            return;
        }

        SpriteSheet sheet = ResourceManager.getPlayerSpriteSheet();
        if (sheet == null) return;

        float scale = Settings.getScaledEntity() * 3.0f;
        float width = sheet.getFrameWidth() * scale;
        float height = sheet.getFrameHeight() * scale;

        float slotSize = Settings.getScaledSlot();
        float spacing = Settings.getScaledSpacing();

        float slotsTotalHeight = K.UI.INVENTORY_ROWS * slotSize + (K.UI.INVENTORY_ROWS - 1) * spacing;
        float bottomY = getAbsoluteY() + Settings.getScaledPadding() + slotsTotalHeight;
        float backgroundWidth = width + 20.0f;
        float backgroundHeight = height + 20.0f;

        float bgX = getAbsoluteX() - K.UI.INVENTORY_CHARACTER_OFFSET;
        float bgY = bottomY - backgroundHeight;

        String name = player.getName();
        if (name != null && !name.isEmpty()) {
            float nameWidth = GUI.getStringWidth(name, GUI.getNormalFont());
            float nameX = bgX + (backgroundWidth - nameWidth) / 2.0f;
            float fontHeight = GUI.getNormalFont().getSize();
            float nameY = bgY - fontHeight;
            GUI.drawString(name, nameX, nameY, GUI.getNormalFont(), new Vector4f(1.0f));
        }

        GUI.drawRect(bgX, bgY, backgroundWidth, backgroundHeight, K.UI.UI_BACKGROUND_COLOR_SLOT);
        float spriteX = bgX + (backgroundWidth - width) / 2.0f;
        float spriteY = bgY + (backgroundHeight - height) / 2.0f;

        int frontDirectionOffset = 2;
        int idleFrameCount = 4;
        int idleFrame = (int) ((System.currentTimeMillis() / 200) % idleFrameCount);

        int baseSpriteIndex = (frontDirectionOffset * K.UI.PLAYER_SPRITE_COLS) + idleFrame;
        GUI.drawSprite(sheet, baseSpriteIndex, spriteX, spriteY, width, height, new Vector4f(1.0f));

        if (player.getInventory() != null && player.getInventory().hasBackpackEquipped()) {
            int backpackRowOffset = 8;
            int backpackSpriteIndex = baseSpriteIndex + (backpackRowOffset * K.UI.PLAYER_SPRITE_COLS);
            GUI.drawSprite(sheet, backpackSpriteIndex, spriteX, spriteY, width, height, new Vector4f(1.0f));
        }
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
        int frame = ResourceManager.getItemFrame(carriedItem);

        GUI.drawSprite(sheet, frame, x, y,
                iconSize, iconSize, K.UI.UI_ITEM_TINT);

        if (carriedAmount > 1) {
            String amount = String.valueOf(carriedAmount);

            GUI.drawString(amount, x + iconSize - 10, y + iconSize - 10,
                    GUI.getNormalFont(), K.UI.UI_TEXT_COLOR);
        }
    }

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

    public enum Tab {
        INVENTORY, CRAFTING
    }
}