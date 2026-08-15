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
        camera(gameMaster);
        Vector2i hoveredCell = camera.highlight(
                Mouse.getX(),
                Mouse.getY(),
                gameMaster.getWindowWidth(),
                gameMaster.getWindowHeight());

        if (hoveredCell == null) return null;

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            breakAction(gameMaster, hoveredCell);
        }

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
            placeAction(gameMaster, hoveredCell, selectedItem);
        }

        return hoveredCell;
    }

    private void camera(GameMaster gameMaster) {
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
    }

    private void breakAction(GameMaster gameMaster, Vector2i cell) {
        Crop crop = gameMaster.getWorld().getCropAt(cell.x, cell.y);

        if (crop != null) {
            if (crop.isReadyToHarvest()) {
                cropService.harvest(gameMaster.getPlayer(), crop);
            } else {
                cropService.rip(crop);
            }
            gameUIservice.logAction(cell);
            return;
        }

        int targetY = 1;
        Block block = gameMaster.getWorld().getBlockAt(cell.x, targetY, cell.y);
        if (block != null && gameMaster.getWorld().removeBlock(block)) {
            gameMaster.getPlayer().add(block);
            gameUIservice.logAction(cell);
            log.info("Block removed: {}", block.getType().getName());
        }
    }

    private void placeAction(GameMaster gameMaster, Vector2i cell, Item selectedItem) {
        if (selectedItem instanceof Block block) {
            if (block.getType() == BlockData.DIRT) {
                log.info("Trying to unlock cell {},{}", cell.x, cell.y);
                if (cellService.expandCell(cell.x, cell.y)) {
                    gameMaster.getPlayer().remove(selectedItem);
                    log.info("New cell unlocked at {},{}", cell.x, cell.y);
                    return;
                }
            }

            if (cellService.isUnlocked(cell.x, cell.y)) {
                int targetY = 1;
                if (gameMaster.getWorld().getBlockAt(cell.x, targetY, cell.y) == null) {
                    Block newBlock = new Block(block.getType(), cell.x, targetY, cell.y);
                    if (gameMaster.getWorld().addBlock(newBlock)) {
                        gameMaster.getPlayer().remove(selectedItem);
                        gameUIservice.logAction(cell);
                        log.info("Block placed: {} at {},{},{}", newBlock.getType().getName(), cell.x, targetY, cell.y);
                    }
                }
            }
        } else if (selectedItem instanceof Seed seed) {
            Crop crop = gameMaster.getWorld().getCropAt(cell.x, cell.y);
            if (crop == null && cellService.isUnlocked(cell.x, cell.y) && seed.getType() != null) {
                Crop planted = cropService.plant(
                        cell.x,
                        cell.y,
                        gameMaster.getPlayer(),
                        cellService.find(cell.x, cell.y),
                        seed.getType(),
                        timeService.getCurrentSeason());
                if (planted != null) {
                    gameUIservice.logAction(cell);
                }
            }
        }
    }
}