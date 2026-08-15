package com.sfarm4j.input;

import com.sfarm4j.data.CellExpansion;
import com.sfarm4j.data.Crop;
import com.sfarm4j.data.Item;
import com.sfarm4j.data.Seed;
import com.sfarm4j.graphics.Camera;
import com.sfarm4j.service.*;
import com.sfarm4j.utils.K;
import com.sfarm4j.wrld.GameMaster;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;

public class GameInteraction implements Service<GameInteraction> {
    private static final Logger log = LoggerFactory.getLogger(GameInteraction.class);
    private final CropService cropService;
    private final GameUIService gameUIservice;
    private final CellService cellService;
    private final TimeService timeService;

    private final Camera camera;

    public GameInteraction(CropService cropService,
                           GameUIService gameUIservice,
                           CellService cellService,
                           TimeService timeService,
                           Camera camera) {
        this.cropService = cropService;
        this.gameUIservice = gameUIservice;
        this.cellService = cellService;
        this.timeService = timeService;
        this.camera = camera;
    }

    public Vector2i update(GameMaster gameMaster, Item selectedItem) {
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
            gameMaster.recenter();
        }

        Vector2i hoveredCell = camera.highlight(Mouse.getX(), Mouse.getY(),
                gameMaster.getWindowWidth(), gameMaster.getWindowHeight());

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && hoveredCell != null) {
            Crop crop = gameMaster.getWorld().getCropAt(hoveredCell.x, hoveredCell.y);

            if (crop != null) {
                if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                    if (crop.isReadyToHarvest()) {
                        cropService.harvest(gameMaster.getPlayer(), crop);
                        gameUIservice.logAction(hoveredCell);
                    } else if (!crop.isReadyToHarvest() || !crop.wasHarvested()) {
                        cropService.rip(crop);
                    }
                }
            } else {
                if (selectedItem instanceof CellExpansion) {
                    log.info("Trying to unlock cell {},{}", hoveredCell.x, hoveredCell.y);
                    if (cellService.expandCell(hoveredCell.x, hoveredCell.y)) {
                        gameMaster.getPlayer().remove(selectedItem);

                        log.info("New cell unlocked at {},{}",
                                hoveredCell.x, hoveredCell.y);
                    } else {
                        log.warn("Could not expand cell {},{}",
                                hoveredCell.x, hoveredCell.y);
                    }
                } else if (selectedItem instanceof Seed &&
                        cellService.isUnlocked(hoveredCell.x, hoveredCell.y)) {
                    if (((Seed) selectedItem).getType() != null) {
                        Crop planted = cropService.plant(
                                hoveredCell.x,
                                hoveredCell.y,
                                gameMaster.getPlayer(),
                                cellService.find(hoveredCell.x, hoveredCell.y),
                                ((Seed) selectedItem).getType(),
                                timeService.getCurrentSeason()
                        );
                        if (planted != null) {
                            gameUIservice.logAction(hoveredCell);
                        }
                    } else {
                        log.warn("No crop was selected");
                    }
                }
            }
        }
        return hoveredCell;
    }
}
