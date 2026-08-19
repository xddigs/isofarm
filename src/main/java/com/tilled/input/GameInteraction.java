package com.tilled.input;

import com.tilled.data.*;
import com.tilled.entity.Entity;
import com.tilled.entity.Player;
import com.tilled.entity.WorldItem;
import com.tilled.graphics.Camera;
import com.tilled.graphics.ItemRenderer;
import com.tilled.graphics.ParticleEngine;
import com.tilled.graphics.SpriteSheet;
import com.tilled.service.CropService;
import com.tilled.service.GameUIService;
import com.tilled.service.TimeService;
import com.tilled.utils.K;
import com.tilled.wrld.Chunk;
import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.lwjgl.glfw.GLFW.*;

public class GameInteraction {
    public static final float MAX_INTERACTION_DISTANCE = 5.0f;
    private static final float PICKUP_DISTANCE = 1.5f;

    private static final Logger log = LoggerFactory.getLogger(GameInteraction.class);
    private final CropService cropService;
    private final GameUIService gameUIservice;
    private final TimeService timeService;
    private final ParticleEngine particles;
    private final Camera camera;
    private final SpriteSheet blocksTexture;
    private final ItemRenderer itemRenderer;
    private Hit hoveredCell = null;

    public GameInteraction(GameMaster gameMaster,
                           SpriteSheet blocksTexture) {
        this.cropService = gameMaster.getCropService();
        this.gameUIservice = gameMaster.getGameUIService();
        this.timeService = gameMaster.getTimeService();
        this.particles = gameMaster.getParticles();
        this.camera = gameMaster.getCamera();
        this.blocksTexture = blocksTexture;
        this.itemRenderer = gameMaster.getItemRenderer();
    }

    public Hit getHoveredCell() {
        return hoveredCell;
    }

