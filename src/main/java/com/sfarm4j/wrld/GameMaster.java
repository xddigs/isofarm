package com.sfarm4j.wrld;

import com.sfarm4j.data.*;
import com.sfarm4j.graphics.*;
import com.sfarm4j.input.Keyboard;
import com.sfarm4j.input.Mouse;
import com.sfarm4j.service.CellService;
import com.sfarm4j.service.CropService;
import com.sfarm4j.service.TimeService;
import com.sfarm4j.utils.K;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImString;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

@SuppressWarnings("all")
public class GameMaster {
    private static final Logger log = LoggerFactory.getLogger(GameMaster.class);
    private final World world;
    private final CropService cropService;
    private final TimeService timeService;
    private final CellService cellService;

    private Shader defaultShader;
    private Shader outlineShader;
    private Framebuffer maskFbo;
    private Mesh screenQuadMesh;

    private Mesh blockMesh;
    private Mesh selectionMesh;
    private Mesh spriteMesh;
    private SpriteSheet wheat;
    private SpriteSheet carrot;
    private SpriteSheet potato;
    private final Map<CropType, SpriteSheet> cropSpritesheets;
    private SpriteSheet seedIcons;
    private SpriteSheet cropIcons;
    private Camera camera;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Sunlight sunlight;

    private Vector2i hoveredCell = null;

    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;

    private Player player;
    private Shop shop;

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final ImString nameBuffer = new ImString("", K.UI.PLAYER_NAME_MAX_LENGTH);

    private Vector2i lastActionCell = null;
    private float actionDisplayTimer = 0.0f;
    private Item selectedInventoryItem = null;

    private float hudInactivityTimer = 0.0f;
    private static final float HUD_FADE_DELAY = 2.0f;

