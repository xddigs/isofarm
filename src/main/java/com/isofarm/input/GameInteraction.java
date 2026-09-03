package com.isofarm.input;

import com.isofarm.data.*;
import com.isofarm.entity.Entity;
import com.isofarm.entity.Player;
import com.isofarm.entity.WorldItem;
import com.isofarm.graphics.ParticleEngine;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.item.*;
import com.isofarm.service.*;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.WaterSimulation;
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

@Singleton
@GodObject
public class GameInteraction {
    public static final GameInteraction gami = new GameInteraction();
    private static final float PICKUP_DISTANCE = 1.5f;
    private static final float TIMEOUT = 0.4f;
    private static final float TIMER_MAX = 5.0f;

    private static final Logger log = LoggerFactory.getLogger(GameInteraction.class);

    private int breakingX = Integer.MIN_VALUE;
    private int breakingY = Integer.MIN_VALUE;
    private int breakingZ = Integer.MIN_VALUE;

    private float breakProgress = 0.0f;
    private float breakTimeout = TIMEOUT;
    private float dropTimer = TIMER_MAX;
    private long lastBreakTime = 0L;

    private boolean isSmartShift = false;
    
    private GameInteraction() {}
    
    public BlockPos update(GameMaster gameMaster, Item selectedItem) {
        Player player = GameMaster.game.getPlayer();
        Inventory inventory = player.getInventory();
        boolean isCtrlHeld = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL) ||
                Keyboard.isKeyDown(GLFW_KEY_RIGHT_CONTROL);

