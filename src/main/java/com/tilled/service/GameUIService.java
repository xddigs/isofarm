package com.tilled.service;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import com.tilled.gui.GUI;
import com.tilled.gui.HotbarUI;
import com.tilled.gui.InventoryUI;
import com.tilled.gui.UIManager;
import com.tilled.input.Mouse;
import com.tilled.utils.K;
import com.tilled.utils.Settings;
import com.tilled.wrld.GameMaster;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("all")
public class GameUIService implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(GameUIService.class);
    private final GameMaster gameMaster;
    private final UIManager uiManager;
    private final InventoryUI inventoryUI;
    private final HotbarUI hotbarUI;

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

        this.inventoryUI = new InventoryUI(windowWidth, windowHeight);
        this.hotbarUI = new HotbarUI(windowWidth, windowHeight);

        this.inventoryUI.setPosition(
                windowWidth/2.0f - inventoryUI.getWidth()/2,
                windowHeight/2.0f - inventoryUI.getHeight()/2);

        this.hotbarUI.setPosition(
                windowWidth/2.0f - hotbarUI.getWidth()/2,
                windowHeight - hotbarUI.getHeight() - K.UI.HOTBAR_OFFSET);

        inventoryUI.setSeedIcons(seedIcons);
        inventoryUI.setCropIcons(cropIcons);
        inventoryUI.setBlockIcons(blockIcons);
        inventoryUI.setToolIcons(toolIcons);

        hotbarUI.setSeedIcons(seedIcons);
        hotbarUI.setCropIcons(cropIcons);
        hotbarUI.setBlockIcons(blockIcons);
        hotbarUI.setToolIcons(toolIcons);

        uiManager.getRoot().addChild(inventoryUI);
    }

    public InventoryUI getInventoryUI() {
        return inventoryUI;
    }

    public void setPlayer(Player player) {
        this.player = player;
        inventoryUI.setPlayer(player);
        hotbarUI.setPlayer(player);
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

    public void render(boolean isHUDShown, GameMaster gameMaster) {
        glDisable(GL_DEPTH_TEST);
        GUI.begin(windowWidth, windowHeight);
        if (isHUDShown) {
            if (gameMaster.isInventoryOpen()) {
                uiManager.render();
            }
        }

        hotbarUI.render();
        renderCrosshair(windowWidth, windowHeight);
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
        float centerX = Math.round(windowWidth * 0.5f);
        float centerY = Math.round(windowHeight * 0.5f);

        float length = 16.0f * Settings.getScale();
        float thickness = 2.0f;
        Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        GUI.drawLine(centerX - length * 0.5f, centerY,
                centerX + length * 0.5f, centerY, thickness, color);

        GUI.drawLine(centerX, centerY - length * 0.5f,
                centerX, centerY + length * 0.5f, thickness, color);
    }

    public void selectItem(int direction) {
        if (player == null) {
            return;
        }

        if (direction > 0) {
            hotbarUI.selectNext();
        } else if (direction < 0) {
            hotbarUI.selectPrevious();
        }

        Item item = hotbarUI.getSelectedItem();

        if (item != null) {
            showHotbarLabel(item);
        } else {
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

    public void logAction(Hit cell) {
        this.lastActionCell = new Vector2i(cell.x(), cell.y());
        this.actionDisplayTimer = K.UI.COORD_DISPLAY_DURATION;
    }

    public void onResize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        gameMaster.getToastService().setWindowWidth(width);
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
}