    public Hit update(GameMaster gameMaster, Item selectedItem) {
        if (Keyboard.isKeyPressed(GLFW_KEY_TAB)) {
            gameMaster.setPromptingForInput(true);
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_F1)) {
            gameMaster.toggleHUD();
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_E) && !gameMaster.isPromptingForInput()) {
            gameMaster.toggleInventory();
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_Q)) {
            dropItem(gameMaster, selectedItem);
        }

        pickUp(gameMaster);

        hoveredCell = camera.highlight(gameMaster.getWorld());
        if (hoveredCell == null) return null;
        if (!isWithinRange(gameMaster, hoveredCell)) return null;

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)
                && !gameMaster.isInventoryOpen()) {
            breakAction(gameMaster, hoveredCell);
        }

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)
                && !gameMaster.isInventoryOpen()) {
            placeAction(gameMaster, hoveredCell, selectedItem);
        }

        return hoveredCell;
    }

    public void dropItem(GameMaster gameMaster, Item selectedItem) {
        if (selectedItem == null) return;
        Player player = gameMaster.getPlayer();
        if (player == null) return;

        for (InventorySlot slot : player.getInventory().getSlots()) {
            if (slot.isEmpty()) continue;

            Item item = slot.getItem();
            if (item == null) continue;
            if (!item.equals(selectedItem)) continue;
            int amount = slot.getAmount();
            if (amount <= 0) continue;
            Vector3f playerPosition = player.getPosition();
            Vector3f dropPosition = new Vector3f(playerPosition.x, playerPosition.y + 0.8f, playerPosition.z);
            WorldItem worldItem = new WorldItem(item, amount, dropPosition);

            Vector3f forward = new Vector3f(player.getForward()).normalize();
            Vector3f playerVelocity = new Vector3f(player.getVelocity());
            float inheritedVelocity = 0.35f;
            float throwStrength = 2.5f;
            float verticalStrength = 4.5f;

            Vector3f velocity = new Vector3f(playerVelocity)
                    .mul(inheritedVelocity);

            velocity.x += forward.x * throwStrength;
            velocity.z += forward.z * throwStrength;
            velocity.y += verticalStrength;

            worldItem.setVelocity(velocity);
            worldItem.setWorld(gameMaster.getWorld());

            player.remove(item, amount);
            gameMaster.addEntity(worldItem);
            itemRenderer.playPlaceAnimation();


            log.info("Dropped x{} {} with velocity ({}, {}, {})", amount,
                    item.getName(), velocity.x, velocity.y, velocity.z);

            break;
        }
    }

    private void pickUp(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        if (player == null) return;
        Iterator<Entity> iterator = gameMaster.getEntities().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (!(entity instanceof WorldItem worldItem)) {
                continue;
            }

            if (!worldItem.canBePickedUp()) {
                continue;
            }

            float distance = worldItem.getPosition()
                    .distance(player.getPosition());

            if (distance > PICKUP_DISTANCE) {
                continue;
            }

            Item item = worldItem.getItem();
            int amount = worldItem.getAmount();

            if (item == null || amount <= 0) {
                iterator.remove();
                continue;
            }

            player.add(item, amount);
            iterator.remove();
            log.info("Picked up x{} {}", amount, item.getName());
        }
    }

    private boolean isWithinRange(GameMaster gameMaster, Hit cell) {
        float distance = getDistanceToBlock(gameMaster, cell);
        return distance <= MAX_INTERACTION_DISTANCE;
    }

    public float getDistanceToBlock(GameMaster gameMaster, Hit cell) {
        if (cell == null) return Float.MAX_VALUE;
        if (gameMaster.getPlayer() == null) return Float.MAX_VALUE;

        Vector3f playerPos = gameMaster.getPlayer().getPosition();
        float targetX = cell.x() + 0.5f;
        float targetY = cell.y() + 0.5f;
        float targetZ = cell.z() + 0.5f;

        float dx = playerPos.x - targetX;
        float dy = playerPos.y - targetY;
        float dz = playerPos.z - targetZ;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
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
                cropService.harvest(gameMaster.getPlayer(),crop,
                        gameMaster.getToastService(),sheet);
            } else {
                cropService.rip(crop);
            }

            if (sheet != null) {
                itemRenderer.playBreakAnimation();
                particles.spawn(x, y + K.World.SHORTER_BLOCK_HEIGHT, z, sheet, frameIndex);
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

        if (blockData.getSoundGroup() != null) {
            gameMaster.getSoundService()
                    .playBreakSound(blockData.getSoundGroup(),
                            getDistanceToBlock(gameMaster, cell), MAX_INTERACTION_DISTANCE);
        }

        world.setBlockTypeAt(x, y, z, BlockData.AIR.getId());
        itemRenderer.playBreakAnimation();
        gameMaster.rebuildChunkMeshAt(x, z);

        particles.spawn(x, y, z, blockData, blocksTexture);

        Vector3f position = new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f);
        Block removedBlock = new Block(blockData, x, y, z);
        WorldItem item = new WorldItem(removedBlock, 1, position);
        gameMaster.addEntity(item);
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
            if (gameMaster.getPlayer().intersectsBlock(x, y, z)) return;

            if (y < 0 || y >= Chunk.SIZE_Y) {
                return;
            }

            byte existingBlock = world.getBlockTypeAt(x, y, z);
            if (existingBlock == 0) {
                Block newBlock = new Block(block.getType(), x, y, z);
                world.setBlockTypeAt(x, y, z, block.getType().getId());
                itemRenderer.playPlaceAnimation();
                gameMaster.getSoundService().playBreakSound(newBlock.getType().getSoundGroup(),
                        getDistanceToBlock(gameMaster, cell), MAX_INTERACTION_DISTANCE);

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
                log.debug("Cannot plant at {},{},{}: selected block is not TILLED_DIRT", x, y, z);
                gameMaster.getToastService().error("You can only plant seeds on tilled dirt");
                return;
            }

            if (crop != null) return;
            if (seed.getType() == null) return;

            Block tilledDirt = new Block(BlockData.TILLED_DIRT, x, y, z);
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