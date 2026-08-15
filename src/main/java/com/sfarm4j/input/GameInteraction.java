package com.sfarm4j.input;

import com.sfarm4j.data.*;
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

        Vector2i hoveredCell = camera.highlight(
                Mouse.getX(),
                Mouse.getY(),
                gameMaster.getWindowWidth(),
                gameMaster.getWindowHeight()
        );

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

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && hoveredCell != null) {
            Crop crop = gameMaster.getWorld().getCropAt(hoveredCell.x, hoveredCell.y);
            if (crop != null) {
                if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                    if (crop.isReadyToHarvest()) {
                        cropService.harvest(gameMaster.getPlayer(), crop);
                    } else {
                        cropService.rip(crop);
                    }
                    gameUIservice.logAction(hoveredCell);
                }
            } else if (selectedItem instanceof Block block) {
                int targetY = 1;
                if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                    Block b = gameMaster.getWorld().getBlockAt(hoveredCell.x, targetY, hoveredCell.y);
                    if (gameMaster.getWorld().removeBlock(b)) {
                        gameMaster.getPlayer().add(b);
                        gameUIservice.logAction(hoveredCell);
                        log.info("Block removed: {}", b.getType().getName());
                    }
                } else {
                    if (cellService.isUnlocked(hoveredCell.x, hoveredCell.y)) {
                        Block newBlock = new Block(block.getType(), hoveredCell.x, targetY, hoveredCell.y);
                        if (gameMaster.getWorld().addBlock(newBlock)) {
                            gameMaster.getPlayer().remove(selectedItem);
                            gameUIservice.logAction(hoveredCell);
                            log.info("Block placed: {} at {},{},{}", newBlock.getType().getName(),
                                    hoveredCell.x, targetY, hoveredCell.y);
                        } else {
                            log.warn("Cannot place block: space occupied at {},{},{}",
                                    hoveredCell.x, targetY, hoveredCell.y);
                        }
                    }
                }

                if (block.getType() == BlockData.DIRT) {
                    log.info("Trying to unlock cell {},{}", hoveredCell.x, hoveredCell.y);
                    if (cellService.expandCell(hoveredCell.x, hoveredCell.y)) {
                        gameMaster.getPlayer().remove(selectedItem);

                        log.info("New cell unlocked at {},{}",
                                hoveredCell.x, hoveredCell.y);
                    } else {
                        log.warn("Could not expand cell {},{}",
                                hoveredCell.x, hoveredCell.y);
                    }
                }
            } else {
                if (selectedItem instanceof Seed &&
                        cellService.isUnlocked(hoveredCell.x, hoveredCell.y)) {
                    if (((Seed) selectedItem).getType() != null) {
                        Crop planted = cropService.plant(
                                hoveredCell.x,
                                hoveredCell.y,
                                gameMaster.getPlayer(),
                                cellService.find(hoveredCell.x, hoveredCell.y),
                                ((Seed) selectedItem).getType(),
                                timeService.getCurrentSeason());
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
