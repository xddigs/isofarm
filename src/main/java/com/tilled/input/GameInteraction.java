package com.tilled.input;

import com.tilled.data.*;
import com.tilled.graphics.Camera;
import com.tilled.graphics.ParticleEngine;
import com.tilled.graphics.SpriteSheet;
import com.tilled.service.CropService;
import com.tilled.service.GameUIService;
import com.tilled.service.TimeService;
import com.tilled.utils.K;
import com.tilled.wrld.Chunk;
import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;

public class GameInteraction {
    private static final Logger log = LoggerFactory.getLogger(GameInteraction.class);
    private final CropService cropService;
    private final GameUIService gameUIservice;
    private final TimeService timeService;
    private final ParticleEngine particles;
    private final Camera camera;

    public GameInteraction(CropService cropService, GameUIService gameUIservice,
                           TimeService timeService, ParticleEngine particles, Camera camera) {
        this.cropService = cropService;
        this.gameUIservice = gameUIservice;
        this.timeService = timeService;
        this.particles = particles;
        this.camera = camera;
    }

    public Hit update(GameMaster gameMaster, Item selectedItem) {
        camera(gameMaster);

        if (Keyboard.isKeyPressed(GLFW_KEY_TAB)) {
            gameMaster.setPromptingForInput(true);
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_F1)) {
            gameMaster.toggleHUD();
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_E) && !gameMaster.isPromptingForInput()) {
            gameMaster.toggleInventory();
        }

        Hit hoveredCell = camera.highlight(Mouse.getX(), Mouse.getY(),
                gameMaster.getWindowWidth(), gameMaster.getWindowHeight(),
                gameMaster.getWorld());

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

        if (Mouse.isButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (isCtrlDown) {
                camera.rotateYaw(Mouse.getDeltaX() * K.Camera.ROTATION_SENSITIVITY);
            } else {
                camera.pan(Mouse.getDeltaX(), Mouse.getDeltaY(), K.Camera.PAN_SENSITIVITY);
            }
        }

        float scrollY = Mouse.getScrollY();
        if (scrollY != 0.0f) {
            if (isCtrlDown) {
                camera.zoom(scrollY);
            } else {
                gameUIservice.selectItem(scrollY > 0 ? -1 : 1);
            }
        }

        if (Mouse.isButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
            gameMaster.recenter();
        }
    }

    private void breakAction(GameMaster gameMaster, Hit cell) {
        World world = gameMaster.getWorld();
        int x = cell.x();
        int y = cell.y();
        int z = cell.z();

        Crop crop = world.getCropAt(x, y, z);
        if (crop != null) {

            if (crop.isReadyToHarvest()) {
                cropService.harvest(gameMaster.getPlayer(), crop,
                        gameMaster.getToastService());
            } else {
                cropService.rip(crop);
            }

            SpriteSheet sheet = gameMaster.getCropSpriteSheet(crop.getCropType());
            particles.spawn(x, K.World.CROP_Y_OFFSET, z, sheet, crop.getStage().getFrameIndex());
            gameUIservice.logAction(cell);
            return;
        }

        byte blockId = world.getBlockTypeAt(x, y, z);
        if (blockId == 0) {
            return;
        }

        BlockData blockData = getBlockData(blockId);
        if (blockData == null) return;

        world.setBlockTypeAt(x, y, z, (byte) 0);
        gameMaster.rebuildChunkMeshAt(x, z);
        particles.spawn(x, y, z, blockData);
        Block removedBlock = new Block(blockData, x, y, z);
        gameMaster.getPlayer().add(removedBlock);
        gameUIservice.logAction(cell);
        log.info("Block removed: {} at {},{},{}", blockData.getName(), x, y, z);
    }

    private void placeAction(GameMaster gameMaster, Hit cell, Item selectedItem) {
        World world = gameMaster.getWorld();

        if (selectedItem instanceof Block block) {
            int x = cell.x() + cell.normalX();
            int y = cell.y() + cell.normalY();
            int z = cell.z() + cell.normalZ();

            if (y < 0 || y >= Chunk.SIZE_Y) {
                return;
            }

            byte existingBlock = world.getBlockTypeAt(x, y, z);
            if (existingBlock == 0) {
                Block newBlock = new Block(block.getType(), x, y, z);
                world.setBlockTypeAt(x, y, z, block.getType().getId());
                gameMaster.getPlayer().remove(selectedItem);
                gameMaster.rebuildChunkMeshAt(x, z);
                gameUIservice.logAction(new Hit(x, y, z, cell.normalX(), cell.normalY(), cell.normalZ()));
                log.info("Block placed: {} at {},{},{}", newBlock.getType().getName(), x, y, z);
            }

            return;
        }

        if (selectedItem instanceof WateringCan wateringCan) {
            wateringCan.use(world);
            gameMaster.getToastService().success("You water the crops!");
            return;
        }

        if (selectedItem instanceof Hoe hoe) {
            Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
            hoe.use(world, block);
            gameMaster.rebuildChunkMeshAt(block.getX(), block.getZ());
        }

        if (selectedItem instanceof Seed seed) {
            int x = cell.x();
            int y = cell.y();
            int z = cell.z();

            Crop crop = world.getCropAt(x, y, z);
            byte blockId = world.getBlockTypeAt(x, y, z);

            if (blockId != BlockData.TILLED_DIRT.getId()) {
                log.debug("Cannot plant at {},{},{}: selected block is not TILLED_DIRT", x, y, z );
                return;
            }

            if (crop != null) return;
            if (seed.getType() == null) return;

            Block tilledDirt = new Block(BlockData.TILLED_DIRT,x,y,z);
            Crop planted = cropService.plant(x, y, z, gameMaster.getPlayer(), tilledDirt,
                    seed.getType(), timeService.getCurrentSeason(), gameMaster.getToastService());

            if (planted != null) {
                gameUIservice.logAction(cell);
                log.info("Planted {} at {},{},{}", seed.getType().getName(), x, y, z);
            }
        }
    }

    private BlockData getBlockData(byte blockId) {
        for (BlockData data : BlockData.values()) {
            if (data.getId() == blockId) {
                return data;
            }
        }

        return null;
    }
}