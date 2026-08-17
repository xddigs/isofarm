package com.tilled.service;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
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
import org.joml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.Math;
import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@SuppressWarnings("all")
public class GameUIService implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(GameUIService.class);
    private final GameMaster gameMaster;
    private final ImGuiImplGlfw imGuiGlfw;
    private final ImGuiImplGl3 imGuiGl3;
    private final ImString nameBuffer;
    private final ImString commandBuffer;
    private final CommandService commandService;

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
            GameMaster gameMaster,
            CommandService commandService,
            SpriteSheet seedIcons,
            SpriteSheet cropIcons,
            SpriteSheet blockIcons,
            SpriteSheet toolIcons
    ) {
        this.gameMaster = gameMaster;
        this.commandService = commandService;
        this.seedIcons = seedIcons;
        this.cropIcons = cropIcons;
        this.blockIcons = blockIcons;
        this.toolIcons = toolIcons;

        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl3 = new ImGuiImplGl3();
        this.nameBuffer = new ImString("", K.UI.PLAYER_NAME_MAX_LENGTH);
        this.commandBuffer = new ImString("", K.UI.COMMAND_MAX_LENGTH);

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
        setColor(style, ImGuiCol.FrameBgHovered,
                K.Style.COLOR_FRAME_BG_HOVERED);
        setColor(style, ImGuiCol.FrameBgActive,
                K.Style.COLOR_FRAME_BG_ACTIVE);
        setColor(style, ImGuiCol.Button, K.Style.COLOR_BUTTON);
        setColor(style, ImGuiCol.ButtonHovered,
                K.Style.COLOR_BUTTON_HOVERED);
        setColor(style, ImGuiCol.ButtonActive,
                K.Style.COLOR_BUTTON_ACTIVE);
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

        gameMaster.getToastService().update(delta);
        float scroll = Mouse.getScrollY();
        if (scroll != 0) {
            selectItem(scroll > 0 ? -1 : 1);
        }
    }

    private Vector2f worldToScreen(Vector3f worldPosition) {
        Matrix4f view = gameMaster.getCamera().getViewMatrix();
        Matrix4f projection = gameMaster.getCamera().getProjectionMatrix();

        Vector4f clipSpace = new Vector4f(worldPosition, 1.0f);

        projection.mul(view).transform(clipSpace);

        if (clipSpace.w <= 0.0f) {
            return null;
        }

        float ndcX = clipSpace.x / clipSpace.w;
        float ndcY = clipSpace.y / clipSpace.w;

        float screenX = (ndcX + 1.0f) * 0.5f * windowWidth;
        float screenY = (1.0f - ndcY) * 0.5f * windowHeight;

        return new Vector2f(screenX, screenY);
    }

    public void beginFrame() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    public void renderHUD(GameMaster gameMaster) {
        if (player == null) {
            return;
        }

        if (gameMaster.isInventoryOpen()) {
            renderInv();
            renderShop();
        }

        renderHotbar();
        renderHotbarLabel();
        renderToasts();
        renderCrosshair(windowWidth, windowHeight);
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
        float size = 8.0f;
        float thickness = 2.0f;

        ImGui.getForegroundDrawList().addLine(
                centerX - size, centerY, centerX + size, centerY,
                ImGui.getColorU32(1.0f, 1.0f, 1.0f, 0.8f), thickness);

        ImGui.getForegroundDrawList().addLine(
                centerX, centerY - size, centerX, centerY + size,
                ImGui.getColorU32(1.0f, 1.0f, 1.0f, 0.8f), thickness);
    }

    public void renderHotbar() {
        if (player == null) return;

        float hotbarWidth =
                (K.UI.ICON_SIZE + K.Style.FRAME_PADDING_X * 2.0f)
                        * K.UI.HOTBAR_SLOTS
                        + K.Style.ITEM_SPACING * (K.UI.HOTBAR_SLOTS - 1)
                        + K.Style.WINDOW_PADDING_X * 2.0f;

        float hotbarHeight = K.UI.ICON_SIZE
                + K.Style.FRAME_PADDING_Y * 2.0f
                + K.Style.WINDOW_PADDING_Y * 2.0f;

        ImGui.setNextWindowPos(
                windowWidth / 2 - hotbarWidth / 2,
                windowHeight - hotbarHeight - K.UI.HUD_PADDING,
                ImGuiCond.Always);
        ImGui.setNextWindowSize(hotbarWidth, hotbarHeight, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoBackground;

        if (ImGui.begin("Inventory", flags)) {
            Inventory inv = player.getInventory();
            inv.sort();
            List<Item> hotbarItems = inv.getHotbarItems();

            if (selectedInventoryItem != null
                    && !hotbarItems.contains(selectedInventoryItem)) {
                selectedInventoryItem = null;
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing,
                    K.Style.ITEM_SPACING, K.Style.ITEM_SPACING);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);

            for (int slotIndex = 0; slotIndex < K.UI.HOTBAR_SLOTS;
                 slotIndex++) {
                Item item = slotIndex < hotbarItems.size()
                        ? hotbarItems.get(slotIndex) : null;
                int totalAmount = item != null ? inv.getAmount(item) : 0;

                boolean isSelected = selectedHotbarSlot == slotIndex;

                setColor(ImGuiCol.Button, isSelected
                        ? K.Style.COLOR_BUTTON : K.Style.COLOR_SLOT_BG);
                setColor(ImGuiCol.ButtonHovered, isSelected
                        ? K.Style.COLOR_BUTTON_HOVERED
                        : K.Style.COLOR_SLOT_HOVERED);
                setColor(ImGuiCol.ButtonActive, isSelected
                        ? K.Style.COLOR_BUTTON_ACTIVE : K.Style.COLOR_SLOT_BG);
                setColor(ImGuiCol.Border, isSelected
                        ? K.Style.COLOR_SLOT_BORDER_SEL
                        : K.Style.COLOR_SLOT_BORDER);

                ImGui.pushID("hotbar_slot_" + slotIndex);

                if (item != null) {
                    SpriteSheet atlas = getItemSpritesheet(item);
                    int col = getItemIconColumn(item);
                    int totalCols = atlas.getTotalFrames();
                    float u0 = (float) col / totalCols;
                    float u1 = (float) (col + 1) / totalCols;

                    if (ImGui.imageButton(atlas.getTextureId(),
                            K.UI.ICON_SIZE, K.UI.ICON_SIZE,
                            u0, 1.0f, u1, 0.0f)) {
                        selectedInventoryItem = item;
                        selectedHotbarSlot = slotIndex;
                        showHotbarLabel(item);
                    }

                    renderSlotCount(totalAmount);

                    if (ImGui.isItemHovered()) {
                        ImGui.setTooltip(item.getName());
                    }

                } else {
                    if (ImGui.button("##empty",
                            K.UI.ICON_SIZE + K.Style.FRAME_PADDING_X * 2.0f,
                            K.UI.ICON_SIZE + K.Style.FRAME_PADDING_Y * 2.0f)) {
                        selectedInventoryItem = null;
                        selectedHotbarSlot = slotIndex;
                    }
                }

                ImGui.popID();
                ImGui.popStyleColor(4);

                if (slotIndex < K.UI.HOTBAR_SLOTS - 1) {
                    ImGui.sameLine();
                }
            }

            ImGui.popStyleVar(2);
        }

        ImGui.end();
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
    }

    private void renderHotbarLabel() {
        if (hotbarLabel == null || hotbarLabelTimer <= 0.0f) {
            return;
        }

        float fadeStart = K.UI.HOTBAR_LABEL_FADE_DURATION;
        float alpha = 1.0f;

        if (hotbarLabelTimer < fadeStart) {
            alpha = hotbarLabelTimer / fadeStart;
        }

        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        float textWidth = ImGui.calcTextSize(hotbarLabel).x;
        float textHeight = ImGui.getTextLineHeight();

        float width = textWidth + K.UI.HOTBAR_LABEL_PADDING_X * 2.0f;
        float height = textHeight + K.UI.HOTBAR_LABEL_PADDING_Y * 2.0f;
        float x = windowWidth * 0.5f - width * 0.5f;
        float hotbarBottom = windowHeight - K.UI.HUD_PADDING;
        float y = hotbarBottom - height - K.UI.HOTBAR_LABEL_OFFSET_Y;

        int bg = ImGui.getColorU32(
                K.Colors.COLOR_HOTBAR_LABEL_BG[0],
                K.Colors.COLOR_HOTBAR_LABEL_BG[1],
                K.Colors.COLOR_HOTBAR_LABEL_BG[2],
                K.Colors.COLOR_HOTBAR_LABEL_BG[3] * alpha);

        int border = ImGui.getColorU32(
                K.Colors.COLOR_HOTBAR_LABEL_BORDER[0],
                K.Colors.COLOR_HOTBAR_LABEL_BORDER[1],
                K.Colors.COLOR_HOTBAR_LABEL_BORDER[2],
                K.Colors.COLOR_HOTBAR_LABEL_BORDER[3] * alpha);

        int text = ImGui.getColorU32(
                K.Colors.COLOR_HOTBAR_LABEL_TEXT[0],
                K.Colors.COLOR_HOTBAR_LABEL_TEXT[1],
                K.Colors.COLOR_HOTBAR_LABEL_TEXT[2],
                K.Colors.COLOR_HOTBAR_LABEL_TEXT[3] * alpha);

        ImGui.getForegroundDrawList().addRectFilled(
                x, y, x + width, y + height, bg, K.UI.HOTBAR_LABEL_ROUNDING);

        ImGui.getForegroundDrawList().addRect(
                x, y, x + width, y + height, border,
                K.UI.HOTBAR_LABEL_ROUNDING, 0, K.UI.HOTBAR_LABEL_BORDER);
        ImGui.getForegroundDrawList().addText(
                x + K.UI.HOTBAR_LABEL_PADDING_X,
                y + K.UI.HOTBAR_LABEL_PADDING_Y, text, hotbarLabel);
    }

    private void renderToast(Toast toast) {
        float x = toast.getX();
        float y = toast.getY();

        float width = K.UI.TOAST_WIDTH;
        float[] background = getToastBackground(toast.getType());
        float[] accent = getToastAccent(toast.getType());

        ImGui.setNextWindowPos(x, y, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, 0.0f, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoScrollbar
                | ImGuiWindowFlags.NoScrollWithMouse
                | ImGuiWindowFlags.NoInputs
                | ImGuiWindowFlags.NoFocusOnAppearing
                | ImGuiWindowFlags.NoNav
                | ImGuiWindowFlags.AlwaysAutoResize;

        ImGui.pushStyleColor(ImGuiCol.WindowBg,
                background[0], background[1], background[2], background[3]);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, K.UI.TOAST_ROUNDING);
        ImGui.pushStyleVar(
                ImGuiStyleVar.WindowPadding,
                K.UI.TOAST_PADDING_X, K.UI.TOAST_PADDING_Y);

        String windowId = "Toast##" + System.identityHashCode(toast);

        if (ImGui.begin(windowId, flags)) {
            float cursorX = ImGui.getCursorPosX();
            float cursorY = ImGui.getCursorPosY();

            ImGui.getWindowDrawList().addRectFilled(
                    x, y, x + 4.0f, y,
                    ImGui.getColorU32(
                            accent[0], accent[1], accent[2], accent[3]));

            ImGui.pushStyleColor(ImGuiCol.Text,
                    accent[0], accent[1], accent[2], accent[3]);

            ImGui.setCursorPos(cursorX + K.UI.TOAST_PADDING_X, cursorY);
            ImGui.text(getToastPrefix(toast.getType()));
            ImGui.popStyleColor();

            ImGui.setCursorPos(
                    cursorX + K.UI.TOAST_PADDING_X
                            + K.UI.TOAST_ICON_SIZE + K.UI.TOAST_PADDING_X,
                    cursorY);

            ImGui.pushStyleColor(ImGuiCol.Text,
                    accent[0], accent[1], accent[2], accent[3]);

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0.0f, 2.0f);
            ImGui.text(toast.getType().getTitle());
            ImGui.textWrapped(toast.getMessage());

            ImGui.popStyleVar();
            ImGui.popStyleColor();
        }

        ImGui.end();

        ImGui.popStyleVar(2);
        ImGui.popStyleColor();
    }

    private float[] getToastAccent(ToastData type) {
        return switch (type) {
            case SUCCESS  -> K.Style.COLOR_TOAST_SUCCESS;
            case INFO     -> K.Style.COLOR_TOAST_INFO;
            case WARNING  -> K.Style.COLOR_TOAST_WARNING;
            case ERROR    -> K.Style.COLOR_TOAST_ERROR;
            case REWARD   -> K.Style.COLOR_TOAST_REWARD;
            case PURCHASE -> K.Style.COLOR_TOAST_SUCCESS;
            case SELL     -> K.Style.COLOR_TOAST_REWARD;
        };
    }

    private float[] getToastBackground(ToastData type) {
        return switch (type) {
            case SUCCESS  -> K.Style.COLOR_TOAST_SUCCESS_BG;
            case INFO     -> K.Style.COLOR_TOAST_INFO_BG;
            case WARNING  -> K.Style.COLOR_TOAST_WARNING_BG;
            case ERROR    -> K.Style.COLOR_TOAST_ERROR_BG;
            case REWARD   -> K.Style.COLOR_TOAST_REWARD_BG;
            case PURCHASE -> K.Style.COLOR_TOAST_SUCCESS_BG;
            case SELL     -> K.Style.COLOR_TOAST_REWARD_BG;
        };
    }

    private String getToastPrefix(ToastData type) {
        return switch (type) {
            case PURCHASE, SUCCESS -> "+";
            case SELL              -> "**";
            case INFO              -> "i";
            case WARNING           -> "!";
            case ERROR             -> "X";
            case REWARD            -> "*";
        };
    }

    private boolean isWorldInteractable(Item item) {
        return item instanceof Tool || item instanceof Seed;
    }

    public void renderTooltip(Hit hoveredCell, World world) {
        if (hoveredCell == null || player == null) {
            return;
        }

        String name = null;
        Crop crop = world.getCropAt(
                hoveredCell.x(), hoveredCell.y(), hoveredCell.z());

        if (crop != null) {
            name = crop.getCropType().getName();
        } else if (selectedInventoryItem != null
                && isWorldInteractable(selectedInventoryItem)) {
            name = selectedInventoryItem.getName();
        }

        if (name == null || name.isBlank()) {
            return;
        }

        Vector3f worldPosition = new Vector3f(
                hoveredCell.x() + 0.5f,
                hoveredCell.y() + K.UI.WORLD_TOOLTIP_OFFSET_Y,
                hoveredCell.z() + 0.5f);
        Vector2f screenPosition = worldToScreen(worldPosition);

        if (screenPosition == null) {
            return;
        }

        float distance = getCameraDistance(worldPosition);
        float scale = getWorldTooltipScale(distance);
        renderWorldTooltip(name, screenPosition, scale);
    }

    private void renderWorldTooltip(
            String text, Vector2f screenPosition, float scale) {
        float textWidth = ImGui.calcTextSize(text).x;
        float textHeight = ImGui.getTextLineHeight();

        float width = (textWidth + K.UI.WORLD_TOOLTIP_PADDING_X * 2.0f) * scale;
        float height =
                (textHeight + K.UI.WORLD_TOOLTIP_PADDING_Y * 2.0f) * scale;

        float x = screenPosition.x - width * 0.5f;
        float y = screenPosition.y - height;

        int background = ImGui.getColorU32(
                K.Colors.COLOR_WORLD_TOOLTIP_BG[0],
                K.Colors.COLOR_WORLD_TOOLTIP_BG[1],
                K.Colors.COLOR_WORLD_TOOLTIP_BG[2],
                K.Colors.COLOR_WORLD_TOOLTIP_BG[3]);
        int border = ImGui.getColorU32(
                K.Colors.COLOR_WORLD_TOOLTIP_BORDER[0],
                K.Colors.COLOR_WORLD_TOOLTIP_BORDER[1],
                K.Colors.COLOR_WORLD_TOOLTIP_BORDER[2],
                K.Colors.COLOR_WORLD_TOOLTIP_BORDER[3]);
        int textColor = ImGui.getColorU32(
                K.Colors.COLOR_WORLD_TOOLTIP_TEXT[0],
                K.Colors.COLOR_WORLD_TOOLTIP_TEXT[1],
                K.Colors.COLOR_WORLD_TOOLTIP_TEXT[2],
                K.Colors.COLOR_WORLD_TOOLTIP_TEXT[3]);

        ImGui.getForegroundDrawList().addRectFilled(
                x, y, x + width, y + height,
                background, K.UI.WORLD_TOOLTIP_ROUNDING * scale);
        ImGui.getForegroundDrawList().addRect(
                x, y, x + width, y + height, border,
                K.UI.WORLD_TOOLTIP_ROUNDING * scale, 0,
                K.UI.WORLD_TOOLTIP_BORDER * scale);

        float textX = x + K.UI.WORLD_TOOLTIP_PADDING_X * scale;
        float textY = y + K.UI.WORLD_TOOLTIP_PADDING_Y * scale;
        ImGui.getForegroundDrawList().addText(textX, textY, textColor, text);
    }

    private float getWorldTooltipScale(float distance) {
        float normalized = distance / K.UI.WORLD_TOOLTIP_SCALE_DISTANCE;
        float scale = K.UI.WORLD_TOOLTIP_MAX_SCALE
                - normalized * (K.UI.WORLD_TOOLTIP_MAX_SCALE
                - K.UI.WORLD_TOOLTIP_MIN_SCALE);
        return Math.max(K.UI.WORLD_TOOLTIP_MIN_SCALE,
                Math.min(K.UI.WORLD_TOOLTIP_MAX_SCALE, scale));
    }

    private float getCameraDistance(Vector3f worldPosition) {
        Vector3f cameraPosition = gameMaster.getCamera().getPosition();
        return cameraPosition.distance(worldPosition);
    }

    public boolean renderNewPlayer() {
        if (player != null) {
            return false;
        }

        ImGui.setNextWindowPos(
                windowWidth * K.UI.CENTER_PIVOT,
                windowHeight * K.UI.CENTER_PIVOT,
                ImGuiCond.Always,
                K.UI.CENTER_PIVOT, K.UI.CENTER_PIVOT);
        ImGui.setNextWindowSize(
                K.UI.NEW_PLAYER_WIDTH, K.UI.NEW_PLAYER_HEIGHT);

        int windowFlags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoCollapse;

        ImGui.begin("New Farmer", windowFlags);
        ImGui.text("What's your name, kid?");
        ImGui.pushItemWidth(K.UI.MATCH_PARENT_WIDTH);
        ImGui.setKeyboardFocusHere();

        boolean wasEnterPressed = ImGui.inputText(
                "##PlayerName", nameBuffer, ImGuiInputTextFlags.EnterReturnsTrue);

        ImGui.popItemWidth();
        boolean wasButtonClicked = ImGui.button(
                "Start", K.UI.MATCH_PARENT_WIDTH, K.UI.LARGE_BUTTON_HEIGHT);
        boolean shouldCreatePlayer = (wasEnterPressed || wasButtonClicked)
                && !nameBuffer.get().isBlank();
        ImGui.end();
        return shouldCreatePlayer;
    }

    public String inputCommand() {
        if (player == null) return null;

        ImGui.setNextWindowPos(
                windowWidth * K.UI.CENTER_PIVOT,
                windowHeight * K.UI.CENTER_PIVOT,
                ImGuiCond.Always,
                K.UI.CENTER_PIVOT, K.UI.CENTER_PIVOT);
        ImGui.setNextWindowSize(K.UI.INPUT_WIDTH, K.UI.INPUT_HEIGHT);

        int windowFlags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoCollapse;

        ImGui.begin("Command", windowFlags);
        ImGui.pushItemWidth(K.UI.MATCH_PARENT_WIDTH);
        int flags = ImGuiInputTextFlags.EnterReturnsTrue;

        ImGui.setKeyboardFocusHere();
        boolean enterPressed = ImGui.inputText(
                "##CommandInput", commandBuffer, flags);

        ImGui.popItemWidth();

        boolean runClicked = ImGui.button(
                "Run", K.UI.MATCH_PARENT_WIDTH, K.UI.LARGE_BUTTON_HEIGHT);

        String command = null;

        if ((enterPressed || runClicked) && !commandBuffer.get().isBlank()) {
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

    public void logAction(Hit cell) {
        this.lastActionCell = new Vector2i(cell.x(), cell.y());
        this.actionDisplayTimer = K.UI.COORD_DISPLAY_DURATION;
    }

    public void endFrame() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void onResize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        gameMaster.getToastService().setWindowWidth(width);
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public void renderInv() {
        if (player == null) return;
        Inventory inv = player.getInventory();
        inv.sort();

        int columns = K.UI.HOTBAR_SLOTS;
        int rows = (int) Math.ceil((double) K.World.TOTAL_SLOTS / columns);

        Map<String, Map.Entry<Item, Integer>> aggregated =
                new LinkedHashMap<>();
        for (Map.Entry<Item, Integer> entry : inv.getItems().entrySet()) {
            String name = entry.getKey().getName();
            if (aggregated.containsKey(name)) {
                int previousAmount = aggregated.get(name).getValue();
                aggregated.put(name, new AbstractMap.SimpleEntry<>(
                        entry.getKey(), previousAmount + entry.getValue()));
            } else {
                aggregated.put(name, entry);
            }
        }

        float slotWidth = K.UI.ICON_SIZE + K.Style.FRAME_PADDING_X * 2.0f;
        float slotHeight = K.UI.ICON_SIZE + K.Style.FRAME_PADDING_Y * 2.0f;

        float inventoryWidth = columns * slotWidth
                + (columns - 1) * K.Style.ITEM_SPACING
                + K.Style.WINDOW_PADDING_X * 2.0f;
        float inventoryHeight = rows * slotHeight
                + (rows - 1) * K.Style.ITEM_SPACING
                + K.Style.WINDOW_PADDING_Y * 3.0f
                + ImGui.getTextLineHeight() * 2.0f
                + K.Style.ITEM_SPACING;

        ImGui.setNextWindowPos(
                windowWidth / 2.0f, windowHeight / 2.0f,
                ImGuiCond.Always, 0.5f, 0.5f);

        ImGui.setNextWindowSize(
                inventoryWidth, inventoryHeight, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoScrollbar
                | ImGuiWindowFlags.NoScrollWithMouse;

        if (ImGui.begin("Inventory", flags)) {
            ImGui.text(player.getName() + "'s Farm, $" + player.purse());
            ImGui.separator();
            if (inv.isEmpty()) {
                ImGui.textDisabled("You're out of stuff!");
                selectedInventoryItem = null;
            } else {
                if (selectedInventoryItem != null
                        && !aggregated.containsKey(
                        selectedInventoryItem.getName())) {
                    selectedInventoryItem = null;
                }

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing,
                        K.Style.ITEM_SPACING, K.Style.ITEM_SPACING);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);

                int slotIndex = 0;
                for (int i = 0; i < K.World.TOTAL_SLOTS; i++) {
                    Item item = null;
                    int totalAmount = 0;

                    if (slotIndex < aggregated.size()) {
                        Map.Entry<String, Map.Entry<Item, Integer>> entry =
                                aggregated.entrySet().stream()
                                        .skip(slotIndex).findFirst().orElse(null);
                        if (entry != null) {
                            item = entry.getValue().getKey();
                            totalAmount = entry.getValue().getValue();
                        }
                    }

                    boolean isSelected = selectedInventorySlot == slotIndex;
                    setColor(ImGuiCol.Button, isSelected
                            ? K.Style.COLOR_BUTTON : K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.ButtonHovered, isSelected
                            ? K.Style.COLOR_BUTTON_HOVERED
                            : K.Style.COLOR_SLOT_HOVERED);
                    setColor(ImGuiCol.ButtonActive, isSelected
                            ? K.Style.COLOR_BUTTON_ACTIVE : K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.Border, isSelected
                            ? K.Style.COLOR_SLOT_BORDER_SEL
                            : K.Style.COLOR_SLOT_BORDER);

                    ImGui.pushID("inv_slot_" + slotIndex);

                    if (item != null) {
                        SpriteSheet atlas = getItemSpritesheet(item);
                        int col = getItemIconColumn(item);
                        int totalCols = atlas.getTotalFrames();
                        float u0 = (float) col / totalCols;
                        float u1 = (float) (col + 1) / totalCols;

                        if (ImGui.imageButton(atlas.getTextureId(),
                                K.UI.ICON_SIZE, K.UI.ICON_SIZE,
                                u0, 1.0f, u1, 0.0f)) {
                            selectedInventoryItem = item;
                            selectedInventorySlot = slotIndex;
                        }

                        if (ImGui.isItemHovered() && ImGui
                                .isMouseDoubleClicked(GLFW_MOUSE_BUTTON_LEFT)) {
                            sellItem(inv, item);
                            selectedInventoryItem = null;
                        }

                        renderSlotCount(totalAmount);

                        if (ImGui.isItemHovered()) {
                            ImGui.setTooltip(item.getName());
                        }

                    } else if (ImGui.button("##empty",
                            K.UI.ICON_SIZE + K.Style.FRAME_PADDING_X * 2.0f,
                            K.UI.ICON_SIZE + K.Style.FRAME_PADDING_Y * 2.0f)) {
                        selectedInventoryItem = null;
                        selectedInventorySlot = slotIndex;
                    }

                    ImGui.popID();
                    ImGui.popStyleColor(4);

                    slotIndex++;

                    if (slotIndex % columns != 0) {
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

        ImGui.getWindowDrawList().addText(x + 1.0f, y + 1.0f, 0xFF000000, count);
        ImGui.getWindowDrawList().addText(x, y, 0xFFFFFFFF, count);
    }

    public void renderShop() {
        if (player == null || shop == null) return;
        ImGui.setNextWindowPos(
                windowWidth - K.UI.INVENTORY_WIDTH - K.UI.HUD_PADDING,
                windowHeight - K.UI.INVENTORY_HEIGHT - K.UI.HUD_PADDING,
                ImGuiCond.Always);
        ImGui.setNextWindowSize(
                K.UI.INVENTORY_WIDTH, K.UI.INVENTORY_HEIGHT, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoScrollbar
                | ImGuiWindowFlags.NoScrollWithMouse;

        if (ImGui.begin("Shop", flags)) {
            ImGui.text(shop.getOwner() + "'s Shop, $" + shop.purse());
            ImGui.separator();
            Inventory stock = shop.getStock();
            stock.sort();

            if (stock.isEmpty()) {
                ImGui.textDisabled("Out of stock!");
            } else {
                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing,
                        K.Style.ITEM_SPACING, K.Style.ITEM_SPACING);

                ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);
                int slotIndex = 0;
                Map<Item, Integer> stockItems =
                        new LinkedHashMap<>(stock.getItems());
                for (int i = 0; i < K.World.TOTAL_SLOTS_STOCK; i++) {
                    Item item = null;
                    int amount = 0;
                    if (slotIndex < stockItems.size()) {
                        Map.Entry<Item, Integer> entry =
                                stockItems.entrySet().stream()
                                        .skip(slotIndex).findFirst().orElse(null);

                        if (entry != null) {
                            item = entry.getKey();
                            amount = entry.getValue();
                        }
                    }

                    setColor(ImGuiCol.Button, K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.ButtonHovered,
                            K.Style.COLOR_SLOT_HOVERED);
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
                                K.UI.ICON_SIZE, K.UI.ICON_SIZE,
                                u0, 1.0f, u1, 0.0f);

                        if (ImGui.isItemHovered() && ImGui
                                .isMouseDoubleClicked(GLFW_MOUSE_BUTTON_LEFT)) {
                            buyItem(stock, item, stock.getAmount(item));
                        } else if (wasClickedOn) {
                            buyItem(stock, item, 1);
                        }

                        renderSlotCount(amount);

                        if (ImGui.isItemHovered()) {
                            ImGui.setTooltip(
                                    item.getName() + " - $" + item.getValue());
                        }

                    } else {
                        ImGui.button("##empty",
                                K.UI.ICON_SIZE + K.Style.FRAME_PADDING_X * 2.0f,
                                K.UI.ICON_SIZE + K.Style.FRAME_PADDING_Y * 2.0f);
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
        if (item instanceof Tool) return toolIcons;
        return seedIcons;
    }
}