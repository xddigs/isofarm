package com.tilled.service;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import com.tilled.gui.GUI;
import com.tilled.gui.UIManager;
import com.tilled.input.Mouse;
import com.tilled.utils.K;
import com.tilled.wrld.GameMaster;
import imgui.ImGui;
import imgui.ImGuiStyle;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("all")
public class GameUIService implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(GameUIService.class);
    private final GameMaster gameMaster;
    private final UIManager uiManager;

    private final SpriteSheet seedIcons;
    private final SpriteSheet cropIcons;
    private final SpriteSheet blockIcons;
    private final SpriteSheet toolIcons;
    private Player player;
    private Shop shop;
    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;

    private Vector2i lastActionCell = null;
    private float actionDisplayTimer = 0.0f;

    private Item selectedInventoryItem = null;
    private int selectedHotbarSlot = -1;
    private int selectedInventorySlot = -1;

    private float hotbarLabelTimer = 0.0f;
    private String hotbarLabel = null;

    public GameUIService(
            long windowHandle,
            GameMaster gameMaster, UIManager uiManager,
            SpriteSheet seedIcons,
            SpriteSheet cropIcons,
            SpriteSheet blockIcons,
            SpriteSheet toolIcons
    ) {
        this.gameMaster = gameMaster;
        this.uiManager = uiManager;
        this.seedIcons = seedIcons;
        this.cropIcons = cropIcons;
        this.blockIcons = blockIcons;
        this.toolIcons = toolIcons;

        // add children
//        uiManager.getRoot().addChild(name);
    }

    private static ImGuiStyle getImGuiStyle() {
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(K.Style.WINDOW_ROUNDING);
        style.setFrameRounding(K.Style.FRAME_ROUNDING);
        style.setWindowBorderSize(K.Style.WINDOW_BORDER_SIZE);
        style.setFrameBorderSize(K.Style.FRAME_BORDER_SIZE);
        style.setWindowPadding(
                K.Style.WINDOW_PADDING_X, K.Style.WINDOW_PADDING_Y);
        style.setFramePadding(
                K.Style.FRAME_PADDING_X, K.Style.FRAME_PADDING_Y);
        style.setItemSpacing(
                K.Style.ITEM_SPACING_X, K.Style.ITEM_SPACING_Y);
        return style;
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

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public void update(float delta) {
        if (actionDisplayTimer > 0.0f) {
            actionDisplayTimer -= delta;
        }

        if (hotbarLabelTimer > 0.0f) {
            hotbarLabelTimer -= delta;

            if (hotbarLabelTimer <= 0.0f) {
                hotbarLabelTimer = 0.0f;
                hotbarLabel = null;
            }
        }

        uiManager.update(delta);
        gameMaster.getToastService().update(delta);
        float scroll = Mouse.getScrollY();
        if (scroll != 0) {
            selectItem(scroll > 0 ? -1 : 1);
        }
    }

    public void render() {
        glDisable(GL_DEPTH_TEST);
        GUI.begin(windowWidth, windowHeight);
        uiManager.render();
        GUI.end();
        glEnable(GL_DEPTH_TEST);
    }

    public void renderToasts() {
        if (gameMaster.getToastService().isEmpty()) {
            return;
        }

        for (Toast toast : gameMaster.getToastService().getToasts()) {
            renderToast(toast);
        }
    }

    public void renderCrosshair(float windowWidth, float windowHeight) {
        float centerX = windowWidth / 2.0f;
        float centerY = windowHeight / 2.0f;
        float size = K.UI.CROSSHAIR_SIZE;
        float thickness = K.UI.CROSSHAIR_THICKNESS;
    }

    public void selectItem(int direction) {
        if (player == null) return;

        int slots = K.UI.HOTBAR_SLOTS;

        if (selectedHotbarSlot < 0) {
            selectedHotbarSlot = direction > 0 ? 0 : slots - 1;
        } else {
            selectedHotbarSlot = (selectedHotbarSlot + direction) % slots;
            if (selectedHotbarSlot < 0) {
                selectedHotbarSlot = slots - 1;
            }
        }

        List<Item> hotbarItems = player.getInventory().getHotbarItems();
        if (selectedHotbarSlot < hotbarItems.size()) {
            selectedInventoryItem = hotbarItems.get(selectedHotbarSlot);
            showHotbarLabel(selectedInventoryItem);
        } else {
            selectedInventoryItem = null;
            hotbarLabel = null;
            hotbarLabelTimer = 0.0f;
        }
    }

    private void showHotbarLabel(Item item) {
        if (item == null) {
            hotbarLabel = null;
            hotbarLabelTimer = 0.0f;
            return;
        }

        hotbarLabel = item.getName();
        hotbarLabelTimer = K.UI.HOTBAR_LABEL_DURATION;

        // TODO showHotbarLabel with GUI API
    }

    private void renderToast(Toast toast) {
        float x = toast.getX();
        float y = toast.getY();

        float width = K.UI.TOAST_WIDTH;
        float[] background = getToastBackground(toast.getType());
        float[] accent = getToastAccent(toast.getType());

        // TODO renderToast with GUI API
    }

    private float[] getToastAccent(ToastData type) {
        return switch (type) {
            case SUCCESS -> K.Style.COLOR_TOAST_SUCCESS;
            case INFO -> K.Style.COLOR_TOAST_INFO;
            case WARNING -> K.Style.COLOR_TOAST_WARNING;
            case ERROR -> K.Style.COLOR_TOAST_ERROR;
            case REWARD -> K.Style.COLOR_TOAST_REWARD;
            case PURCHASE -> K.Style.COLOR_TOAST_SUCCESS;
            case SELL -> K.Style.COLOR_TOAST_REWARD;
        };
    }

    private float[] getToastBackground(ToastData type) {
        return switch (type) {
            case SUCCESS -> K.Style.COLOR_TOAST_SUCCESS_BG;
            case INFO -> K.Style.COLOR_TOAST_INFO_BG;
            case WARNING -> K.Style.COLOR_TOAST_WARNING_BG;
            case ERROR -> K.Style.COLOR_TOAST_ERROR_BG;
            case REWARD -> K.Style.COLOR_TOAST_REWARD_BG;
            case PURCHASE -> K.Style.COLOR_TOAST_SUCCESS_BG;
            case SELL -> K.Style.COLOR_TOAST_REWARD_BG;
        };
    }

    private String getToastPrefix(ToastData type) {
        return switch (type) {
            case PURCHASE, SUCCESS -> "+";
            case SELL -> "**";
            case INFO -> "i";
            case WARNING -> "!";
            case ERROR -> "X";
            case REWARD -> "*";
        };
    }

    public String getEnteredPlayerName() {
        return "";
    }

    public Item getSelectedInventoryItem() {
        return selectedInventoryItem;
    }

    public void logAction(Hit cell) {
        this.lastActionCell = new Vector2i(cell.x(), cell.y());
        this.actionDisplayTimer = K.UI.COORD_DISPLAY_DURATION;
    }

    public void onResize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        gameMaster.getToastService().setWindowWidth(width);
    }

    public String inputCommand() {
        // TODO implement command input with GUI API
        return "";
    }

    public void renderInv() {
        if (player == null) return;

    }

    private void sellItem(Inventory inv, Item item) {
        Item targetItem = null;
        int cumulativeAmount = 0;

        for (Map.Entry<Item, Integer> entry :
                new HashMap<>(inv.getItems()).entrySet()) {
            if (entry.getKey().getName().equals(item.getName())) {
                targetItem = entry.getKey();
                cumulativeAmount += entry.getValue();
                player.sell(entry.getKey(), entry.getValue());
            }
        }

        if (targetItem != null && cumulativeAmount > 0) {
            shop.buy(targetItem, cumulativeAmount);
        }
    }

    private void buyItem(Inventory stock, Item item, int amount) {
        if (amount <= 0) return;

        int totalPrice = item.getValue() * amount;
        if (player.purse() < totalPrice) {
            log.warn("Player doesn't have enough money to buy {} x{}",
                    item.getName(), amount);
            gameMaster.getToastService().warning(
                    "You don't have enough money to buy " + amount + " "
                            + item.getName() + "!");
            return;
        }

        player.spend(totalPrice);
        shop.earn(totalPrice);

        stock.remove(item, amount);
        player.getInventory().add(item, amount);

        log.info("Player bought {} x{} from shop", item.getName(), amount);
        gameMaster.getToastService().success(
                "You bought " + amount + " " + item.getName() + "!");
    }

    private SpriteSheet getItemSpritesheet(Item item) {
        if (item instanceof Crop) return cropIcons;
        if (item instanceof Seed) return seedIcons;
        if (item instanceof Block) return blockIcons;
        if (item instanceof Tool) return toolIcons;
        return seedIcons;
    }
}