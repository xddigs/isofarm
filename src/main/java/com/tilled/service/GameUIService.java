package com.tilled.service;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import com.tilled.input.Keyboard;
import com.tilled.input.Mouse;
import com.tilled.utils.K;
import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImString;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@SuppressWarnings("all")
public class GameUIService implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(GameUIService.class);
    private static final float HUD_FADE_DELAY = 2.0f;
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final ImString nameBuffer = new ImString("", K.UI.PLAYER_NAME_MAX_LENGTH);
    private final ImString commandBuffer = new ImString("", K.UI.COMMAND_MAX_LENGTH);
    private final CommandService commandService;
    private final ToastService toastService;

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
    private float hudInactivityTimer = 0.0f;

    public GameUIService(long windowHandle, CommandService commandService,
                         ToastService toastService,
                         SpriteSheet seedIcons,
                         SpriteSheet cropIcons,
                         SpriteSheet blockIcons, SpriteSheet toolIcons) {
        this.commandService = commandService;
        this.toastService = toastService;
        this.seedIcons = seedIcons;
        this.cropIcons = cropIcons;
        this.blockIcons = blockIcons;
        this.toolIcons = toolIcons;

        ImGui.createContext();
        ImGui.getIO().setIniFilename(null);

        ImGuiIO io = ImGui.getIO();
        if (new File(K.Paths.FONT).exists()) {
            io.getFonts().addFontFromFileTTF(
                    K.Paths.FONT, K.Style.FONT_SIZE);
        }

        ImGuiStyle style = getImGuiStyle();
        setColor(style, ImGuiCol.WindowBg, K.Style.COLOR_WINDOW_BG);
        setColor(style, ImGuiCol.FrameBg, K.Style.COLOR_FRAME_BG);
        setColor(style, ImGuiCol.FrameBgHovered, K.Style.COLOR_FRAME_BG_HOVERED);
        setColor(style, ImGuiCol.FrameBgActive, K.Style.COLOR_FRAME_BG_ACTIVE);
        setColor(style, ImGuiCol.Button, K.Style.COLOR_BUTTON);
        setColor(style, ImGuiCol.ButtonHovered, K.Style.COLOR_BUTTON_HOVERED);
        setColor(style, ImGuiCol.ButtonActive, K.Style.COLOR_BUTTON_ACTIVE);
        setColor(style, ImGuiCol.Text, K.Style.COLOR_TEXT);
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init(K.Render.GLSL_VERSION);
    }

    private static ImGuiStyle getImGuiStyle() {
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(K.Style.WINDOW_ROUNDING);
        style.setFrameRounding(K.Style.FRAME_ROUNDING);
        style.setWindowBorderSize(K.Style.WINDOW_BORDER_SIZE);
        style.setFrameBorderSize(K.Style.FRAME_BORDER_SIZE);
        style.setWindowPadding(K.Style.WINDOW_PADDING_X, K.Style.WINDOW_PADDING_Y);
        style.setFramePadding(K.Style.FRAME_PADDING_X, K.Style.FRAME_PADDING_Y);
        style.setItemSpacing(K.Style.ITEM_SPACING_X, K.Style.ITEM_SPACING_Y);
        return style;
    }

    private static int getItemIconColumn(Item item) {
        if (item instanceof Seed seed && seed.getType() != null) {
            return seed.getType().getId();
        }

        if (item instanceof Crop crop && crop.getType() != null) {
            return crop.getType().getId();
        }

        if (item instanceof Block block && block.getType() != null) {
            return block.getType().getId();
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
        if (Mouse.getDeltaX() != 0.0f ||
                Mouse.getDeltaY() != 0.0f ||
                Mouse.getScrollY() != 0.0f ||
                Mouse.isButtonDown(GLFW_MOUSE_BUTTON_LEFT) ||
                Keyboard.anyKeyPressed()) {
            hudInactivityTimer = HUD_FADE_DELAY;

        } else if (hudInactivityTimer > 0.0f) {
            hudInactivityTimer -= delta;
        }

        if (actionDisplayTimer > 0.0f) {
            actionDisplayTimer -= delta;
        }

        toastService.update(delta);
    }

    public void beginFrame() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    public void renderHUD() {
        if (player == null) {
            return;
        }

        if (hudInactivityTimer > 0.0f) {
            renderInv();
            renderShop();
            renderCoordinates();
        }

        renderToasts();
    }

    public void renderToasts() {
        if (toastService.isEmpty()) {
            return;
        }

        for (Toast toast : toastService.getToasts()) {
            renderToast(toast);
        }
    }

    private void renderToast(Toast toast) {
        float x = toast.getX();
        float y = toast.getY();

        float width = K.UI.TOAST_WIDTH;
        float height = K.UI.TOAST_HEIGHT;

        float[] background = getToastBackground(toast.getType());
        float[] accent = getToastAccent(toast.getType());

        ImGui.setNextWindowPos(
                x,
                y,
                ImGuiCond.Always
        );

        ImGui.setNextWindowSize(
                width,
                height,
                ImGuiCond.Always
        );

        int flags =     ImGuiWindowFlags.NoTitleBar |
                        ImGuiWindowFlags.NoResize |
                        ImGuiWindowFlags.NoMove |
                        ImGuiWindowFlags.NoCollapse |
                        ImGuiWindowFlags.NoScrollbar |
                        ImGuiWindowFlags.NoScrollWithMouse |
                        ImGuiWindowFlags.NoInputs |
                        ImGuiWindowFlags.NoFocusOnAppearing |
                        ImGuiWindowFlags.NoNav;

        ImGui.pushStyleColor(
                ImGuiCol.WindowBg,
                background[0],
                background[1],
                background[2],
                background[3]
        );

        ImGui.pushStyleVar(
                ImGuiStyleVar.WindowRounding,
                K.UI.TOAST_ROUNDING
        );

        ImGui.pushStyleVar(
                ImGuiStyleVar.WindowPadding,
                K.UI.TOAST_PADDING_X,
                K.UI.TOAST_PADDING_Y
        );

        String windowId = "Toast##" +
                System.identityHashCode(toast);

        if (ImGui.begin(windowId, flags)) {

            float cursorX = ImGui.getCursorPosX();
            float cursorY = ImGui.getCursorPosY();

            // Accent bar
            ImGui.getWindowDrawList().addRectFilled(
                    x,
                    y,
                    x + 4.0f,
                    y + height,
                    ImGui.getColorU32(
                            accent[0],
                            accent[1],
                            accent[2],
                            accent[3]
                    )
            );

            ImGui.setCursorPosX(
                    cursorX + K.UI.TOAST_PADDING_X
            );

            ImGui.text(toast.getType().getTitle());

            ImGui.pushStyleColor(
                    ImGuiCol.Text,
                    accent[0],
                    accent[1],
                    accent[2],
                    accent[3]
            );

            ImGui.setCursorPosY(
                    ImGui.getCursorPosY() + 2.0f
            );

            ImGui.textWrapped(toast.getMessage());

            ImGui.popStyleColor();
        }

        ImGui.end();

        ImGui.popStyleVar(2);
        ImGui.popStyleColor();
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
            case SELL -> "-";
            case INFO -> "i";
            case WARNING -> "!";
            case ERROR -> "X";
            case REWARD -> "*";
        };
    }

    public void renderTooltip(Vector2i hoveredCell, World world) {
        if (hoveredCell == null || player == null) {
            return;
        }

        Crop crop = world.getCropAt(hoveredCell.x, hoveredCell.y);

        boolean hasCrop = crop != null;
        boolean hasSeedSelected = !hasCrop && selectedInventoryItem instanceof Seed;

        if (!hasCrop && !hasSeedSelected) {
            return;
        }

        ImGui.setNextWindowPos(Mouse.getX() + K.UI.TOOLTIP_OFFSET_X, Mouse.getY() +
                K.UI.TOOLTIP_OFFSET_Y, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoInputs |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoFocusOnAppearing;

        ImGui.begin("CropCardTooltip", flags);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing,
                K.UI.TOOLTIP_ITEM_SPACING_X, K.Style.LINEHEIGHT);

        if (hasCrop) {
            ImGui.text(crop.getType().getName());
            ImGui.textDisabled("Status: " + crop.getStage().getName());

        } else {
            Seed seed = (Seed) selectedInventoryItem;
            int amount = player.getInventory().getAmount(seed);
            ImGui.text(seed.getName() + " (x" + amount + ")");
            ImGui.textDisabled(seed.getDescription());
        }
        ImGui.popStyleVar();
        ImGui.end();
    }

    public boolean renderNewPlayer() {
        if (player != null) {
            return false;
        }

        ImGui.setNextWindowPos(windowWidth * K.UI.CENTER_PIVOT, windowHeight *
                K.UI.CENTER_PIVOT, ImGuiCond.Always, K.UI.CENTER_PIVOT, K.UI.CENTER_PIVOT);
        ImGui.setNextWindowSize(K.UI.NEW_PLAYER_WIDTH, K.UI.NEW_PLAYER_HEIGHT);

        int windowFlags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoCollapse;

        ImGui.begin("New Farmer", windowFlags);
        ImGui.text("What's your name, kid?");
        ImGui.pushItemWidth(K.UI.MATCH_PARENT_WIDTH);
        ImGui.setKeyboardFocusHere();
        boolean wasEnterPressed = ImGui.inputText("##PlayerName", nameBuffer,
                ImGuiInputTextFlags.EnterReturnsTrue);

        ImGui.popItemWidth();
        boolean wasButtonClicked = ImGui.button("Start",
                K.UI.MATCH_PARENT_WIDTH, K.UI.LARGE_BUTTON_HEIGHT);
        boolean shouldCreatePlayer = (wasEnterPressed || wasButtonClicked) && !nameBuffer.get().isBlank();
        ImGui.end();
        return shouldCreatePlayer;
    }

    public String inputCommand() {
        if (player == null) return null;

        ImGui.setNextWindowPos(windowWidth * K.UI.CENTER_PIVOT, windowHeight * K.UI.CENTER_PIVOT,
                ImGuiCond.Always, K.UI.CENTER_PIVOT, K.UI.CENTER_PIVOT);
        ImGui.setNextWindowSize(K.UI.INPUT_WIDTH, K.UI.INPUT_HEIGHT);

        int windowFlags = ImGuiWindowFlags.NoTitleBar |
                        ImGuiWindowFlags.NoResize |
                        ImGuiWindowFlags.NoMove |
                        ImGuiWindowFlags.NoCollapse;

        ImGui.begin("Command", windowFlags);
        ImGui.pushItemWidth(K.UI.MATCH_PARENT_WIDTH);
        int flags = ImGuiInputTextFlags.EnterReturnsTrue;

        ImGui.setKeyboardFocusHere();
        boolean enterPressed = ImGui.inputText(
                "##CommandInput",
                commandBuffer,
                flags
        );

        ImGui.popItemWidth();

        boolean runClicked = ImGui.button(
                "Run",
                K.UI.MATCH_PARENT_WIDTH,
                K.UI.LARGE_BUTTON_HEIGHT
        );

        String command = null;

        if ((enterPressed || runClicked) &&
                !commandBuffer.get().isBlank()) {
            command = commandBuffer.get().trim();
            commandBuffer.set("");
        }

        ImGui.end();
        return command;
    }

    public String getEnteredPlayerName() {
        return nameBuffer.get();
    }

    public Item getSelectedInventoryItem() {
        return selectedInventoryItem;
    }

    public void logAction(Vector2i cell) {
        this.lastActionCell = new Vector2i(cell);
        this.actionDisplayTimer = K.UI.COORD_DISPLAY_DURATION;
    }

    public void endFrame() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void onResize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public void renderInv() {
        if (player == null) return;
        ImGui.setNextWindowPos(K.UI.HUD_PADDING, windowHeight -
                K.UI.INVENTORY_HEIGHT - K.UI.HUD_PADDING, ImGuiCond.Always);
        ImGui.setNextWindowSize(K.UI.INVENTORY_WIDTH,
                K.UI.INVENTORY_HEIGHT, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoCollapse;

        if (ImGui.begin("Inventory", flags)) {
            Inventory inv = player.getInventory();
            inv.sort();

            ImGui.text(player.getName() + "'s Farm, $" + player.purse());
            ImGui.separator();

            if (inv.isEmpty()) {
                ImGui.textDisabled("You're out of stuff!");
                selectedInventoryItem = null;

            } else {

                Map<String, Map.Entry<Item, Integer>> aggregated = new LinkedHashMap<>();
                for (Map.Entry<Item, Integer> entry : inv.getItems().entrySet()) {
                    String name = entry.getKey().getName();
                    if (aggregated.containsKey(name)) {
                        int prevAmount = aggregated.get(name).getValue();
                        aggregated.put(name, new AbstractMap.SimpleEntry<>(
                                entry.getKey(), prevAmount + entry.getValue()));

                    } else {
                        aggregated.put(name, entry);
                    }
                }

                if (selectedInventoryItem != null &&
                        !aggregated.containsKey(selectedInventoryItem.getName())) {
                    selectedInventoryItem = null;
                }

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, K.Style.ITEM_SPACING, K.Style.ITEM_SPACING);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);

                int slotIndex = 0;
                for (int i = 0; i < K.World.TOTAL_SLOTS; i++) {
                    Item item = null;
                    int totalAmount = 0;
                    if (slotIndex < aggregated.size()) {
                        Map.Entry<String, Map.Entry<Item, Integer>> entry =
                                aggregated.entrySet().stream()
                                        .skip(slotIndex)
                                        .findFirst()
                                        .orElse(null);

                        if (entry != null) {
                            item = entry.getValue().getKey();
                            totalAmount = entry.getValue().getValue();
                        }
                    }

                    boolean isSelected = item != null &&
                            selectedInventoryItem != null &&
                            selectedInventoryItem.getName().equals(item.getName());

                    setColor(ImGuiCol.Button, isSelected ? K.Style.COLOR_BUTTON : K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.ButtonHovered, isSelected ? K.Style.COLOR_BUTTON_HOVERED : K.Style.COLOR_SLOT_HOVERED);
                    setColor(ImGuiCol.ButtonActive, isSelected ? K.Style.COLOR_BUTTON_ACTIVE : K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.Border, isSelected ? K.Style.COLOR_SLOT_BORDER_SEL : K.Style.COLOR_SLOT_BORDER);

                    ImGui.pushID("inv_slot_" + slotIndex);

                    if (item != null) {
                        SpriteSheet atlas = getItemSpritesheet(item);
                        int col = getItemIconColumn(item);
                        int totalCols = atlas.getTotalFrames();
                        float u0 = (float) col / totalCols;
                        float u1 = (float) (col + 1) / totalCols;

                        if (ImGui.imageButton(
                                atlas.getTextureId(),
                                K.UI.ICON_SIZE,
                                K.UI.ICON_SIZE,
                                u0, 1.0f,
                                u1, 0.0f)) {
                            selectedInventoryItem = item;
                        }

                        if (ImGui.isItemHovered() &&
                                ImGui.isMouseDoubleClicked(GLFW_MOUSE_BUTTON_LEFT)) {
                            sellItem(inv, item);
                            selectedInventoryItem = null;
                        }

                        renderSlotCount(totalAmount);

                        if (ImGui.isItemHovered()) {
                            ImGui.setTooltip(item.getName());
                        }

                    } else {
                        ImGui.button(
                                "##empty",
                                K.UI.ICON_SIZE + K.Style.FRAME_PADDING_X * 2.0f,
                                K.UI.ICON_SIZE + K.Style.FRAME_PADDING_Y * 2.0f
                        );
                    }

                    ImGui.popID();
                    ImGui.popStyleColor(4);

                    slotIndex++;

                    if (slotIndex % 4 != 0) {
                        ImGui.sameLine();
                    }
                }

                ImGui.popStyleVar(2);

            }
        }

        ImGui.end();
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
            return;
        }

        player.spend(totalPrice);
        shop.earn(totalPrice);

        stock.remove(item, amount);
        player.getInventory().add(item, amount);

        log.info("Player bought {} x{} from shop",
                item.getName(), amount);
    }

    private void renderSlotCount(int amount) {
        if (amount <= 0) return;

        float slotX = ImGui.getItemRectMinX();
        float slotY = ImGui.getItemRectMinY();
        float slotWidth = ImGui.getItemRectSizeX();
        float slotHeight = ImGui.getItemRectSizeY();

        String count = String.valueOf(amount);
        float textWidth = ImGui.calcTextSize(count).x;
        float textHeight = ImGui.getTextLineHeight();

        float x = slotX + slotWidth - textWidth - 4.0f;
        float y = slotY + slotHeight - textHeight - 2.0f;

        ImGui.getWindowDrawList().addText(x + 1.0f, y + 1.0f,
                0xFF000000, count);
        ImGui.getWindowDrawList().addText(x, y, 0xFFFFFFFF, count);
    }

    public void renderShop() {
        if (player == null || shop == null) return;
        ImGui.setNextWindowPos(windowWidth - K.UI.INVENTORY_WIDTH - K.UI.HUD_PADDING,
                windowHeight - K.UI.INVENTORY_HEIGHT - K.UI.HUD_PADDING, ImGuiCond.Always);

        ImGui.setNextWindowSize(K.UI.INVENTORY_WIDTH,
                K.UI.INVENTORY_HEIGHT, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoCollapse;

        if (ImGui.begin("Shop", flags)) {
            ImGui.text(shop.getOwner() + "'s Shop, $" + shop.purse());
            ImGui.separator();
            Inventory stock = shop.getStock();
            stock.sort();

            if (stock.isEmpty()) {
                ImGui.textDisabled("Out of stock!");
            } else {
                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing,
                        K.Style.ITEM_SPACING,
                        K.Style.ITEM_SPACING);

                ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);
                int slotIndex = 0;
                Map<Item, Integer> stockItems = new LinkedHashMap<>(stock.getItems());
                for (int i = 0; i < K.World.TOTAL_SLOTS; i++) {
                    Item item = null;
                    int amount = 0;
                    if (slotIndex < stockItems.size()) {
                        Map.Entry<Item, Integer> entry =
                                stockItems.entrySet().stream()
                                        .skip(slotIndex)
                                        .findFirst()
                                        .orElse(null);

                        if (entry != null) {
                            item = entry.getKey();
                            amount = entry.getValue();
                        }
                    }

                    setColor(ImGuiCol.Button, K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.ButtonHovered, K.Style.COLOR_SLOT_HOVERED);
                    setColor(ImGuiCol.ButtonActive, K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.Border, K.Style.COLOR_SLOT_BORDER);

                    ImGui.pushID("shop_slot_" + slotIndex);

                    if (item != null) {
                        SpriteSheet atlas = getItemSpritesheet(item);
                        int col = getItemIconColumn(item);
                        int totalCols = atlas.getTotalFrames();
                        float u0 = (float) col / totalCols;
                        float u1 = (float) (col + 1) / totalCols;

                        boolean wasClickedOn = ImGui.imageButton(
                                atlas.getTextureId(),
                                K.UI.ICON_SIZE,
                                K.UI.ICON_SIZE,
                                u0, 1.0f,
                                u1, 0.0f);

                        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(GLFW_MOUSE_BUTTON_LEFT)) {
                            buyItem(stock, item, stock.getAmount(item));
                        } else if (wasClickedOn) {
                            buyItem(stock, item, 1);
                        }

                        renderSlotCount(amount);

                        if (ImGui.isItemHovered()) {
                            ImGui.setTooltip(item.getName() + " - $" + item.getValue());
                        }

                    } else {
                        ImGui.button(
                                "##empty",
                                K.UI.ICON_SIZE + K.Style.FRAME_PADDING_X * 2.0f,
                                K.UI.ICON_SIZE + K.Style.FRAME_PADDING_Y * 2.0f
                        );
                    }

                    ImGui.popID();
                    ImGui.popStyleColor(4);

                    slotIndex++;

                    if (slotIndex % 4 != 0) {
                        ImGui.sameLine();
                    }
                }

                ImGui.popStyleVar(2);
            }
        }

        ImGui.end();
    }

    private void renderCoordinates() {
        if (player == null || actionDisplayTimer <= 0.0f || lastActionCell == null) {
            return;
        }

        ImGui.setNextWindowPos(windowWidth - K.UI.HUD_PADDING,
                K.UI.HUD_PADDING, ImGuiCond.Always, 1.0f, 0.0f);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoFocusOnAppearing;

        ImGui.begin("GridActionHUD", flags);
        ImGui.text(String.format("[%d, %d]", lastActionCell.x, lastActionCell.y));

        ImGui.end();
    }

    private void setColor(ImGuiStyle style, int target, float[] rgba) {
        style.setColor(target, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    private void setColor(int col, float[] rgba) {
        ImGui.pushStyleColor(col, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    private SpriteSheet getItemSpritesheet(Item item) {
        if (item instanceof Crop) return cropIcons;
        if (item instanceof Seed) return seedIcons;
        if (item instanceof Block) return blockIcons;
        if (item instanceof Coin) return toolIcons;
        return seedIcons;
    }
}