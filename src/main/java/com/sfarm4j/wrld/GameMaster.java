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
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

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
    private Spritesheet wheat;
    private Spritesheet carrot;
    private Spritesheet seedIcons;
    private Spritesheet cropIcons;
    private Camera camera;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Sunlight sunlight;

    private Vector2i hoveredCell = null;
    private CropType currentCrop = CropType.WHEAT;

    private float windowWidth = K.Window.DEFAULT_WIDTH;
    private float windowHeight = K.Window.DEFAULT_HEIGHT;
    private Player player;

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final ImString nameBuffer = new ImString("", K.UI.PLAYER_NAME_MAX_LENGTH);

    private Vector2i lastActionCell = null;
    private float actionDisplayTimer = 0.0f;
    private Item selectedInventoryItem = null;

    public GameMaster(long windowHandle) {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService();
        this.cellService = new CellService();
        this.sunlight = new Sunlight(K.Sunlight.DEFAULT_DIRECTION);

        for (int x = 0; x < K.World.GRID_SIZE; x++) {
            for (int z = 0; z < K.World.GRID_SIZE; z++) {
                cellService.setCell(CellType.TILLED, x, z);
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
        this.wheat = new Spritesheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.carrot = new Spritesheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.seedIcons = new Spritesheet(K.Paths.SEED_ICONS, K.UI.ICON_ATLAS_FRAMES);
        this.cropIcons = new Spritesheet(K.Paths.CROP_ICONS, K.UI.ICON_ATLAS_FRAMES);

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

    private void setColor(ImGuiStyle style, int target, float[] rgba) {
        style.setColor(target, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public void update(float delta) {
        if (player == null) {
            return;
        }

        if (actionDisplayTimer > 0.0f) {
            actionDisplayTimer -= delta;
        }

        timeService.update(delta);
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
                        if (currentCrop != null) {
                            cropService.plant(
                                    hoveredCell.x,
                                    hoveredCell.y,
                                    player,
                                    cellService.getCell(hoveredCell.x, hoveredCell.y),
                                    currentCrop,
                                    timeService.getCurrentSeason()
                            );
                            logAction(hoveredCell);
                        } else {
                            log.warn("No crop was selected");
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
            Spritesheet sheet = (crop.getType() == CropType.WHEAT) ? wheat : carrot;
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
                        Spritesheet sheet = (crop.getType() == CropType.WHEAT) ? wheat : carrot;
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

        renderInv();
        renderCoordinates();

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
                log.info("Player created: {}", player.getName());
            }
            ImGui.end();
        }

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void renderInv() {
        if (player == null) return;
        ImGui.setNextWindowPos(K.UI.INVENTORY_POS_X, windowHeight -
                K.UI.INVENTORY_POS_Y_OFFSET, ImGuiCond.FirstUseEver);

        ImGui.setNextWindowSize(K.UI.INVENTORY_WIDTH,
                K.UI.INVENTORY_HEIGHT, ImGuiCond.FirstUseEver);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize   |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoMove;

        if (ImGui.begin("Inventory", flags)) {
            Inventory inv = player.getInventory();

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

                int totalAtlasColumns = seedIcons.getTotalFrames();
                int totalCropAtlasColumns = cropIcons.getTotalFrames();
                float iconSize = 32.0f;

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, K.Style.ITEM_SPACING, K.Style.ITEM_SPACING);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1.0f);

                for (Map.Entry<String, Map.Entry<Item, Integer>> entry : aggregated.entrySet()) {
                    Item item = entry.getValue().getKey();
                    int totalAmount = entry.getValue().getValue();

                    boolean isSelected = (selectedInventoryItem != null &&
                            selectedInventoryItem.getName().equals(item.getName()));

                    Spritesheet atlas = getItemSpritesheet(item);
                    int iconIndex = getItemIconIndex(item);

                    float u0 = (float) iconIndex / totalAtlasColumns;
                    float u1 = (float) (iconIndex + 1) / totalAtlasColumns;
                    float v0 = 1.0f;
                    float v1 = 0.0f;

                    if (isSelected) {
                        ImGui.pushStyleColor(ImGuiCol.Button,
                                K.Style.COLOR_BUTTON_HOVERED[0],
                                K.Style.COLOR_BUTTON_HOVERED[1],
                                K.Style.COLOR_BUTTON_HOVERED[2],
                                K.Style.COLOR_BUTTON_HOVERED[3]);
                        ImGui.pushStyleColor(ImGuiCol.ButtonHovered,
                                K.Style.COLOR_BUTTON_ACTIVE[0],
                                K.Style.COLOR_BUTTON_ACTIVE[1],
                                K.Style.COLOR_BUTTON_ACTIVE[2],
                                K.Style.COLOR_BUTTON_ACTIVE[3]);
                    }

                    ImGui.pushID("inv_item_" + item.getName());
                    if (ImGui.imageButton(atlas.getTextureId(), iconSize, iconSize, u0, v0, u1, v1)) {
                        selectedInventoryItem = item;
                        this.currentCrop = resolveCropType(item);
                    }

                    if (isSelected) {
                        ImGui.popStyleColor(2);
                    }

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
                        for (Map.Entry<Item, Integer> entry : new HashMap<>(inv.getItems()).entrySet()) {
                            if (entry.getKey().getName().equals(selectedInventoryItem.getName())) {
                                player.sell(entry.getKey(), entry.getValue());
                            }
                        }
                        selectedInventoryItem = null;
                        currentCrop = null;
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

    private void renderCoordinates() {
        if (player == null || actionDisplayTimer <= 0.0f
                || lastActionCell == null) return;
        ImGui.setNextWindowPos(
                windowWidth - K.UI.HUD_PADDING,
                K.UI.HUD_PADDING,
                ImGuiCond.Always,
                1.0f, 0.0f
        );

        int flags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.AlwaysAutoResize
                | ImGuiWindowFlags.NoFocusOnAppearing;

        ImGui.begin("GridActionHUD", flags);
        ImGui.text(String.format("[%d, %d]", lastActionCell.x, lastActionCell.y));
        ImGui.end();
    }

    private CropType resolveCropType(Item item) {
        if (item instanceof Seed seed) {
            return seed.getType();
        }
        return null;
    }

    private Spritesheet getItemSpritesheet(Item item) {
        if (item instanceof Crop) {
            return cropIcons;
        }
        return seedIcons;
    }

    private int getItemIconIndex(Item item) {
        if (item instanceof Seed seed && seed.getType() != null) {
            return seed.getType().getId();
        }
        if (item instanceof Crop crop && crop.getType() != null) {
            return crop.getType().getId();
        }
        return 0;
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