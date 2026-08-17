package com.tilled.service;

import com.tilled.data.*;
import com.tilled.graphics.SpriteSheet;
import com.tilled.input.Mouse;
import com.tilled.utils.K;
import com.tilled.utils.Settings;
import com.tilled.wrld.GameMaster;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.ImVec2;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImString;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
            renderSliders();
        }

        renderHotbar();
        renderHotbarLabel();
        renderToasts();

        if (!gameMaster.isInventoryOpen()) {
            renderCrosshair(windowWidth, windowHeight);
        }
    }

    private void renderSliders() {
        if (player == null) return;

        float windowWidth = K.UI.SETTINGS_PANEL_WIDTH;
        ImGui.setNextWindowPos(K.UI.HUD_PADDING, windowHeight / 2, ImGuiCond.Always);
        ImGui.setNextWindowSize(windowWidth, 0.0f, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoScrollbar |
                ImGuiWindowFlags.NoScrollWithMouse |
                ImGuiWindowFlags.AlwaysAutoResize;

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, K.Style.WINDOW_ROUNDING);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, K.Style.FRAME_ROUNDING);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, K.Style.WINDOW_PADDING_X, K.Style.WINDOW_PADDING_Y);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, K.Style.ITEM_SPACING, K.Style.ITEM_SPACING_Y);

        ImGui.pushStyleColor(ImGuiCol.WindowBg, K.Style.COLOR_WINDOW_BG[0], K.Style.COLOR_WINDOW_BG[1], K.Style.COLOR_WINDOW_BG[2], K.Style.COLOR_WINDOW_BG[3]);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, K.Style.COLOR_SLOT_BG[0], K.Style.COLOR_SLOT_BG[1], K.Style.COLOR_SLOT_BG[2], K.Style.COLOR_SLOT_BG[3]);
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, K.Style.COLOR_SLOT_HOVERED[0], K.Style.COLOR_SLOT_HOVERED[1], K.Style.COLOR_SLOT_HOVERED[2], K.Style.COLOR_SLOT_HOVERED[3]);
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, K.Style.COLOR_SLOT_BORDER[0], K.Style.COLOR_SLOT_BORDER[1], K.Style.COLOR_SLOT_BORDER[2], K.Style.COLOR_SLOT_BORDER[3]);

        ImGui.pushStyleColor(ImGuiCol.SliderGrab, K.Style.COLOR_SLOT_BORDER_SEL[0], K.Style.COLOR_SLOT_BORDER_SEL[1], K.Style.COLOR_SLOT_BORDER_SEL[2], K.Style.COLOR_SLOT_BORDER_SEL[3]);
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, K.Style.COLOR_BUTTON_HOVERED[0], K.Style.COLOR_BUTTON_HOVERED[1], K.Style.COLOR_BUTTON_HOVERED[2], K.Style.COLOR_BUTTON_HOVERED[3]);
        ImGui.pushStyleColor(ImGuiCol.Text, K.Style.COLOR_TEXT[0], K.Style.COLOR_TEXT[1], K.Style.COLOR_TEXT[2], K.Style.COLOR_TEXT[3]);

        if (ImGui.begin("HUD_Settings_Panel", flags)) {
            int[] rdBuffer = new int[]{ Settings.renderDistance };
            ImGui.textDisabled("Render");
            ImGui.pushItemWidth(-1);
            if (ImGui.sliderInt("##RenderDistance", rdBuffer, 2, 32, "%d Chunks")) {
                Settings.renderDistance = rdBuffer[0];
                if (gameMaster != null && gameMaster.getCamera() != null) {
                    gameMaster.getCamera().updateProjection(windowWidth, windowHeight, Settings.renderDistance);
                    gameMaster.setLastPlayerChunkX(Integer.MAX_VALUE);
                }
            }
            ImGui.popItemWidth();

            float[] fovBuffer = new float[]{ Settings.fov };
            ImGui.textDisabled("FOV");
            ImGui.pushItemWidth(-1);
            if (ImGui.sliderFloat("##FOV", fovBuffer, 60.0f, 110.0f, "%.0f°")) {
                Settings.fov = fovBuffer[0];
                if (gameMaster != null && gameMaster.getCamera() != null) {
                    gameMaster.getCamera().setFov(Settings.fov);
                }
            }
            ImGui.popItemWidth();

            float[] sensBuffer = new float[]{ Settings.mouseSensitivity };
            ImGui.textDisabled("Sensivity");
            ImGui.pushItemWidth(-1);
            if (ImGui.sliderFloat("##Sensitivity", sensBuffer, 0.05f, 1.0f, "%.2f")) {
                Settings.mouseSensitivity = sensBuffer[0];
            }
            ImGui.popItemWidth();

            int[] scaleBuffer = new int[]{ Settings.guiScaleIndex };
            ImGui.textDisabled("GUI Scale");
            ImGui.pushItemWidth(-1);
            if (ImGui.sliderInt("##guiScale", scaleBuffer, 0, 
                    Settings.GUI_SCALES.length - 1, "Scale %dx")) {
                Settings.guiScaleIndex = scaleBuffer[0];
            }
            ImGui.popItemWidth();
        }
        ImGui.end();
        ImGui.popStyleColor(7);
        ImGui.popStyleVar(4);
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

        ImGui.getForegroundDrawList().addLine(
                centerX - size, centerY, centerX + size, centerY,
                ImGui.getColorU32(1.0f, 1.0f, 1.0f, 0.8f), thickness);

        ImGui.getForegroundDrawList().addLine(
                centerX, centerY - size, centerX, centerY + size,
                ImGui.getColorU32(1.0f, 1.0f, 1.0f, 0.8f), thickness);
    }

    public void renderHotbar() {
        if (player == null) return;

        float hotbarWidth = (Settings.getScaledIconSize() + K.Style.FRAME_PADDING_X * 2.0f)
                        * K.UI.HOTBAR_SLOTS
                        + K.Style.ITEM_SPACING * (K.UI.HOTBAR_SLOTS - 1)
                        + K.Style.WINDOW_PADDING_X * 2.0f;

        float hotbarHeight = Settings.getScaledIconSize()
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
                            Settings.getScaledIconSize(), Settings.getScaledIconSize(),
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
                            Settings.getScaledIconSize() + K.Style.FRAME_PADDING_X * 2.0f,
                            Settings.getScaledIconSize() + K.Style.FRAME_PADDING_Y * 2.0f)) {
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

            ImGui.getWindowDrawList().addRectFilled(x, y, x + 4.0f, y,
                    ImGui.getColorU32(accent[0], accent[1], accent[2], accent[3]));

            ImGui.pushStyleColor(ImGuiCol.Text,accent[0], accent[1], accent[2], accent[3]);

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
        int columns = K.UI.HOTBAR_SLOTS;
        int rows = (int) Math.ceil((double) K.World.TOTAL_SLOTS / columns);

        float slotWidth = Settings.getScaledIconSize() + K.Style.FRAME_PADDING_X * 2.0f;
        float slotHeight = Settings.getScaledIconSize() + K.Style.FRAME_PADDING_Y * 2.0f;

        float inventoryWidth = columns * slotWidth + (columns - 1) * K.Style.ITEM_SPACING +
                K.Style.WINDOW_PADDING_X * 2.0f;
        float inventoryHeight = rows * slotHeight + (rows - 1) * K.Style.ITEM_SPACING +
                K.Style.WINDOW_PADDING_Y * 3.0f + ImGui.getTextLineHeight() * 2.0f + K.Style.ITEM_SPACING;

        ImGui.setNextWindowPos(windowWidth / 2.0f, windowHeight / 2.0f, ImGuiCond.Always, 0.5f, 0.5f);
        ImGui.setNextWindowSize(inventoryWidth, inventoryHeight, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoScrollbar |
                ImGuiWindowFlags.NoScrollWithMouse;

        if (ImGui.begin("Inventory", flags)) {
            ImGui.text(player.getName() + "'s Farm, $" + player.purse());
            ImGui.separator();

            List<InventorySlot> slots = inv.getSlots();

            if (selectedInventorySlot >= slots.size()) {
                selectedInventorySlot = -1;
                selectedInventoryItem = null;
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, K.Style.ITEM_SPACING, K.Style.ITEM_SPACING);

            ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);

            for (int slotIndex = 0; slotIndex < K.World.TOTAL_SLOTS; slotIndex++) {
                InventorySlot slot = slots.get(slotIndex);
                Item item = !slot.isEmpty() ? slot.getItem() : null;
                int amount = item != null ? item.getAmount() : 0;
                boolean isSelected = selectedInventorySlot == slotIndex;
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

                    if (ImGui.imageButton(atlas.getTextureId(), Settings.getScaledIconSize(),
                            Settings.getScaledIconSize(), u0, 1.0f, u1, 0.0f)) {

                        if (selectedInventorySlot == -1) {
                            selectedInventorySlot = slotIndex;
                            selectedInventoryItem = item;
                        } else if (selectedInventorySlot == slotIndex) {
                            selectedInventorySlot = -1;
                            selectedInventoryItem = null;
                        } else {
                            int fromIndex = selectedInventorySlot;
                            inv.pickAndDrop(fromIndex, slotIndex);
                            selectedInventorySlot = -1;
                            selectedInventoryItem = null;
                        }
                    }

                    renderSlotCount(amount);

                    if (ImGui.isItemHovered()) {
                        ImGui.setTooltip(item.getName());
                    }
                } else {
                    if (ImGui.button("##empty", Settings.getScaledIconSize() +
                            K.Style.FRAME_PADDING_X * 2.0f, Settings.getScaledIconSize() +
                            K.Style.FRAME_PADDING_Y * 2.0f)) {

                        if (selectedInventorySlot != -1) {
                            int fromIndex = selectedInventorySlot;
                            inv.pickAndDrop(fromIndex, slotIndex);
                            selectedInventorySlot = -1;
                            selectedInventoryItem = null;
                        }
                    }
                }

                ImGui.popID();
                ImGui.popStyleColor(4);

                if (slotIndex % columns != columns - 1) {
                    ImGui.sameLine();
                }
            }

            ImGui.popStyleVar(2);
        }

        ImGui.end();
        renderDraggedItem();
    }

    private void renderDraggedItem() {
        if (selectedInventorySlot == -1 || selectedInventoryItem == null) return;

        ImVec2 mousePos = new ImVec2();
        ImGui.getMousePos(mousePos);
        ImGui.setNextWindowPos(mousePos.x + 10.0f, mousePos.y + 10.0f);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoInputs |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoSavedSettings |
                ImGuiWindowFlags.NoFocusOnAppearing;

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 2.0f, 2.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.0f, 0.0f, 0.0f, 0.0f);

        if (ImGui.begin("##dragged_item", flags)) {
            SpriteSheet atlas = getItemSpritesheet(selectedInventoryItem);
            int col = getItemIconColumn(selectedInventoryItem);
            int totalCols = atlas.getTotalFrames();

            float u0 = (float) col / totalCols;
            float u1 = (float) (col + 1) / totalCols;

            ImGui.image(atlas.getTextureId(), Settings.getScaledIconSize(), Settings.getScaledIconSize(), u0, 1.0f, u1, 0.0f);
            int amount = selectedInventoryItem.getAmount();
            if (amount > 1) {
                renderSlotCount(amount);
            }
        }
        ImGui.end();
        ImGui.popStyleColor(1);
        ImGui.popStyleVar(2);
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
                                Settings.getScaledIconSize(), Settings.getScaledIconSize(),
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
                                Settings.getScaledIconSize() + K.Style.FRAME_PADDING_X * 2.0f,
                                Settings.getScaledIconSize() + K.Style.FRAME_PADDING_Y * 2.0f);
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