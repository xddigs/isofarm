package com.tilled.input;

import com.tilled.data.*;
import com.tilled.graphics.Camera;
import com.tilled.graphics.ParticleEngine;
import com.tilled.graphics.SpriteSheet;
import com.tilled.service.*;
import com.tilled.utils.K;
import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;

public class GameInteraction {
    private static final Logger log = LoggerFactory.getLogger(GameInteraction.class);
    private final CropService cropService;
    private final GameUIService gameUIservice;
    private final BlockService blockService;
    private final TimeService timeService;
    private final ParticleEngine particles;
    private final Camera camera;

    public GameInteraction(CropService cropService,
                           GameUIService gameUIservice,
                           BlockService blockService,
                           TimeService timeService,
                           ParticleEngine particles, Camera camera) {
        this.cropService = cropService;
        this.gameUIservice = gameUIservice;
        this.blockService = blockService;
        this.timeService = timeService;
        this.particles = particles;
        this.camera = camera;
    }

    public Vector2i update(GameMaster gameMaster, Item selectedItem) {
        camera(gameMaster);

        if (Keyboard.isKeyPressed(GLFW_KEY_TAB)) {
            gameMaster.setPromptingForInput(true);
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_E)) {
            gameMaster.toggleInventory();
        }

        Vector2i hoveredCell = camera.highlight(
                Mouse.getX(),
                Mouse.getY(),
                gameMaster.getWindowWidth(),
                gameMaster.getWindowHeight());

        if (hoveredCell == null) {
            return null;
        }

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
        int x = cell.x();
        int z = cell.y();

        Crop crop = gameMaster.getWorld().getCropAt(x, z);
        if (crop != null) {
            if (crop.isReadyToHarvest()) {
                cropService.harvest(gameMaster.getPlayer(), crop);
            } else {
                cropService.rip(crop);
            }

            SpriteSheet sheet = gameMaster.getCropSpriteSheet(crop.getType());
            particles.spawn(x, K.World.CROP_ELEVATION_Y, z, sheet,
                    crop.getStage().getFrameIndex());

            gameUIservice.logAction(cell);
            return;
        }

        int topY = getTopBlockY(gameMaster.getWorld(), x, z);
        if (topY >= 1) {
            Block block = gameMaster.getWorld().getBlockAt(x, topY, z);
            if (block != null && gameMaster.getWorld().removeBlock(block)) {
                particles.spawn(x, K.World.CROP_ELEVATION_Y, z, block.getType());
                gameMaster.getPlayer().add(block);
                gameUIservice.logAction(cell);

                log.info("Block removed: {} at {},{},{}", block.getType().getName(),
                        x, topY, z);
            }
            return;
        }

        Block baseBlock = blockService.find(x, z);
        if (baseBlock != null && baseBlock.getType() == BlockData.TILLED_DIRT) {

            if (!baseBlock.isUnlocked()) {
                return;
            }

            Block removed = blockService.removeBlock(x, z);
            if (removed != null) {
                gameMaster.getPlayer().add(removed);
                gameUIservice.logAction(cell);
                log.info("TILLED_DIRT removed at {},{}", x, z);
            }
        }
    }

    private void placeAction(GameMaster gameMaster, Vector2i cell, Item selectedItem) {
        if (selectedItem instanceof Block block) {
            if (block.getType() == BlockData.TILLED_DIRT) {
                if (blockService.expandBlock(cell.x(), cell.y())) {
                    gameMaster.getPlayer().remove(selectedItem);
                    gameUIservice.logAction(cell);
                    log.info("New TILLED_DIRT placed at {},{}", cell.x(), cell.y());
                    gameMaster.getToastService().success("A new expansion has been created!");
                }
                return;
            }

            if (!blockService.isUnlocked(cell.x(), cell.y())) {
                return;
            }

            int targetY = getFirstFreeY(gameMaster.getWorld(), cell.x, cell.y);
            Block newBlock = new Block(block.getType(), cell.x, targetY, cell.y);
            if (gameMaster.getWorld().addBlock(newBlock)) {
                gameMaster.getPlayer().remove(selectedItem);
                gameUIservice.logAction(cell);
                log.info("Block placed: {} at {},{},{}",
                        newBlock.getType().getName(), cell.x, targetY, cell.y);
            }
        } else if (selectedItem instanceof WateringCan wateringCan) {
            wateringCan.use(gameMaster.getWorld());
            gameMaster.getToastService().success("You water the crops!");

        } else if (selectedItem instanceof Seed seed) {
            Crop crop = gameMaster.getWorld().getCropAt(cell.x, cell.y);
            Block baseBlock = blockService.find(cell.x, cell.y);

            if (crop == null && blockService.isUnlocked(cell.x, cell.y) && seed.getType() != null) {
                Crop planted = cropService.plant(
                        cell.x,
                        cell.y,
                        gameMaster.getPlayer(),
                        baseBlock,
                        seed.getType(),
                        timeService.getCurrentSeason());
                if (planted != null) {
                    gameUIservice.logAction(cell);
                }
            }
        }
    }

    private int getFirstFreeY(World world, int x, int z) {
        int y = 1;
        while (world.getBlockAt(x, y, z) != null) y++;
        return y;
    }

    private int getTopBlockY(World world, int x, int z) {
        int y = 1;
        if (world.getBlockAt(x, y, z) == null) return -1;
        while (world.getBlockAt(x, y + 1, z) != null) y++;
        return y;
    }
}