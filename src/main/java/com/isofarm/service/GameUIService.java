package com.isofarm.service;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.gui.*;
import com.isofarm.input.Mouse;
import com.isofarm.item.Item;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("all")
public class GameUIService implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(GameUIService.class);
    private static final float CHAT_HISTORY_DURATION = 3.0f;
    private final GameMaster gameMaster;
    private final UIManager uiManager;
    private final InventoryUI inventoryUI;
    private final RecipeBookUI recipeBookUI;
    private final HotbarUI hotbarUI;
    private final UITextField chatField;
    private final UILabel time;
    private final UILabel coords;
    private final UILabel fps;
    private UIProgressBar healthBar;
    private UIProgressBar staminaBar;
    private final List<String> chatHistory;
    private final SpriteSheet seedIcons;
    private final SpriteSheet cropIcons;
    private final SpriteSheet blockIcons;
    private final SpriteSheet toolIcons;
    private final SpriteSheet materialIcons;
    private Player player;
    private Shop shop;
    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;
    private Vector2i lastActionCell = null;
    private float actionDisplayTimer = 0.0f;
    private float hotbarLabelTimer = 0.0f;
    private String hotbarLabel = null;
    private float chatHistoryTimer = 0.0f;

    public GameUIService(
            long windowHandle,
            GameMaster gameMaster,
            UIManager uiManager,
            SpriteSheet seedIcons,
            SpriteSheet cropIcons,
            SpriteSheet blockIcons,
            SpriteSheet toolIcons,
            SpriteSheet materialIcons,
            SpriteSheet inventoryIcons) {
        this.gameMaster = gameMaster;
        this.uiManager = uiManager;

        this.seedIcons = seedIcons;
        this.cropIcons = cropIcons;
        this.blockIcons = blockIcons;
        this.toolIcons = toolIcons;
        this.materialIcons = materialIcons;

        this.chatHistory = new ArrayList<>();
        this.inventoryUI = new InventoryUI(windowWidth, windowHeight);
        this.inventoryUI.setLayer(100);
        this.inventoryUI.hide();
        this.hotbarUI = new HotbarUI(windowWidth, windowHeight);
        this.hotbarUI.setLayer(50);

        this.inventoryUI.setPosition(
                windowWidth / 2.0f - inventoryUI.getWidth() / 2,
                windowHeight / 2.0f - inventoryUI.getHeight() / 2);

        this.hotbarUI.setPosition(
                windowWidth / 2.0f - hotbarUI.getWidth() / 2,
                windowHeight - hotbarUI.getHeight() - K.UI.HOTBAR_OFFSET);

        inventoryUI.setSeedIcons(seedIcons);
        inventoryUI.setCropIcons(cropIcons);
        inventoryUI.setBlockIcons(blockIcons);
        inventoryUI.setToolIcons(toolIcons);
        inventoryUI.setMaterialIcons(materialIcons);
        inventoryUI.setInventoryIcons(inventoryIcons);
        inventoryUI.setHotbarUI(gameMaster, hotbarUI);

        uiManager.getRoot().addChild(inventoryUI);
        uiManager.getRoot().addChild(hotbarUI);

        this.chatField = new UITextField(10, windowHeight - 40, windowWidth - 20, 30);
        this.chatField.setLayer(1000);
        this.chatField.hide();
        uiManager.getRoot().addChild(chatField);

        this.time = new UILabel(20, 40, 100f, 25f, null);
        this.time.show();
        uiManager.getRoot().addChild(time);

        this.coords = new UILabel(20, time.getAbsoluteY() + time.getAbsoluteHeight(),
                100f, 25f, null);
        this.coords.show();
        uiManager.getRoot().addChild(coords);

        this.fps = new UILabel(20, coords.getAbsoluteY() + coords.getAbsoluteHeight(),
                100f, 25f, null);
        this.fps.show();
        uiManager.getRoot().addChild(fps);

        int barWidth = 160;
        int barHeight = 16;
        int gapBetweenBars = 12;
        int offsetAboveHotbar = 10;

        float totalBarsWidth = (barWidth * 2) + gapBetweenBars;
        float startX = (windowWidth - totalBarsWidth) / 2.0f;
        float barY = windowHeight - hotbarUI.getHeight() - K.UI.HOTBAR_OFFSET - barHeight - offsetAboveHotbar;

        this.healthBar = new UIProgressBar(startX, barY, barWidth, barHeight,
                100, 100, true);

        this.healthBar.setColors(
                new Vector4f(0.8f, 0.1f, 0.1f, 1.0f),
                new Vector4f(0.2f, 0.05f, 0.05f, 0.8f));
        this.healthBar.setLayer(10);
        uiManager.getRoot().addChild(this.healthBar);

        float staminaX = startX + barWidth + gapBetweenBars;
        this.staminaBar = new UIProgressBar(staminaX, barY, barWidth, barHeight,
                100, 100, true);

        this.staminaBar.setColors(
                new Vector4f(0.1f, 0.8f, 0.2f, 1.0f),
                new Vector4f(0.05f, 0.2f, 0.05f, 0.8f));
        this.staminaBar.setLayer(10);
        uiManager.getRoot().addChild(this.staminaBar);

        inventoryUI.setHealthBar(healthBar);
        inventoryUI.setStaminaBar(staminaBar);

        this.recipeBookUI = new RecipeBookUI(
                inventoryUI.getAbsoluteX() + inventoryUI.getAbsoluteWidth()
                        + Settings.getScaledPadding(),
                inventoryUI.getAbsoluteY(),
                Settings.scale(250f),
                inventoryUI.getAbsoluteHeight(),
                null
        );

        this.recipeBookUI.hide();
        uiManager.getRoot().addChild(recipeBookUI);
    }

    public InventoryUI getInventoryUI() {
        return inventoryUI;
    }

    public HotbarUI getHotbarUI() {
        return hotbarUI;
    }

    public void setPlayer(Player player) {
        this.player = player;
        inventoryUI.setPlayer(player);
        hotbarUI.setPlayer(player);
        recipeBookUI.setPlayer(player);
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

        if (chatHistoryTimer > 0.0f) {
            chatHistoryTimer -= delta;

            if (chatHistoryTimer < 0.0f) {
                chatHistoryTimer = 0.0f;
            }
        }

        time.setText(gameMaster.getTimeService().getFormattedTime());
        coords.setText(gameMaster.getPlayer().getPositionString());
        fps.setText(gameMaster.getFps());

        if (Settings.doEnableDebugInfo()) {
            time.show();
            coords.show();
            fps.show();
        } else {
            time.hide();
            coords.hide();
            fps.hide();
        }

        if (player != null) {
            healthBar.setValues(player.getHitpoints(), player.getMaxHitpoints());
            staminaBar.setValues(player.getStamina(), player.getMaxStamina());
        }

        uiManager.update(delta);
        gameMaster.getToastService().update(delta);

        if (!gameMaster.isInventoryOpen()) {
            float scroll = Mouse.getScrollY();
            if (scroll != 0) {
                selectItem(scroll > 0 ? -1 : 1);
                gameMaster.getItemRenderer().playPlaceAnimation();
            }
        }
    }

    public void render(boolean isHUDShown, GameMaster gameMaster) {
        glDisable(GL_DEPTH_TEST);
        GUI.begin(windowWidth, windowHeight);
        if (isHUDShown) {
            uiManager.render();
            renderHotbarLabel();
            renderToasts();
        } else {}

        renderChatHistory();
        if (!gameMaster.isInventoryOpen() && !gameMaster.isOrthographicCamera()) {
            renderCrosshair(windowWidth, windowHeight);
        }

        GUI.end();
        glEnable(GL_DEPTH_TEST);
    }

    public void toggleRecipeBook() {
        if (recipeBookUI.isVisible()) {
            recipeBookUI.hide();
        } else {
            recipeBookUI.show();
            recipeBookUI.refresh();
        }
    }

    private void renderChatHistory() {
        if (chatHistoryTimer <= 0.0f || chatHistory.isEmpty()) {
            return;
        }

        float x = K.UI.CHAT_HISTORY_X;
        float y = chatField.getY() - K.UI.CHAT_HISTORY_OFFSET_Y;

        Vector4f color = K.UI.CHAT_HISTORY_TEXT_COLOR;

        int start = Math.max(
                0,
                chatHistory.size() - K.UI.CHAT_HISTORY_MAX_MESSAGES
        );

        for (int i = chatHistory.size() - 1; i >= start; i--) {
            GUI.drawNormalString(chatHistory.get(i), x, y, color);
            y -= K.UI.CHAT_HISTORY_LINE_HEIGHT;
        }
    }

    public void addChatMessage(String message) {
        if (message == null || message.isBlank()) return;
        chatHistory.add(message);
        if (chatHistory.size() > 50) {
            chatHistory.remove(0);
        }

        chatHistoryTimer = CHAT_HISTORY_DURATION;
    }

    private void renderHotbarLabel() {
        if (hotbarLabel == null || hotbarLabelTimer <= 0.0f) {
            return;
        }

        float alpha = Math.min(1.0f, hotbarLabelTimer * 2.0f);
        Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, alpha);
        float x = windowWidth / 2.0f - GUI.getStringWidth(hotbarLabel, GUI.getNormalFont()) / 2.0f;
        float y = hotbarUI.getY() - K.UI.HOTBAR_LABEL_OFFSET_Y * Settings.getScale();
        GUI.drawNormalString(hotbarLabel, x, y, color);
    }

    public void resetHotbarPosition() {
        float hotbarX = windowWidth / 2.0f - hotbarUI.getWidth() / 2.0f;
        float hotbarY = windowHeight - hotbarUI.getHeight() - K.UI.HOTBAR_OFFSET;
        hotbarUI.setPosition(hotbarX, hotbarY);

        float barWidth = healthBar.getWidth();
        float barHeight = healthBar.getHeight();
        float gapBetweenBars = 12.0f;
        float offsetAboveHotbar = 10.0f;

        float totalBarsWidth = (barWidth * 2.0f) + gapBetweenBars;
        float startX = (windowWidth - totalBarsWidth) / 2.0f;
        float barY = hotbarY - barHeight - offsetAboveHotbar;

        healthBar.setPosition(startX, barY);
        staminaBar.setPosition(startX + barWidth + gapBetweenBars, barY);
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
        float length = Settings.scale(6f);
        float thickness = 2.0f;
        Vector4f color = new Vector4f(1.0f);
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
    }

    private void renderToast(Toast toast) {
        UIFont prefixFont = GUI.getNormalBoldFont();
        UIFont messageFont = GUI.getNormalFont();

        String prefix = getToastPrefix(toast.getType());
        String message = toast.getMessage();

        float paddingLeft = Settings.scale(K.UI.TOAST_MESSAGE_OFFSET_X);
        float paddingRight = Settings.scale(K.UI.TOAST_PADDING_RIGHT);
        float gap = Settings.scale(K.UI.TOAST_GAP_X);
        float accentWidth = Settings.scale(K.UI.TOAST_ACCENT_WIDTH);

        float prefixWidth = GUI.getStringWidth(prefix, prefixFont);
        float messageWidth = GUI.getStringWidth(message, messageFont);

        float x = toast.getX();
        float y = toast.getY();

        float screenWidth = GUI.getScreenWidth();
        float marginFromEdge = Settings.scale(10.0f);
        float maxPossibleWidth = Math.max(100.0f, screenWidth - x - marginFromEdge);

        float baseWidth = Settings.scale(K.UI.TOAST_WIDTH);
        float contentWidth = paddingLeft + prefixWidth + gap + messageWidth + paddingRight;

        float width = Math.min(maxPossibleWidth, Math.max(baseWidth, contentWidth));

        float availableTextWidth = Math.max(Settings.scale(100.0f),
                width - paddingLeft - prefixWidth - gap - paddingRight);

        String[] lines = GUI.wrapText(message, availableTextWidth, messageFont);

        float fontHeight = messageFont.getSize();
        float lineHeight = fontHeight * 1.2f;
        float textBlockHeight = lines.length * lineHeight;

        float minHeight = Settings.scale(36.0f);
        float height = Math.max(minHeight, textBlockHeight + Settings.scale(12.0f));

        float[] bg = getToastBackground(toast.getType());
        float[] acc = getToastAccent(toast.getType());
        Vector4f bgColor = new Vector4f(bg[0], bg[1], bg[2], bg[3]);
        Vector4f accentColor = new Vector4f(acc[0], acc[1], acc[2], acc[3]);

        float cornerRadius = Settings.getScaledCornerRadius();
        GUI.drawRect(x, y, width, height, bgColor, cornerRadius);
        GUI.drawRect(x, y, accentWidth, height, accentColor, cornerRadius);

        float messageX = x + paddingLeft + prefixWidth + gap;
        float startY = y + (height - textBlockHeight) * 0.5f + (fontHeight * 0.7f);
        float prefixFontHeight = prefixFont.getSize();
        float prefixY = y + (height - prefixFontHeight) * 0.5f + (prefixFontHeight * 0.7f);
        GUI.drawString(prefix, x + paddingLeft, prefixY, prefixFont, accentColor, 1.0f);

        for (int i = 0; i < lines.length; i++) {
            GUI.drawString(lines[i], messageX, startY + (i * lineHeight), messageFont, K.UI.UI_TEXT_COLOR, 1.0f);
        }
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

    public String getPlayerName() {
        return "Gabi";
    }

    public void logAction(Hit cell) {
        this.lastActionCell = new Vector2i(cell.x(), cell.y());
        this.actionDisplayTimer = K.UI.COORD_DISPLAY_DURATION;
    }

    public void onResize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        gameMaster.getToastService().setWindowWidth(width);
        gameMaster.getToastService().info("Resized to " + width + "x" + height);
        resetHotbarPosition();

        if (healthBar != null && staminaBar != null) {
            float barWidth = healthBar.getWidth();
            float barHeight = healthBar.getHeight();
            float gapBetweenBars = 12.0f;
            float offsetAboveHotbar = 10.0f;

            float totalBarsWidth = (barWidth * 2.0f) + gapBetweenBars;
            float startX = (width - totalBarsWidth) / 2.0f;
            float barY = height - hotbarUI.getHeight() - K.UI.HOTBAR_OFFSET -
                    barHeight - offsetAboveHotbar;

            healthBar.setPosition(startX, barY);
            staminaBar.setPosition(startX + barWidth + gapBetweenBars, barY);
        }

        if (inventoryUI != null) {
            inventoryUI.setPosition((width - inventoryUI.getWidth()) / 2.0f,
                    (height - inventoryUI.getHeight()) / 2.0f);
        }

        if (chatField != null) {
            chatField.setPosition(10, height - 40);
            chatField.setWidth(width - 20);
        }
    }

    public void openChat() {
        chatField.clear();
        chatField.show();
        uiManager.setFocusedElement(chatField);
    }

    public void closeChat() {
        chatField.hide();
        uiManager.clearFocus();
    }

    public String getChatText() {
        return chatField.getText();
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

    public boolean canCraft(Player player, Recipe recipe) {
        for (RecipeIngredient ing : recipe.ingredients()) {
            int countInInventory = player.getInventory().getAmountOfMaterial(ing.materialID());
            if (countInInventory < ing.amount()) {
                return false;
            }
        }
        return true;
    }

    public void executeCraft(Player player, Recipe recipe) {
        if (!canCraft(player, recipe)) return;
        for (RecipeIngredient ing : recipe.ingredients()) {
            player.getInventory().remove(ing, ing.amount());
        }
        player.add(recipe.result().copy(), recipe.resultAmount());
    }
}