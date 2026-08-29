package com.isofarm.input;

import com.isofarm.data.*;
import com.isofarm.entity.Entity;
import com.isofarm.entity.Player;
import com.isofarm.entity.WorldItem;
import com.isofarm.entity.states.InteractingState;
import com.isofarm.graphics.ParticleEngine;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.graphics.TextureAtlas;
import com.isofarm.gui.GameUIService;
import com.isofarm.item.*;
import com.isofarm.service.CropService;
import com.isofarm.service.TimeService;
import com.isofarm.service.TreeService;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.joml.Math.lerp;
import static org.lwjgl.glfw.GLFW.*;

public class GameInteraction {
    private static final float PICKUP_DISTANCE = 1.5f;
    private static final float TIMEOUT = 0.4f;
    private static final float TIMER_MAX = 5.0f;

    private static final Logger log = LoggerFactory.getLogger(GameInteraction.class);
    private final CropService cropService;
    private final GameUIService gameUIservice;
    private final TimeService timeService;
    private final ParticleEngine particles;
    private final TextureAtlas blocksTexture;

    private int breakingX = Integer.MIN_VALUE;
    private int breakingY = Integer.MIN_VALUE;
    private int breakingZ = Integer.MIN_VALUE;

    private float breakProgress = 0.0f;
    private float breakTimeout = TIMEOUT;
    private float dropTimer = TIMER_MAX;
    private long lastBreakTime = 0L;

    private boolean isSmartShift = false;

    public GameInteraction(GameMaster gameMaster, TextureAtlas blockTexture) {
        this.cropService = gameMaster.getCropService();
        this.gameUIservice = gameMaster.getGameUIService();
        this.timeService = gameMaster.getTimeService();
        this.particles = gameMaster.getParticles();
        this.blocksTexture = blockTexture;
    }