    public GameMaster(long windowHandle) {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService();
        this.cellService = new CellService();
        this.sunlight = new Sunlight(K.Sunlight.DEFAULT_DIRECTION);

        for (int x = 0; x < K.World.GRID_SIZE; x++) {
            for (int z = 0; z < K.World.GRID_SIZE; z++) {
                cellService.setCell(CellType.TILLED, x, z);
                Cell cell = cellService.find(x, z);
                if (cell != null) {
                    cell.setUnlocked(true);
                }
            }
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);

        this.defaultShader = new Shader(K.Paths.DEFAULT_VERT_SHADER, K.Paths.DEFAULT_FRAG_SHADER);
        this.outlineShader = new Shader(K.Paths.OUTLINE_VERT_SHADER, K.Paths.OUTLINE_FRAG_SHADER);

        this.maskFbo = new Framebuffer((int) windowWidth, (int) windowHeight);
        this.screenQuadMesh = Mesh.screenQuad();

        this.blockMesh = Mesh.createMesh(K.World.DEFAULT_BLOCK_DEPTH);
        this.selectionMesh = Mesh.selection();
        this.spriteMesh = Mesh.createCrop();
        this.cropSpritesheets = new EnumMap(CropType.class);
        this.wheat = new SpriteSheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.carrot = new SpriteSheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.potato = new SpriteSheet(K.Paths.POTATO_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.seedIcons = new SpriteSheet(K.Paths.SEED_ICONS, K.UI.ICON_ATLAS_FRAMES);
        this.cropIcons = new SpriteSheet(K.Paths.CROP_ICONS, K.UI.ICON_ATLAS_FRAMES);
        this.shop = new Shop();

        cropSpritesheets.put(CropType.WHEAT, wheat);
        cropSpritesheets.put(CropType.CARROT, carrot);
        cropSpritesheets.put(CropType.POTATO, potato);

        this.camera = new Camera(K.Camera.DEFAULT_WIDTH, K.Camera.DEFAULT_HEIGHT);
        this.camera.setPosition(0.0f, 0.0f, 0.0f);
        recenter();

        ImGui.createContext();
        ImGui.getIO().setIniFilename(null);

        ImGuiIO io = ImGui.getIO();
        if (new File(K.Paths.FONT).exists()) {
            io.getFonts().addFontFromFileTTF(K.Paths.FONT, K.Style.FONT_SIZE);
        }

        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(K.Style.WINDOW_ROUNDING);
        style.setFrameRounding(K.Style.FRAME_ROUNDING);
        style.setWindowBorderSize(K.Style.WINDOW_BORDER_SIZE);
        style.setFrameBorderSize(K.Style.FRAME_BORDER_SIZE);
        style.setWindowPadding(K.Style.WINDOW_PADDING_X, K.Style.WINDOW_PADDING_Y);
        style.setFramePadding(K.Style.FRAME_PADDING_X, K.Style.FRAME_PADDING_Y);
        style.setItemSpacing(K.Style.ITEM_SPACING_X, K.Style.ITEM_SPACING_Y);
        setColor(style, ImGuiCol.WindowBg,        K.Style.COLOR_WINDOW_BG);
        setColor(style, ImGuiCol.FrameBg,         K.Style.COLOR_FRAME_BG);
        setColor(style, ImGuiCol.FrameBgHovered,  K.Style.COLOR_FRAME_BG_HOVERED);
        setColor(style, ImGuiCol.FrameBgActive,   K.Style.COLOR_FRAME_BG_ACTIVE);
        setColor(style, ImGuiCol.Button,          K.Style.COLOR_BUTTON);
        setColor(style, ImGuiCol.ButtonHovered,   K.Style.COLOR_BUTTON_HOVERED);
        setColor(style, ImGuiCol.ButtonActive,    K.Style.COLOR_BUTTON_ACTIVE);
        setColor(style, ImGuiCol.Text,            K.Style.COLOR_TEXT);

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init(K.Render.GLSL_VERSION);
        log.info("GameMaster initialized with grid size: {}x{}",
                K.World.GRID_SIZE, K.World.GRID_SIZE);
    }

    public void update(float delta) {
        if (player == null) {
            return;
        }

        if (Mouse.getDeltaX() != 0.0f || Mouse.getDeltaY() != 0.0f ||
                Mouse.getScrollY() != 0.0f || Mouse.isButtonDown(GLFW_MOUSE_BUTTON_LEFT) ||
                Keyboard.anyKeyPressed()) {
            hudInactivityTimer = HUD_FADE_DELAY;
        } else if (hudInactivityTimer > 0.0f) {
            hudInactivityTimer -= delta;
        }

        if (actionDisplayTimer > 0.0f) {
            actionDisplayTimer -= delta;
        }

        timeService.update(delta);
        shop.update(timeService);
        cropService.update(delta);
        camera.update(delta);

        ImGuiIO io = ImGui.getIO();

        if (!io.getWantCaptureMouse()) {
            boolean isCtrlDown = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL) ||
                    Keyboard.isKeyDown(GLFW_KEY_RIGHT_CONTROL);
            if (Mouse.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                if (isCtrlDown) {
                    camera.rotateYaw(Mouse.getDeltaX() * K.Camera.ROTATION_SENSITIVITY);
                } else {
                    camera.pan(Mouse.getDeltaX(), Mouse.getDeltaY(), K.Camera.PAN_SENSITIVITY);
                }
            }

            float scrollY = Mouse.getScrollY();
            if (scrollY != 0.0f) {
                camera.zoom(scrollY);
            }

            if (Mouse.isButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
                recenter();
            }

            Vector2i cell = camera.highlight(Mouse.getX(), Mouse.getY(),
                    windowWidth, windowHeight);
            if (cell != null && cell.x >= 0 && cell.x < K.World.GRID_SIZE
                    && cell.y >= 0 && cell.y < K.World.GRID_SIZE) {
                hoveredCell = cell;
            } else {
                hoveredCell = null;
            }

            if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && hoveredCell != null) {
                Crop crop = world.getCropAt(hoveredCell.x, hoveredCell.y);

                if (crop != null) {
                    if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                        if (crop.isReadyToHarvest()) {
                            cropService.harvest(player, crop);
                            logAction(hoveredCell);
                        } else if (!crop.isReadyToHarvest() || !crop.wasHarvested()) {
                            cropService.rip(crop);
                        }
                    }
                } else {
                    if (!Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                        if (selectedInventoryItem instanceof CellExpansion) {
                            if (cellService.unlockCell(hoveredCell.x, hoveredCell.y)) {
                                player.remove(selectedInventoryItem);
                                selectedInventoryItem = null;
                                log.info("New cell unlocked at {},{}", hoveredCell.x, hoveredCell.y);
                            }
                        } else if (selectedInventoryItem instanceof Seed &&
                                cellService.isUnlocked(hoveredCell.x, hoveredCell.y)) {
                            if (((Seed) selectedInventoryItem).getType() != null) {
                                Crop planted = cropService.plant(
                                        hoveredCell.x,
                                        hoveredCell.y,
                                        player,
                                        cellService.find(hoveredCell.x, hoveredCell.y),
                                        ((Seed) selectedInventoryItem).getType(),
                                        timeService.getCurrentSeason()
                                );
                                if (planted != null) {
                                    selectedInventoryItem = null;
                                    logAction(hoveredCell);
                                }
                            } else {
                                log.warn("No crop was selected");
                            }
                        }
                    }
                }
            }
        } else {
            hoveredCell = null;
        }

        Mouse.update();
        Keyboard.update();
    }

    public void render() {
        glActiveTexture(GL_TEXTURE0);
        defaultShader.bind();
        defaultShader.setUniform("uIsMaskPass", false);

        defaultShader.setUniform("uProjection", camera.getProjectionMatrix());
        defaultShader.setUniform("uView", camera.getViewMatrix());

        defaultShader.setUniform("uSunColor", TimeService.getSunLightColor());
        defaultShader.setUniform("uLightIntensity", TimeService.getSunIntensity());
        defaultShader.setUniform("uLightDirection", sunlight.getDirection());

        defaultShader.setUniform("uUseTexture", false);
        defaultShader.setUniform("uBaseColor", K.Colors.CELL_EVEN);
        cellService.renderAll(defaultShader, blockMesh, modelMatrix, sunlight);

        defaultShader.setUniform("uUseTexture", true);
        defaultShader.setUniform("uTexture", K.Render.PRIMARY_TEXTURE_UNIT);
        defaultShader.setUniform("uTotalFrames", wheat.getTotalFrames());

        world.getActiveCrops().forEach(crop -> {
            SpriteSheet sheet = cropSpritesheets.get(crop.getType());
            sheet.bind();

            modelMatrix.identity().translate(crop.getX(), K.World.CROP_ELEVATION_Y, crop.getZ());
            defaultShader.setUniform("uModel", modelMatrix);
            defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());
            spriteMesh.render();

            sheet.unbind();
        });

        if (hoveredCell != null) {
            defaultShader.setUniform("uUseTexture", false);
            modelMatrix.identity().translate(hoveredCell.x, 0.0f, hoveredCell.y);
            defaultShader.setUniform("uModel", modelMatrix);
            selectionMesh.renderLines();
        }

        if (hoveredCell != null) {
            maskFbo.bind();
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            defaultShader.setUniform("uIsMaskPass", true);
            defaultShader.setUniform("uUseTexture", true);

            world.getActiveCrops().stream()
                    .filter(c -> Math.round(c.getX()) == hoveredCell.x
                            && Math.round(c.getZ()) == hoveredCell.y)
                    .findFirst()
                    .ifPresent(crop -> {
                        SpriteSheet sheet = cropSpritesheets.get(crop.getType());
                        sheet.bind();
                        modelMatrix.identity().translate(crop.getX(), K.World.CROP_ELEVATION_Y, crop.getZ());
                        defaultShader.setUniform("uModel", modelMatrix);
                        defaultShader.setUniform("uFrameIndex", crop.getStage().getFrameIndex());
                        spriteMesh.render();
                        sheet.unbind();
            });

            maskFbo.unbind((int) windowWidth, (int) windowHeight);
            glDisable(GL_DEPTH_TEST);

            outlineShader.bind();
            outlineShader.setUniform("uScreenSize", new Vector2f(windowWidth, windowHeight));
            outlineShader.setUniform("uOutlineColor", K.Colors.OUTLINE_DEFAULT);
            outlineShader.setUniform("uMaskTexture", K.Render.PRIMARY_TEXTURE_UNIT);

            glBindTexture(GL_TEXTURE_2D, maskFbo.getTextureId());
            screenQuadMesh.render();

            outlineShader.unbind();
            glEnable(GL_DEPTH_TEST);
        }

        defaultShader.unbind();

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        if (hudInactivityTimer > 0.0f) {
            renderInv();
            renderShop();
            renderCoordinates();
        }

        if (hoveredCell != null && player != null) {
            Crop crop = world.getCropAt(hoveredCell.x, hoveredCell.y);
            boolean hasCrop = (crop != null);
            boolean hasSeedSelected = (!hasCrop && selectedInventoryItem instanceof Seed);

            if (hasCrop || hasSeedSelected) {
                ImGui.setNextWindowPos(Mouse.getX() + K.UI.TOOLTIP_OFFSET_X,
                        Mouse.getY() + K.UI.TOOLTIP_OFFSET_Y,
                        ImGuiCond.Always);

                int flags = ImGuiWindowFlags.NoTitleBar
                        | ImGuiWindowFlags.NoResize
                        | ImGuiWindowFlags.NoMove
                        | ImGuiWindowFlags.AlwaysAutoResize
                        | ImGuiWindowFlags.NoFocusOnAppearing;

                ImGui.begin("CropCardTooltip", flags);
                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing,
                        K.UI.TOOLTIP_ITEM_SPACING_X, K.Style.LINEHEIGHT);

                if (hasCrop) {
                    ImGui.text(crop.getType().getName());
                    ImGui.textDisabled("Status: " + crop.getStage());
                } else {
                    Seed seed = (Seed) selectedInventoryItem;
                    int amount = player.getInventory().getAmount(seed);
                    ImGui.text(seed.getName() + " (x" + amount + ")");
                    ImGui.textDisabled(seed.getDescription());
                }

                ImGui.popStyleVar();
                ImGui.end();
            }
        }

        if (player == null) {
            ImGui.setNextWindowPos(windowWidth * K.UI.CENTER_PIVOT,
                    windowHeight * K.UI.CENTER_PIVOT,
                    ImGuiCond.Always,
                    K.UI.CENTER_PIVOT, K.UI.CENTER_PIVOT);

            ImGui.setNextWindowSize(K.UI.NEW_PLAYER_WIDTH, K.UI.NEW_PLAYER_HEIGHT);
            int windowFlags = ImGuiWindowFlags.NoTitleBar
                    | ImGuiWindowFlags.NoResize
                    | ImGuiWindowFlags.NoMove
                    | ImGuiWindowFlags.NoCollapse;

            ImGui.begin("New Farmer", windowFlags);
            ImGui.text("What's your name, kid?");

            ImGui.pushItemWidth(K.UI.MATCH_PARENT_WIDTH);
            boolean wasEnterPressed = ImGui.inputText("##PlayerName", nameBuffer,
                    ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.popItemWidth();

            boolean wasButtonClicked = ImGui.button("Start", K.UI.MATCH_PARENT_WIDTH,
                    K.UI.LARGE_BUTTON_HEIGHT);

            if ((wasEnterPressed || wasButtonClicked) && !nameBuffer.get().isBlank()) {
                this.player = new Player(nameBuffer.get());
                this.shop.setPlayer(player);
                log.info("Player created: {}", player.getName());
            }
            ImGui.end();
        }

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void renderInv() {
        if (player == null) return;
        ImGui.setNextWindowPos(K.UI.HUD_PADDING,
                windowHeight - K.UI.INVENTORY_HEIGHT - K.UI.HUD_PADDING,
                ImGuiCond.Always);

        ImGui.setNextWindowSize(K.UI.INVENTORY_WIDTH,
                K.UI.INVENTORY_HEIGHT, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize   |
                ImGuiWindowFlags.NoCollapse;

        if (ImGui.begin("Inventory", flags)) {
            Inventory inv = player.getInventory();
            ImGui.text(player.getName() + "'s Farm, $" + player.getMoney());
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

                if (selectedInventoryItem != null && !aggregated
                        .containsKey(selectedInventoryItem.getName())) {
                    selectedInventoryItem = null;
                }

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, K.Style.ITEM_SPACING, K.Style.ITEM_SPACING);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);

                for (Map.Entry<String, Map.Entry<Item, Integer>> entry : aggregated.entrySet()) {
                    Item item = entry.getValue().getKey();
                    int totalAmount = entry.getValue().getValue();

                    boolean isSelected = (selectedInventoryItem != null &&
                            selectedInventoryItem.getName().equals(item.getName()));

                    SpriteSheet atlas = getItemSpritesheet(item);
                    int col = getItemIconColumn(item);
                    int row = getItemIconRow(item);

                    int totalCols = atlas.getTotalFrames();
                    int totalRows = getItemIconRows(item);
                    float iconSize = K.UI.ICON_SIZE;

                    float u0 = (float) col / totalCols;
                    float u1 = (float) (col + 1) / totalCols;
                    float v0 = (float) (row + 1) / totalRows;
                    float v1 = (float) row / totalRows;

                    if (isSelected) {
                        setColor(ImGuiCol.Button,        K.Style.COLOR_BUTTON);
                        setColor(ImGuiCol.ButtonHovered, K.Style.COLOR_BUTTON_HOVERED);
                        setColor(ImGuiCol.ButtonActive,  K.Style.COLOR_BUTTON_ACTIVE);
                        setColor(ImGuiCol.Border,        K.Style.COLOR_SLOT_BORDER_SEL);
                    } else {
                        setColor(ImGuiCol.Button,        K.Style.COLOR_SLOT_BG);
                        setColor(ImGuiCol.ButtonHovered, K.Style.COLOR_SLOT_HOVERED);
                        setColor(ImGuiCol.ButtonActive,  K.Style.COLOR_SLOT_BG);
                        setColor(ImGuiCol.Border,        K.Style.COLOR_SLOT_BORDER);
                    }

                    ImGui.pushID("inv_item_" + item.getName());
                    if (ImGui.imageButton(atlas.getTextureId(), iconSize, iconSize, u0, v0, u1, v1)) {
                        selectedInventoryItem = item;
                    }

                    ImGui.popStyleColor(4);

                    if (ImGui.isItemHovered()) {
                        ImGui.setTooltip(item.getName() + " x" + totalAmount);
                    }

                    ImGui.sameLine();
                    ImGui.popID();
                }

                ImGui.popStyleVar(2);
                ImGui.newLine();
                ImGui.separator();

                if (selectedInventoryItem != null) {
                    int totalAmount = aggregated.get(selectedInventoryItem.getName()).getValue();

                    if (ImGui.button("Sell " + selectedInventoryItem.getName() +
                            " (x" + totalAmount + ")", K.UI.MATCH_PARENT_WIDTH, 0)) {

                        Item targetItem = null;
                        int cumulativeAmount = 0;

                        for (Map.Entry<Item, Integer> entry : new HashMap<>(inv.getItems()).entrySet()) {
                            if (entry.getKey().getName().equals(selectedInventoryItem.getName())) {
                                targetItem = entry.getKey();
                                cumulativeAmount += entry.getValue();
                                player.sell(entry.getKey(), entry.getValue());
                            }
                        }

                        if (targetItem != null && cumulativeAmount > 0) {
                            shop.buy(targetItem, cumulativeAmount);
                        }

                        selectedInventoryItem = null;
                    }
                } else {
                    ImGui.beginDisabled();
                    ImGui.button("Select an item to sell", K.UI.MATCH_PARENT_WIDTH, 0);
                    ImGui.endDisabled();
                }
            }
        }
        ImGui.end();
    }

    public void renderShop() {
        if (player == null || shop == null) return;
        ImGui.setNextWindowPos(windowWidth - K.UI.INVENTORY_WIDTH - K.UI.HUD_PADDING,
                windowHeight - K.UI.INVENTORY_HEIGHT - K.UI.HUD_PADDING,
                ImGuiCond.Always);

        ImGui.setNextWindowSize(K.UI.INVENTORY_WIDTH,
                K.UI.INVENTORY_HEIGHT, ImGuiCond.Always);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize   |
                ImGuiWindowFlags.NoCollapse;

        if (ImGui.begin("Shop", flags)) {
            ImGui.text(shop.getOwner() + "'s Shop, $" + shop.getMoney());
            ImGui.separator();

            Inventory stock = shop.getStock();

            if (stock.isEmpty()) {
                ImGui.textDisabled("Out of stock!");
            } else {
                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, K.Style.ITEM_SPACING, K.Style.ITEM_SPACING);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);

                for (Map.Entry<Item, Integer> entry : new HashMap<>(stock.getItems()).entrySet()) {
                    Item item = entry.getKey();
                    int amount = entry.getValue();

                    SpriteSheet atlas = getItemSpritesheet(item);
                    int col = getItemIconColumn(item);
                    int row = getItemIconRow(item);

                    int totalCols = atlas.getTotalFrames();
                    int totalRows = getItemIconRows(item);
                    float iconSize = K.UI.ICON_SIZE;

                    float u0 = (float) col / totalCols;
                    float u1 = (float) (col + 1) / totalCols;
                    float v0 = (float) (row + 1) / totalRows;
                    float v1 = (float) row / totalRows;

                    setColor(ImGuiCol.Button,        K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.ButtonHovered, K.Style.COLOR_SLOT_HOVERED);
                    setColor(ImGuiCol.ButtonActive,  K.Style.COLOR_SLOT_BG);
                    setColor(ImGuiCol.Border,        K.Style.COLOR_SLOT_BORDER);

                    ImGui.pushID("shop_item_" + item.getName());
                    if (ImGui.imageButton(atlas.getTextureId(), iconSize, iconSize, u0, v0, u1, v1)) {
                        if (player.getMoney() >= item.getValue()) {
                            player.earn(-item.getValue());
                            shop.earn(item.getValue());
                            stock.remove(item, 1);
                            player.getInventory().add(item, 1);
                            log.info("Player bought {} from shop", item.getName());
                        } else {
                            log.warn("Player doesn't have enough money to buy {}", item.getName());
                        }
                    }
                    ImGui.popStyleColor(4);

                    if (ImGui.isItemHovered()) {
                        ImGui.setTooltip(item.getName() + " - $" + item.getValue() + " (Stock: " + amount + ")");
                    }

                    ImGui.sameLine();
                    ImGui.popID();
                }

                ImGui.popStyleVar(2);
            }
        }
        ImGui.end();
    }

    private void renderCoordinates() {
        if (player == null || actionDisplayTimer <= 0.0f
                || lastActionCell == null) return;
        ImGui.setNextWindowPos(windowWidth - K.UI.HUD_PADDING,
                K.UI.HUD_PADDING,
                ImGuiCond.Always,
                1.0f, 0.0f);

        int flags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.AlwaysAutoResize
                | ImGuiWindowFlags.NoFocusOnAppearing;

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

    private CropType resolveCropType(Item item) {
        if (item instanceof Seed seed) {
            return seed.getType();
        }
        return null;
    }

    private static int getItemIconColumn(Item item) {
        if (item instanceof CellExpansion) {
            return 0;
        }

        if (item instanceof Seed seed && seed.getType() != null) {
            return seed.getType().getId();
        }

        if (item instanceof Crop crop && crop.getType() != null) {
            return crop.getType().getId();
        }

        return 0;
    }

    private static int getItemIconRow(Item item) {
        if (item instanceof CellExpansion) {
            return 0;
        }

        if (item instanceof Crop) {
            return 1;
        }

        return 0;
    }

    private static int getItemIconRows(Item item) {
        if (item instanceof Crop || item instanceof CellExpansion) {
            return 2;
        }

        return 1;
    }

    private SpriteSheet getItemSpritesheet(Item item) {
        if (item instanceof Crop || item instanceof CellExpansion) {
            return cropIcons;
        }

        return seedIcons;
    }

    private static int getItemIconIndex(Item item) {
        if (item instanceof Seed seed && seed.getType() != null) {
            return seed.getType().getId();
        }
        if (item instanceof Crop crop && crop.getType() != null) {
            return crop.getType().getId();
        }
        return 0;
    }

    private void addToStock(Item item, int amount) {
        if (shop == null) return;
        shop.add(item, amount);
    }

    private void logAction(Vector2i cell) {
        this.lastActionCell = new Vector2i(cell);
        this.actionDisplayTimer = K.UI.COORD_DISPLAY_DURATION;
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();

        blockMesh.dispose();
        selectionMesh.dispose();
        spriteMesh.dispose();
        screenQuadMesh.dispose();

        wheat.dispose();
        carrot.dispose();
        potato.dispose();

        cropIcons.dispose();
        seedIcons.dispose();

        maskFbo.dispose();
        defaultShader.dispose();
        outlineShader.dispose();
        log.info("GameMaster resources successfully cleaned up");
    }

    public void recenter() {
        float center = (K.World.GRID_SIZE - 1) / 2.0f;
        this.camera.setPosition(center, 0.0f, center);
    }

    public void onResize(int newWidth, int newHeight) {
        this.windowWidth = newWidth;
        this.windowHeight = newHeight;

        if (camera != null) {
            camera.updateProjection(newWidth, newHeight);
        }

        if (maskFbo != null) {
            maskFbo.dispose();
            maskFbo = new Framebuffer(newWidth, newHeight);
        }
    }
}