        boolean isShiftHeld = Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT);
        isSmartShift = isShiftHeld && !GameMaster.game.isInventoryOpen();

        boolean isLeftHeld = Mouse.isButtonDown(GLFW_MOUSE_BUTTON_LEFT);
        boolean isLeftPressed = Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT);
        boolean isRightPressed = Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT);
        boolean canInteract = player != null
                && !player.getGamemode().isNoClip()
                && !GameMaster.game.isInventoryOpen()
                && !GameMaster.game.isChatOpen();

        if (Keyboard.isKeyPressed(GLFW_KEY_ENTER)) {
            if (!GameMaster.game.isChatOpen()) {
                GameMaster.game.setChatOpen(true);
                GameMaster.game.getGameUIService().openChat();
            } else {
                String command = GameMaster.game.getGameUIService().getChatText();
                if (command != null && !command.isEmpty()) {
                    GameMaster.game.getCommandService().execute(command);
                }
                GameMaster.game.setChatOpen(false);
                GameMaster.game.getGameUIService().closeChat();
            }
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_F1)) {
            GameMaster.game.toggleHUD();
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_F3)) {
            Settings.toggleDebugInfo();
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_Q) && canInteract) {
            boolean dropAll = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL);
            dropItem(selectedItem, dropAll);
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_E) && !GameMaster.game.isChatOpen() &&
                !BookService.bs.isOpen()) {
            GameMaster.game.toggleInventory();
        }

        if (inventory.hasBookEquipped() && Keyboard.isKeyPressed(GLFW_KEY_TAB)
                && !GameMaster.game.isChatOpen()) {
            CraftingBook book = inventory.getBook();
            if (book != null) {
                if (!BookService.bs.isOpen()) {
                    BookService.bs.open(book);
                } else {
                    BookService.bs.close();
                }
            }
        } else if (Keyboard.isKeyPressed(GLFW_KEY_E) && BookService.bs.isOpen() &&
                !inventory.hasBookEquipped()) {
            BookService.bs.close();
        }

        if (!GameMaster.game.getPlayer().getGamemode().isNoClip()) {
            pickUp();
            dropTimer -= GameMaster.game.getGenDelta();
            if (dropTimer <= 0.0f) {
                addItem();
                dropTimer = TIMER_MAX;
            }
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_M) && !GameMaster.game.isChatOpen()) {
            Settings.toggleMusic();
        }

        if (isLeftPressed && canInteract) {
            if (!player.isAttacking()) {
                player.interact();
            }
        }

        if (selectedItem instanceof Usable usable) {
            switch (usable) {
                case Backpack backpack -> {
                    if (isRightPressed && !GameMaster.game.isInventoryOpen()) {
                        if (isCtrlHeld) {
                            backpack.unequip();
                        } else {
                            backpack.use(gameMaster, isCtrlHeld);
                        }
                        isRightPressed = false;
                    }
                }

                case CraftingBook book -> {
                    if (isRightPressed && !BookService.bs.isOpen()) {
                        book.use(gameMaster, isCtrlHeld);
                        isRightPressed = false;
                    }
                }

                case Bucket bucket -> {
                    if (isRightPressed && !GameMaster.game.isInventoryOpen()) {
                        bucket.use(gameMaster, isCtrlHeld);
                        isRightPressed = false;
                    }
                }
                default -> throw new IllegalStateException(
                        "Unexpected value: " + usable);
            }
        }

        BlockPos hoveredCell = HoveredCell.get(gameMaster, isShiftHeld);
        if (isShiftHeld && hoveredCell != null) {
            byte blockType = GameMaster.game.getWorld().getBlockTypeAt(hoveredCell);
            if (blockType == BlockData.OAK_LOG.getId()) {
                int bottomY = hoveredCell.y();
                while (bottomY > 0 && GameMaster.game.getWorld().getBlockTypeAt(
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

        if (!isWithinRange(hoveredCell)) {
            resetBreaking();
            return null;
        }

        breakTimeout -= GameMaster.game.getGenDelta();
        breakTimeout = Math.max(breakTimeout, 0.0f);

        if (isLeftHeld && canInteract) {
            if (breakTimeout <= 0.0f) {
                breaking(gameMaster, hoveredCell);
            }
        } else {
            resetBreaking();
        }

        if (isRightPressed && !GameMaster.game.isInventoryOpen()) {
            if (player != null && !player.isAttacking()) {
                player.interact();
            }
            place(gameMaster, hoveredCell, selectedItem);
        }
        return hoveredCell;
    }

    public void addItem() {
        Player player = GameMaster.game.getPlayer();
        if (player == null) return;
        Iterator<Entity> iterator = GameMaster.game.getEntities().iterator();
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
            SoundService.fx.playEntitySound(SoundGroup.ITEMS);
            log.info("Added x{} {}", amount, item.getName());
        }
    }

    public void dropItem(Item selectedItem, boolean dropAll) {
        if (selectedItem == null) return;
        if (selectedItem instanceof Undroppable) {
            ToastFactory.error("toast.item_undroppable");
            return;
        }
        Player player = GameMaster.game.getPlayer();
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
            worldItem.setWorld(GameMaster.game.getWorld());

            player.remove(item, amount);
            GameMaster.game.addEntity(worldItem);
            SoundService.fx.playEntitySound(SoundGroup.ITEMS);

            log.trace("Dropped x{} {} with velocity ({}, {}, {})", amount,
                    item.getName(), velocity.x, velocity.y, velocity.z);
            break;
        }
    }

    private void pickUp() {
        Player player = GameMaster.game.getPlayer();
        if (player == null) return;

        Iterator<Entity> iterator = GameMaster.game.getEntities().iterator();
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
                float delta = GameMaster.game.getGenDelta();
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
                        SoundService.fx.playEntitySound(SoundGroup.ITEMS);
                        log.info("Picked up x{} {}", amount, item.getName());
                    }
                    iterator.remove();
                }
            }
        }
    }

    private boolean isWithinRange(BlockPos cell) {
        float distance = getDistanceToBlock(cell);
        return distance <= Settings.getMaxInteractionDistance();
    }

    public float getDistanceToBlock(BlockPos cell) {
        if (cell == null) return Float.MAX_VALUE;
        if (GameMaster.game.getPlayer() == null) return Float.MAX_VALUE;

        Vector3f playerPos = GameMaster.game.getPlayer().getPosition();
        float targetX = cell.x() + 0.5f;
        float targetY = cell.y() + 0.5f;
        float targetZ = cell.z() + 0.5f;

        float dx = playerPos.x - targetX;
        float dy = playerPos.y - targetY;
        float dz = playerPos.z - targetZ;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void breaking(GameMaster gameMaster, BlockPos cell) {
        if (BookService.bs.isOpen()) return;
        World world = GameMaster.game.getWorld();
        int x = cell.x();
        int y = cell.y();
        int z = cell.z();

        Item selectedItem = Settings.selectedItem;
        if (GameMaster.game.getPlayer() != null) {
            if (!GameMaster.game.getPlayer().isAttacking()) {
                GameMaster.game.getPlayer().interact();
            }
        }

        byte blockId = world.getBlockTypeAt(x, y, z);
        BlockData blockData = getBlockData(blockId);

        Crop crop = world.getCropAt(x, y, z);
        if (crop != null) {
            CropType cropType = crop.getCropType();
            int frameIndex = crop.getStage().getFrameIndex();
            SpriteSheet sheet = GameMaster.game.getCropSpriteSheet(cropType);
            if (crop.isReadyToHarvest()) {
                CropService.cs.harvest(GameMaster.game.getPlayer(), crop);
            } else {
                CropService.cs.rip(crop);
            }

            SoundService.fx.playBreakSound(SoundGroup.SOIL);
            if (sheet != null) ParticleEngine.peng.spawnCrop(x, y + K.World.SHORTER_BLOCK_HEIGHT,
                    z, sheet, frameIndex);
            GameMaster.game.getGameUIService().logAction(cell);
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

        Gamemode gamemode = GameMaster.game.getPlayer().getGamemode();
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
        World world = GameMaster.game.getWorld();
        if (blockData.getSoundGroup() != null) {
            SoundService.fx.playBreakSound(blockData.getSoundGroup());
        }

        Vector3f position = new Vector3f(cell.x() + 0.5f, cell.y() + 0.5f, cell.z() + 0.5f);
        Block removedBlock = new Block(blockData, cell);
        Item itemToDrop = null;

        if (selectedItem instanceof Tool tool) {
            boolean isUsableOn = Arrays.stream(tool.getType().getUsableOn())
                    .anyMatch(b -> b.getId() == blockId);

            tool.setPlayer(GameMaster.game.getPlayer());
            if (!isUsableOn) {
                tool.misuse();
            } else {
                tool.use();
            }

            if (tool instanceof Axe axe && isSmartShift) {
                List<BlockPos> destroyedBlocks = TreeService.chop(gameMaster, axe);
                for (BlockPos pos : destroyedBlocks) {
                    GameMaster.game.getGameUIService().logAction(new BlockPos(blockData, pos.x(), pos.y(), pos.z()));
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

                    ParticleEngine.peng.spawnBlock(pos, bData);
                    Vector3f dropPos = new Vector3f(pos.x() + 0.5f, pos.y() + 0.5f, pos.z() + 0.5f);

                    int count = (int) (Math.random() * 2) + 1;
                    WorldItem dropEntity = new WorldItem(itemToDrop, count, dropPos);
                    GameMaster.game.addEntity(dropEntity);
                }

                log.info("Tree chopped successfully at base {},{},{}", cell.x(), cell.y(), cell.z());
                return;
            }
        }

        world.setBlockTypeAt(cell, BlockData.AIR.getId());
        world.setWaterLevelAt(cell.x(), cell.y(), cell.z(), (byte) 0);
        breakAbove(cell.x(), cell.y(), cell.z());
        WaterSimulation.ws.onBlockDestroyed(cell.x(), cell.y(), cell.z());
        GameMaster.game.rebuildChunkMeshAt(cell);
        ParticleEngine.peng.spawnBlock(cell, blockData);

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
        GameMaster.game.addEntity(dropEntity);

        GameMaster.game.getGameUIService().logAction(cell);
        log.trace("Block removed: {} at {},{},{}", blockData.getDisplayName(), cell.x(), cell.y(), cell.z());
        this.breakTimeout = TIMEOUT;
    }

    private void resetBreaking() {
        breakingX = Integer.MIN_VALUE;
        breakingY = Integer.MIN_VALUE;
        breakingZ = Integer.MIN_VALUE;

        breakProgress = 0.0f;
        lastBreakTime = 0L;
    }

    private void place(GameMaster gameMaster, BlockPos cell,
                       Item selectedItem) {
        World world = GameMaster.game.getWorld();
        Player player = GameMaster.game.getPlayer();

        if (BookService.bs.isOpen()) return;
        if (player.checkCollision(world)) return;

        if (player != null) {
            player.interact();
        }

        int normalX = GameMaster.game.getOrthoCamera().getLastHitNormalX();
        int normalY = GameMaster.game.getOrthoCamera().getLastHitNormalY();
        int normalZ = GameMaster.game.getOrthoCamera().getLastHitNormalZ();

        if (selectedItem instanceof Block block) {
            int placeX = cell.x() + normalX;
            int placeY = cell.y() + normalY;
            int placeZ = cell.z() + normalZ;

            if (player.intersectsBlock(placeX, placeY, placeZ)) return;

            byte targetBlock = world.getBlockTypeAt(placeX, placeY, placeZ);
            BlockData target = BlockData.fromId(targetBlock);

            if (target == null || !target.equals(BlockData.AIR)) {
                return;
            }

            Block newBlock = new Block(block.getType(), placeX, placeY, placeZ);

            if (block.getType().equals(BlockData.OAK_BONSAI)) {
                TreeService.ts.plant(placeX, placeY, placeZ, BlockData.OAK_BONSAI);
            } else {
                world.setBlockTypeAt(placeX, placeY, placeZ, block.getType().getId());
            }

            player.remove(selectedItem);
            SoundService.fx.playBreakSound(newBlock.getType().getSoundGroup());
            GameMaster.game.rebuildChunkMeshAt(placeX, placeZ);
            GameMaster.game.getGameUIService().logAction(
                    new BlockPos(newBlock.getType(), placeX, placeY, placeZ));
            log.trace("Block placed: {} at {},{},{}",
                    newBlock.getType().getName(), placeX, placeY, placeZ);
            return;
        }

        if (selectedItem instanceof Hoe hoe) {
            Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
            if (Arrays.stream(hoe.getType().getUsableOn()).noneMatch(
                    b -> b.getId() == block.getType().getId())) {
                hoe.setPlayer(player);
                hoe.misuse();
            } else {
                hoe.use(gameMaster, block);
            }
            GameMaster.game.rebuildChunkMeshAt(block.getX(), block.getZ());
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
            Crop planted = CropService.cs.plant(x, y, z, player, tilledDirt, seed.getType(),
                    TimeService.ts.getCurrentSeason());

            if (planted != null) {
                GameMaster.game.getGameUIService().logAction(cell);
                log.info("Planted {} at {},{},{}", seed.getType().getName(), x, y, z);
            }
        }
    }

    private void breakAbove(int x, int y, int z) {
        int aboveY = y + 1;
        if (aboveY >= Chunk.SIZE_Y) {
            return;
        }

        Crop crop = World.wrld.getCropAt(x, aboveY, z);
        if (crop != null) {
            CropService.cs.rip(crop);
            World.wrld.removeBlockAt(x, aboveY, z);
            World.wrld.setBlockTypeAt(x, aboveY, z, BlockData.AIR.getId());
            World.wrld.setWaterLevelAt(x, aboveY, z, (byte) 0);
            GameMaster.game.rebuildChunkMeshAt(x, z);
            return;
        }

        byte aboveBlockId = World.wrld.getBlockTypeAt(x, aboveY, z);
        BlockData aboveBlock = BlockData.fromId(aboveBlockId);

        if (aboveBlock == null || !aboveBlock.isPlant()) {
            return;
        }

        World.wrld.removeBlockAt(x, aboveY, z);
        World.wrld.setBlockTypeAt(x, aboveY, z, BlockData.AIR.getId());
        World.wrld.setWaterLevelAt(x, aboveY, z, (byte) 0);
        ParticleEngine.peng.spawnBlock(new BlockPos(aboveBlock, x, aboveY, z), aboveBlock);
        GameMaster.game.rebuildChunkMeshAt(x, z);
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

    public boolean isSmartShiftActive() {
        return isSmartShift;
    }
}