    public BlockPos update(GameMaster gameMaster, Item selectedItem) {
        Player player = gameMaster.getPlayer();
        boolean isCtrlHeld = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL) || Keyboard.isKeyDown(GLFW_KEY_RIGHT_CONTROL);
        boolean isShiftHeld = Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT);
        isSmartShift = isShiftHeld && !gameMaster.isInventoryOpen();
        boolean isLeftHeld = Mouse.isButtonDown(GLFW_MOUSE_BUTTON_LEFT);
        boolean isRightPressed = Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT);
        boolean canInteract = player != null
                && !player.getGamemode().isNoClip()
                && !isCtrlHeld
                && !gameMaster.isInventoryOpen()
                && !gameMaster.isChatOpen();

        if (Keyboard.isKeyPressed(GLFW_KEY_ENTER)) {
            if (!gameMaster.isChatOpen()) {
                gameMaster.setChatOpen(true);
                gameUIservice.openChat();
            } else {
                String command = gameUIservice.getChatText();
                if (command != null && !command.isEmpty()) {
                    gameMaster.getCommandService().execute(command);
                }
                gameMaster.setChatOpen(false);
                gameUIservice.closeChat();
            }
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_F1)) {
            gameMaster.toggleHUD();
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_F3)) {
            Settings.toggleDebugInfo();
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_Q) && canInteract) {
            boolean dropAll = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL);
            dropItem(gameMaster, selectedItem, dropAll);
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_E) && !gameMaster.isChatOpen()) {
            gameMaster.toggleInventory();
        }

        if (!gameMaster.getPlayer().getGamemode().isNoClip()) {
            pickUp(gameMaster);
            dropTimer -= gameMaster.getGenDelta();
            if (dropTimer <= 0.0f) {
                addItem(gameMaster);
                dropTimer = TIMER_MAX;
            }
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_M) && !gameMaster.isChatOpen()) {
            Settings.toggleMusic();
        }

        if (selectedItem instanceof Backpack backpack &&
                isRightPressed && !gameMaster.isInventoryOpen()) {
            if (isCtrlHeld) {
                backpack.unequip(gameMaster);
            } else {
                backpack.use(gameMaster);
            }
            isRightPressed = false;
        }

        if (selectedItem instanceof Bucket bucket) {
            if (isRightPressed && !gameMaster.isInventoryOpen()) {
                bucket.use(gameMaster);
                isRightPressed = false;
            }
        }

        BlockPos hoveredCell = HoveredCell.get(gameMaster, isShiftHeld);
        if (isShiftHeld && hoveredCell != null) {
            byte blockType = gameMaster.getWorld().getBlockTypeAt(hoveredCell);
            if (blockType == BlockData.OAK_LOG.getId()) {
                int bottomY = hoveredCell.y();
                while (bottomY > 0 && gameMaster.getWorld().getBlockTypeAt(
                        hoveredCell.x(), bottomY - 1, hoveredCell.z()) == BlockData.OAK_LOG.getId()) {
                    bottomY--;
                }
                hoveredCell = new BlockPos(getBlockData(blockType), hoveredCell.x(), bottomY,
                        hoveredCell.z());
            }
        }


        if (hoveredCell == null) {
            resetBreaking();
            return null;
        }

        if (!isWithinRange(gameMaster, hoveredCell)) {
            resetBreaking();
            return null;
        }

        breakTimeout -= gameMaster.getGenDelta();
        breakTimeout = Math.max(breakTimeout, 0.0f);

        if (isLeftHeld && canInteract) {
            if (breakTimeout <= 0.0f) {
                breakAction(gameMaster, hoveredCell);
            }
        } else {
            resetBreaking();
        }

        if (isRightPressed && !gameMaster.isInventoryOpen()) {
            placeAction(gameMaster, hoveredCell, selectedItem);
        }
        return hoveredCell;
    }

    private void addItem(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        if (player == null) return;
        Iterator<Entity> iterator = gameMaster.getEntities().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (!(entity instanceof WorldItem worldItem)) continue;
            Item item = worldItem.getItem();
            int amount = worldItem.getAmount();

            if (item == null || amount <= 0) {
                iterator.remove();
                continue;
            }

            if (!player.hasSpace()) {
                player.addToBackpack(item, amount);
            } else {
                player.add(item, amount);
            }

            iterator.remove();
            gameMaster.getSoundService().playEntitySound(SoundGroup.ITEMS);
            log.info("Added x{} {}", amount, item.getName());
        }
    }

    public void dropItem(GameMaster gameMaster, Item selectedItem, boolean dropAll) {
        if (selectedItem == null) return;
        if (selectedItem instanceof Backpack || selectedItem instanceof CraftingKit) {
            ToastFactory.error("You can't drop your " + selectedItem.getName() + "!");
            return;
        }
        Player player = gameMaster.getPlayer();
        if (player == null) return;

        for (InventorySlot slot : player.getInventory().getSlots()) {
            if (slot.isEmpty()) continue;

            Item item = slot.getItem();
            if (item == null) continue;
            if (!item.equals(selectedItem)) continue;
            int amount = dropAll ? slot.getAmount() : 1;
            if (amount <= 0) continue;
            Vector3f playerPosition = player.getPosition();
            Vector3f dropPosition = new Vector3f(playerPosition.x, playerPosition.y + 0.8f, playerPosition.z);
            WorldItem worldItem = new WorldItem(item, amount, dropPosition);

            Vector3f forward = new Vector3f(player.getForward()).normalize();
            Vector3f playerVelocity = new Vector3f(player.getVelocity());
            float inheritedVelocity = 0.35f;
            float throwStrength = 8.0f;
            float verticalStrength = 4.7f;

            Vector3f velocity = new Vector3f(playerVelocity).mul(inheritedVelocity);

            velocity.x += forward.x * throwStrength;
            velocity.z += forward.z * throwStrength;
            velocity.y += verticalStrength;

            worldItem.setVelocity(velocity);
            worldItem.setWorld(gameMaster.getWorld());

            player.remove(item, amount);
            gameMaster.addEntity(worldItem);
            gameMaster.getSoundService().playEntitySound(SoundGroup.ITEMS);

            log.trace("Dropped x{} {} with velocity ({}, {}, {})", amount,
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
            if (!(entity instanceof WorldItem worldItem)) continue;

            if (!worldItem.canBePickedUp()) continue;

            Vector3f playerPos = player.getPosition();
            Vector3f itemPos = worldItem.getPosition();
            float distance = itemPos.distance(playerPos);

            if (distance <= PICKUP_DISTANCE) {
                worldItem.setAttracting(true);
            }

            if (worldItem.isAttracting()) {
                float delta = gameMaster.getGenDelta();
                float lerpFactor = Math.min(1.0f, 10.0f * delta);

                Vector3f targetPos = new Vector3f(playerPos.x,
                        playerPos.y + player.getDimensions().y, playerPos.z);

                itemPos.x = lerp(itemPos.x, targetPos.x, lerpFactor);
                itemPos.y = lerp(itemPos.y, targetPos.y, lerpFactor);
                itemPos.z = lerp(itemPos.z, targetPos.z, lerpFactor);

                if (distance < 0.4f) {
                    Item item = worldItem.getItem();
                    int amount = worldItem.getAmount();

                    if (item != null && amount > 0) {
                        if (!player.hasSpace()) {
                            player.addToBackpack(item, amount);
                        } else {
                            player.add(item, amount);
                        }
                        gameMaster.getSoundService().playEntitySound(SoundGroup.ITEMS);
                        log.info("Picked up x{} {}", amount, item.getName());
                    }
                    iterator.remove();
                }
            }
        }
    }

    private boolean isWithinRange(GameMaster gameMaster, BlockPos cell) {
        float distance = getDistanceToBlock(gameMaster, cell);
        return distance <= Settings.getMaxInteractionDistance();
    }

    public float getDistanceToBlock(GameMaster gameMaster, BlockPos cell) {
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

    private void breakAction(GameMaster gameMaster, BlockPos cell) {
        World world = gameMaster.getWorld();
        int x = cell.x();
        int y = cell.y();
        int z = cell.z();

        Item selectedItem = gameMaster.getGameUIService().getHotbarUI().getSelectedItem();

        if (gameMaster.getPlayer() != null &&
                !(gameMaster.getPlayer().getCurrentState() instanceof InteractingState)) {
            gameMaster.getPlayer().changeState(new InteractingState());
        }

        byte blockId = world.getBlockTypeAt(x, y, z);
        BlockData blockData = getBlockData(blockId);

        Crop crop = world.getCropAt(x, y, z);
        if (crop != null) {
            CropType cropType = crop.getCropType();
            int frameIndex = crop.getStage().getFrameIndex();
            SpriteSheet sheet = gameMaster.getCropSpriteSheet(cropType);
            if (crop.isReadyToHarvest()) {
                cropService.harvest(gameMaster.getPlayer(), crop);
            } else {
                cropService.rip(crop);
            }

            if (sheet != null) {
                particles.spawnCrop(x, y + K.World.SHORTER_BLOCK_HEIGHT, z, sheet, frameIndex);
            }

            gameUIservice.logAction(cell);
            return;
        }

        if (blockId == 0) {
            resetBreaking();
            return;
        }

        if (blockData == null) {
            resetBreaking();
            return;
        }

        if (breakingX != x || breakingY != y || breakingZ != z) {
            breakingX = x;
            breakingY = y;
            breakingZ = z;
            breakProgress = 0.0f;
            lastBreakTime = System.nanoTime();
        }

        Gamemode gamemode = gameMaster.getPlayer().getGamemode();
        if (gamemode.isGodmode()) {
            breakBlock(gameMaster, cell, blockData, blockId, selectedItem);
            resetBreaking();
            return;
        }

        float destroyTime = blockData.getDestroyTime();
        if (selectedItem instanceof Tool tool) {
            BlockData[] usableOn = tool.getType().getUsableOn();
            float[] efficiency = tool.getType().getEfficiency();
            for (int i = 0; i < usableOn.length; i++) {
                if (usableOn[i].getId() == blockId) {
                    if (i < efficiency.length) {
                        destroyTime *= efficiency[i];
                    }
                    break;
                }
            }
        }

        if (destroyTime <= 0.0f) {
            resetBreaking();
            return;
        }

        long now = System.nanoTime();
        float deltaTime = (now - lastBreakTime) / 1_000_000_000.0f;
        lastBreakTime = now;
        breakProgress += deltaTime / destroyTime;

        if (breakProgress >= 1.0f) {
            breakBlock(gameMaster, cell, blockData, blockId, selectedItem);
            resetBreaking();
        }
    }

    private void breakBlock(GameMaster gameMaster, BlockPos cell, BlockData blockData,
                            byte blockId, Item selectedItem) {
        World world = gameMaster.getWorld();
        if (blockData.getSoundGroup() != null) {
            gameMaster.getSoundService().playBreakSound(blockData.getSoundGroup(),
                    getDistanceToBlock(gameMaster, cell), Settings.getMaxInteractionDistance());
        }

        Vector3f position = new Vector3f(cell.x() + 0.5f, cell.y() + 0.5f, cell.z() + 0.5f);
        Block removedBlock = new Block(blockData, cell);
        Item itemToDrop = null;

        if (selectedItem instanceof Tool tool) {
            if (tool instanceof Backpack || tool instanceof CraftingKit) return;
            boolean isUsableOn = Arrays.stream(tool.getType().getUsableOn())
                    .anyMatch(b -> b.getId() == blockId);
            tool.setPlayer(gameMaster.getPlayer());
            if (!isUsableOn) {
                tool.misuse();
            } else {
                tool.use();
            }

            if (tool instanceof Axe axe && isSmartShift) {
                List<BlockPos> destroyedBlocks = TreeService.chop(gameMaster, axe);
                for (BlockPos pos : destroyedBlocks) {
                    gameUIservice.logAction(new BlockPos(blockData, pos.x(), pos.y(), pos.z()));

                    BlockData bData = pos.data();
                    Block brokenBlock = new Block(bData, pos.x(), pos.y(), pos.z());

                    if (bData.hasDrops()) {
                        Object dropObj = bData.getRandomDrop();
                        if (dropObj instanceof MaterialID mid) {
                            itemToDrop = new Material(bData.getTier(), mid);
                        } else if (dropObj instanceof MiningComponent mc) {
                            itemToDrop = mc;
                        } else if (dropObj instanceof Item item) {
                            itemToDrop = item;
                        }
                    }

                    if (itemToDrop == null) {
                        itemToDrop = brokenBlock;
                    }

                    particles.spawnBlock(pos, bData);
                    Vector3f dropPos = new Vector3f(pos.x() + 0.5f, pos.y() + 0.5f, pos.z() + 0.5f);

                    int count = (int) (Math.random() * 2) + 1;
                    WorldItem dropEntity = new WorldItem(itemToDrop, count, dropPos);
                    gameMaster.addEntity(dropEntity);
                }

                log.info("Tree chopped successfully at base {},{},{}", cell.x(), cell.y(), cell.z());
                return;
            }
        }

        world.setBlockTypeAt(cell, BlockData.AIR.getId());
        gameMaster.rebuildChunkMeshAt(cell);
        particles.spawnBlock(HoveredCell.get(gameMaster), blockData);

        if (removedBlock.getType().hasDrops()) {
            Object dropObj = removedBlock.getType().getRandomDrop();
            if (dropObj instanceof MaterialID mid) {
                itemToDrop = new Material(removedBlock.getType().getTier(), mid);
            } else if (dropObj instanceof MiningComponent mc) {
                itemToDrop = mc;
            } else if (dropObj instanceof Item item) {
                itemToDrop = item;
            }
        }

        if (itemToDrop == null) {
            itemToDrop = removedBlock;
        }

        WorldItem dropEntity = new WorldItem(itemToDrop, (int) (Math.random()) + 1, position);
        gameMaster.addEntity(dropEntity);

        gameUIservice.logAction(cell);
        log.info("Block removed: {} at {},{},{}", blockData.getName(), cell.x(), cell.y(), cell.z());
        this.breakTimeout = TIMEOUT;
    }

    private void resetBreaking() {
        breakingX = Integer.MIN_VALUE;
        breakingY = Integer.MIN_VALUE;
        breakingZ = Integer.MIN_VALUE;

        breakProgress = 0.0f;
        lastBreakTime = 0L;
    }

    private void placeAction(GameMaster gameMaster, BlockPos cell,
                             Item selectedItem) {
        World world = gameMaster.getWorld();
        if (gameMaster.getPlayer().checkCollision(world)) return;

        if (gameMaster.getPlayer() != null &&
                !(gameMaster.getPlayer().getCurrentState() instanceof InteractingState)) {
            gameMaster.getPlayer().changeState(new InteractingState());
        }

        int normalX = gameMaster.getOrthoCamera().getLastHitNormalX();
        int normalY = gameMaster.getOrthoCamera().getLastHitNormalY();
        int normalZ = gameMaster.getOrthoCamera().getLastHitNormalZ();

        if (selectedItem instanceof Block block) {
            int placeX = cell.x() + normalX;
            int placeY = cell.y() + normalY;
            int placeZ = cell.z() + normalZ;
            if (gameMaster.getPlayer().intersectsBlock(placeX, placeY, placeZ)) return;

            byte targetBlock = world.getBlockTypeAt(placeX, placeY, placeZ);
            if (targetBlock == 0) {
                Block newBlock = new Block(block.getType(), placeX, placeY, placeZ);
                if (block.getType().equals(BlockData.OAK_BONSAI)) {
                    gameMaster.getTreeService().plant(placeX, placeY, placeZ, BlockData.OAK_BONSAI);
                } else {
                    world.setBlockTypeAt(placeX, placeY, placeZ, block.getType().getId());
                }

                gameMaster.getPlayer().remove(selectedItem);
                gameMaster.getSoundService().playBreakSound(newBlock.getType()
                        .getSoundGroup(), getDistanceToBlock(gameMaster, cell), Settings.getMaxInteractionDistance());
                gameMaster.rebuildChunkMeshAt(placeX, placeZ);
                gameUIservice.logAction(new BlockPos(newBlock.getType(), placeX, placeY, placeZ));
                log.info("Block placed: {} at {},{},{}", newBlock.getType().getName(), placeX, placeY, placeZ);
            }
            return;
        }


        if (selectedItem instanceof Hoe hoe) {
            Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
            if (Arrays.stream(hoe.getType().getUsableOn()).noneMatch(
                    b -> b.getId() == block.getType().getId())) {
                hoe.setPlayer(gameMaster.getPlayer());
                hoe.misuse();
            } else {
                hoe.use(gameMaster, block);
            }
            gameMaster.rebuildChunkMeshAt(block.getX(), block.getZ());
            return;
        }

        if (selectedItem instanceof Seed seed) {
            int x = cell.x();
            int y = cell.y();
            int z = cell.z();

            Crop crop = world.getCropAt(x, y, z);
            byte blockId = world.getBlockTypeAt(x, y, z);

            if (blockId != BlockData.TILLED_DIRT.getId()) {
                log.trace("Cannot plant at {},{},{}: selected block is not TILLED_DIRT", x, y, z);
                return;
            }

            if (crop != null) return;
            if (seed.getType() == null) return;

            Block tilledDirt = new Block(BlockData.TILLED_DIRT, x, y, z);
            Crop planted = cropService.plant(x, y, z, gameMaster.getPlayer(), tilledDirt, seed.getType(),
                    timeService.getCurrentSeason());

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

    public boolean isBreakingBlock() {
        return breakProgress > 0.0f && breakingX != Integer.MIN_VALUE;
    }

    public Vector3i getBreakingBlockPos() {
        return new Vector3i(breakingX, breakingY, breakingZ);
    }

    public float getBreakProgress() {
        return breakProgress;
    }

    public TextureAtlas getBlocksTexture() {
        return blocksTexture;
    }

    public boolean isSmartShiftActive() {
        return isSmartShift;
    }
}