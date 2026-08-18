package com.tilled.input;

import com.tilled.data.*;
import com.tilled.graphics.Camera;
import com.tilled.graphics.ParticleEngine;
import com.tilled.graphics.SpriteSheet;
import com.tilled.service.CropService;
import com.tilled.service.GameUIService;
import com.tilled.service.TimeService;
import com.tilled.wrld.Chunk;
import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;

public class GameInteraction {
    private static final Logger log = LoggerFactory.getLogger(GameInteraction.class);
    private static final float MAX_INTERACTION_DISTANCE = 5.0f;
    private final CropService cropService;
    private final GameUIService gameUIservice;
    private final TimeService timeService;
    private final ParticleEngine particles;
    private final Camera camera;
    private final SpriteSheet blocksTexture;

    public GameInteraction(CropService cropService,
                           GameUIService gameUIservice,
                           TimeService timeService,
                           ParticleEngine particles,
                           Camera camera,
                           SpriteSheet blocksTexture) {
        this.cropService = cropService;
        this.gameUIservice = gameUIservice;
        this.timeService = timeService;
        this.particles = particles;
        this.camera = camera;
        this.blocksTexture = blocksTexture;
    }

    public Hit update(GameMaster gameMaster,Item selectedItem) {
        if (Keyboard.isKeyPressed(GLFW_KEY_TAB)) {
            gameMaster.setPromptingForInput(true);
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_F1)) {
            gameMaster.toggleHUD();
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_E) && !gameMaster.isPromptingForInput()) {
            gameMaster.toggleInventory();
        }

        Hit hoveredCell = camera.highlight(gameMaster.getWorld());

        if (hoveredCell == null) return null;
        if (!isWithinRange(gameMaster, hoveredCell)) return null;

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)
                && !gameMaster.isInventoryOpen()) {
            breakAction(gameMaster,hoveredCell);
        }

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)
                && !gameMaster.isInventoryOpen()) {
            placeAction(gameMaster,hoveredCell,selectedItem);
        }

        return hoveredCell;
    }

    private boolean isWithinRange(GameMaster gameMaster, Hit cell) {
        if (gameMaster.getPlayer() == null) {
            return false;
        }

        Vector3f playerPos = gameMaster.getPlayer().getPosition();
        float targetX = cell.x() + 0.5f;
        float targetY = cell.y() + 0.5f;
        float targetZ = cell.z() + 0.5f;
        float dx = playerPos.x - targetX;
        float dy = playerPos.y - targetY;
        float dz = playerPos.z - targetZ;
        float distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared <= (MAX_INTERACTION_DISTANCE * MAX_INTERACTION_DISTANCE);
    }

    private void breakAction(GameMaster gameMaster, Hit cell) {
        World world = gameMaster.getWorld();
        int x = cell.x();
        int y = cell.y();
        int z = cell.z();

        Crop crop = world.getCropAt(x, y, z);
        if (crop != null) {
            CropType cropType = crop.getCropType();
            int frameIndex = crop.getStage().getFrameIndex();
            SpriteSheet sheet = gameMaster.getCropSpriteSheet(cropType);

            if (crop.isReadyToHarvest()) {
                cropService.harvest(
                        gameMaster.getPlayer(),
                        crop,
                        gameMaster.getToastService(),
                        sheet
                );
            } else {
                cropService.rip(crop);
            }

            if (sheet != null) {
                particles.spawn(x, y, z, sheet, frameIndex);
            }

            gameUIservice.logAction(cell);
            return;
        }

        byte blockId = world.getBlockTypeAt(x, y, z);
        if (blockId == 0) {
            return;
        }

        BlockData blockData = getBlockData(blockId);
        if (blockData == null) return;

        if (blockData.getStepSoundGroup() != null) {
            gameMaster.getSoundService()
                    .playBreakSound(blockData.getStepSoundGroup());
        }

        world.setBlockTypeAt(x, y, z, (byte) 0);
        gameMaster.rebuildChunkMeshAt(x, z);

        particles.spawn(x, y, z, blockData, blocksTexture);

        Block removedBlock = new Block(blockData, x, y, z);
        gameMaster.getPlayer().add(removedBlock);

        gameUIservice.logAction(cell);
        log.info("Block removed: {} at {},{},{}", blockData.getName(), x, y, z);
    }

    private void placeAction(GameMaster gameMaster, Hit cell, Item selectedItem) {
        World world = gameMaster.getWorld();
        if (gameMaster.getPlayer().checkCollision(world)) return;

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
                gameMaster.getSoundService().playBreakSound(SoundGroup.GRASS);
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
            hoe.use(gameMaster, block);
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
                gameMaster.getToastService().error("You can only plant seeds on tilled dirt");
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