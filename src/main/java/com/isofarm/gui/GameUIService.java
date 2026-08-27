package com.isofarm.gui;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.input.CommandCompletionProvider;
import com.isofarm.input.Mouse;
import com.isofarm.item.Item;
import com.isofarm.service.Service;
import com.isofarm.utils.ToastFactory;
import com.isofarm.utils.Components;
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
    private static final float HARDWARE_UPDATE_INTERVAL = 0.5f;
    private static final float DEBUG_LABEL_WIDTH = 500.0f;
    private final GameMaster gameMaster;
    private final UIManager uiManager;
    private final InventoryUI inventoryUI;
    private final BackpackInventoryUI backpackUI;
    private final HotbarUI hotbarUI;
    private final UITextField chatField;
    private final UILabel time;
    private final UILabel coords;
    private final UILabel fps;
    private final UILabel gpu;
    private final UILabel cpu;
    private final UILabel cpuTemp;
    private final UILabel memory;
    private final List<String> chatHistory;
    private final SpriteSheet seedIcons;
    private final SpriteSheet cropIcons;
    private final SpriteSheet blockIcons;
    private final SpriteSheet toolIcons;
    private final SpriteSheet materialIcons;
    private Player player;
    private Shop shop;
    private float windowWidth;
    private float windowHeight;
    private Vector2i lastActionCell = null;
    private float actionDisplayTimer = 0.0f;
    private float hotbarLabelTimer = 0.0f;
    private String hotbarLabel = null;
    private float chatHistoryTimer = 0.0f;
    private float hardwareUpdateTimer = 0.0f;
    private int lastRenderedDamageSequence = -1;
    private float flashTimer = 0.0f;

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

        this.windowWidth = gameMaster.getWindowWidth();
        this.windowHeight = gameMaster.getWindowHeight();

        this.chatHistory = new ArrayList<>();
        this.inventoryUI = new InventoryUI(windowWidth, windowHeight);
        this.inventoryUI.setLayer(100);
        this.inventoryUI.hide();
        this.hotbarUI = new HotbarUI(windowWidth, windowHeight);
        this.hotbarUI.setLayer(50);

        this.inventoryUI.setPosition(
                windowWidth / 2.0f - inventoryUI.getAbsoluteWidth() / 2.0f,
                windowHeight / 2.0f - inventoryUI.getAbsoluteHeight() / 2.0f);

        this.hotbarUI.setPosition(
                windowWidth / 2.0f - hotbarUI.getAbsoluteWidth() / 2.0f,
                windowHeight - hotbarUI.getAbsoluteHeight() - K.UI.HOTBAR_OFFSET);

        this.backpackUI = new BackpackInventoryUI(
                inventoryUI.getAbsoluteX() + inventoryUI.getAbsoluteWidth() -
                        K.UI.INVENTORY_BACKPACK_OFFSET,
                inventoryUI.getAbsoluteY());

        backpackUI.setLayer(100);
        backpackUI.hide();

        inventoryUI.setSeedIcons(seedIcons);
        inventoryUI.setCropIcons(cropIcons);
        inventoryUI.setBlockIcons(blockIcons);
        inventoryUI.setToolIcons(toolIcons);
        inventoryUI.setMaterialIcons(materialIcons);
        inventoryUI.setInventoryIcons(inventoryIcons);
        inventoryUI.setHotbarUI(gameMaster, hotbarUI);
        inventoryUI.setGameMaster(gameMaster);
        backpackUI.setGameMaster(gameMaster);

        uiManager.getRoot().addChild(inventoryUI);
        uiManager.getRoot().addChild(hotbarUI);
        uiManager.getRoot().addChild(backpackUI);

        this.chatField = new UITextField(10, windowHeight - 40, windowWidth - 20, 30);
        this.chatField.setLayer(1000);
        this.chatField.hide();

        chatField.setCompletionProvider(
                new CommandCompletionProvider(gameMaster.getCommandRegistry()));

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

        this.cpu = new UILabel(20, fps.getAbsoluteY() + fps.getAbsoluteHeight(),
                DEBUG_LABEL_WIDTH, 25f, null);
        this.cpu.show();
        uiManager.getRoot().addChild(cpu);

        this.cpuTemp = new UILabel(20, cpu.getAbsoluteY() + cpu.getAbsoluteHeight(),
                DEBUG_LABEL_WIDTH, 25f, null);
        this.cpuTemp.show();
        uiManager.getRoot().addChild(cpuTemp);

        this.gpu = new UILabel(20, cpuTemp.getAbsoluteY() + cpuTemp.getAbsoluteHeight(),
                DEBUG_LABEL_WIDTH, 25f, null);
        this.gpu.show();
        uiManager.getRoot().addChild(gpu);

        this.memory = new UILabel(20, gpu.getAbsoluteY() + gpu.getAbsoluteHeight(),
                DEBUG_LABEL_WIDTH, 25f, null);
        this.memory.show();
        uiManager.getRoot().addChild(memory);

        int barWidth = 160;
        int barHeight = 16;
        int gapBetweenBars = 12;
        int offsetAboveHotbar = 10;

        float totalBarsWidth = (barWidth * 2) + gapBetweenBars;
        float startX = (windowWidth - totalBarsWidth) / 2.0f;
        float barY = windowHeight - hotbarUI.getHeight() - K.UI.HOTBAR_OFFSET - barHeight - offsetAboveHotbar;
    }

    public InventoryUI getInventoryUI() {
        return inventoryUI;
    }

    public HotbarUI getHotbarUI() {
        return hotbarUI;
    }

    public BackpackInventoryUI getBackpackInventoryUI() {
        return backpackUI;
    }

    public void setPlayer(Player player) {
        if (player == null) {
            return;
        }

        this.player = player;
        inventoryUI.setPlayer(player);
        inventoryUI.setInventory(player.getInventory());
        inventoryUI.createSlots();
        hotbarUI.setPlayer(player);
        hotbarUI.setInventory(player.getInventory());
        backpackUI.setPlayer(player);
        backpackUI.setInventory(player.getBackpack());
        backpackUI.createBackpackSlots();
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public void update(float delta) {
        hardwareUpdateTimer -= delta;
        if (hardwareUpdateTimer <= 0.0f) {
            hardwareUpdateTimer = HARDWARE_UPDATE_INTERVAL;
            updateHardwareInfo();
        }

        if (player.getDamageSequence() != lastRenderedDamageSequence) {
            lastRenderedDamageSequence = player.getDamageSequence();
            flashTimer = 0.2f;
        }

        if (flashTimer > 0) {
            flashTimer -= delta;
        }

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
        fps.setText(gameMaster.getFps());
        if (player != null) {
            coords.setText(player.getPositionString());
        }

        if (Settings.doEnableDebugInfo()) {
            time.show();
            coords.show();
            fps.show();
            cpu.show();
            gpu.show();
            cpuTemp.show();
            memory.show();
        } else {
            time.hide();
            coords.hide();
            fps.hide();
            cpu.hide();
            gpu.hide();
            cpuTemp.hide();
            memory.hide();
        }

        uiManager.update(delta);
        ToastFactory.update(delta);

        if (!gameMaster.isInventoryOpen()) {
            float scroll = Mouse.getScrollY();

            if (scroll != 0) {
                selectItem(scroll > 0 ? -1 : 1);
                gameMaster.getItemRenderer().playPlaceAnimation();
            }
        }
    }

    private void updateHardwareInfo() {
        cpu.setText("CPU: " + Components.getCpu());
        gpu.setText("GPU: " + Components.getGpu());
        cpuTemp.setText("CPU Temp: " + Components.getCpuTemperature());
        memory.setText("RAM: " + Components.getPhysicalMemory());
    }

    public void render(boolean isHUDShown, GameMaster gameMaster) {
        glDisable(GL_DEPTH_TEST);
        GUI.begin(windowWidth, windowHeight);
        if (isHUDShown) {
            uiManager.render();
            float startX = hotbarUI.getAbsoluteX() + 10.0f;
            float startY = hotbarUI.getAbsoluteY() - 30.0f;
            renderHearts(gameMaster.getResourceManager().getHeartsSpriteSheet(),
                    startX, startY, player);
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

    public void renderHearts(SpriteSheet heartsSheet, float startX, float startY, Player player) {
        if (player == null || heartsSheet == null) return;

        int currentHp = (int) player.getHitpoints();
        int maxHp = (int) player.getMaxHitpoints();

        int totalHearts = (maxHp + 1) / 2;
        int heartsPerRow = 8;

        float heartSize = Settings.getScaledIcon() / 2f;
        float spacing = Settings.getScaledSpacing() - 2.0f;
        float rowSpacing = spacing;

        heartsSheet.bind();
        for (int i = 0; i < totalHearts; i++) {
            int col = i % heartsPerRow;
            int row = i / heartsPerRow;

            float posX = startX + col * (heartSize + spacing);
            float posY = startY - row * (heartSize + rowSpacing);
            int heartHp = Math.min(2, Math.max(0, currentHp - (i * 2)));
            int frame = (heartHp == 2) ? 0 : (heartHp == 1 ? 1 : 2);

            Vector4f uvBounds = heartsSheet.getUVBounds(frame);
            GUI.drawSprite(heartsSheet, frame, posX, posY, heartSize, heartSize, uvBounds);

            if (flashTimer > 0.0f) {
                int overlayFrame = 3;
                Vector4f overlayUV = heartsSheet.getUVBounds(overlayFrame);
                GUI.drawSprite(heartsSheet, overlayFrame, posX, posY, heartSize, heartSize, overlayUV);
            }
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
        hotbarUI.refreshSize();

        float hotbarX = windowWidth / 2.0f - hotbarUI.getWidth() / 2.0f;
        float hotbarY = windowHeight - hotbarUI.getHeight() - K.UI.HOTBAR_OFFSET;
        hotbarUI.setPosition(hotbarX, hotbarY);
    }

    public void renderToasts() {
        if (ToastFactory.isEmpty()) {
            return;
        }

        for (Toast toast : ToastFactory.getToasts()) {
            renderToast(toast);
        }
    }

    public void renderCrosshair(float windowWidth, float windowHeight) {
        float centerX = Math.round(windowWidth * 0.5f);
        float centerY = Math.round(windowHeight * 0.5f);
        Vector4f color = new Vector4f(1.0f);
        float thickness = 3.0f;

        GUI.drawRect(centerX, centerY, thickness, thickness, color);
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

        float marginFromEdge = Settings.scale(10.0f);
        float maxPossibleWidth = Math.max(100.0f, this.windowWidth - x - marginFromEdge);

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
        ToastFactory.setWindowWidth(width);
        ToastFactory.info("Resized to " + width + "x" + height);
        resetHotbarPosition();

        if (inventoryUI != null) {
            float inventoryX = (width - inventoryUI.getAbsoluteWidth()) / 2.0f;
            float inventoryY = (height - inventoryUI.getAbsoluteHeight()) / 2.0f;
            inventoryUI.setPosition(inventoryX, inventoryY);
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
            ToastFactory.warning(
                    "You don't have enough money to buy " + amount + " "
                            + item.getName() + "!");
            return;
        }

        player.spend(totalPrice);
        shop.earn(totalPrice);

        stock.remove(item, amount);
        player.getInventory().add(item, amount);

        log.info("Player bought {} x{} from shop", item.getName(), amount);
        ToastFactory.success(
                "You bought " + amount + " " + item.getName() + "!");
    }
}