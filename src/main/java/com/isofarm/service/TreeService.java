package com.isofarm.service;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.TreeSapling;
import com.isofarm.item.Axe;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import com.isofarm.wrld.WorldGenerator;

import java.util.*;

public class TreeService {
    private final World world;
    private final List<TreeSapling> saplings = new ArrayList<>();
    private final Random random = new Random();

    public TreeService(World world) {
        this.world = world;
    }

    public void plant(int x, int y, int z, BlockData saplingBlock) {
        world.setBlockTypeAt(x, y, z, saplingBlock.getId());
        saplings.add(new TreeSapling(x, y, z, saplingBlock, (int) Settings.getTicks()));
    }

    public static List<BlockPos> chop(GameMaster gamemaster, Axe axe) {
        List<BlockPos> choppedBlocks = new ArrayList<>();
        BlockPos cell = HoveredCell.get(gamemaster);
        if (cell == null) return choppedBlocks;

        World world = gamemaster.getWorld();
        byte startId = world.getBlockTypeAt(cell.x(), cell.y(), cell.z());
        BlockData startBlock = BlockData.fromId(startId);

        if (!BlockData.OAK_LOG.equals(startBlock)) {
            return choppedBlocks;
        }

        Queue<BlockPos> toProcess = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<ChunkPos> affectedChunks = new HashSet<>();

        BlockPos origin = new BlockPos(startBlock, cell.x(), cell.y(), cell.z());
        toProcess.add(origin);
        visited.add(origin);

        int blocksBroken = 0;
        int maxBlocks = 150;

        while (!toProcess.isEmpty() && blocksBroken < maxBlocks) {
            BlockPos current = toProcess.poll();
            byte currentId = world.getBlockTypeAt(current.x(), current.y(), current.z());
            BlockData currentBlock = BlockData.fromId(currentId);
            boolean isLog = BlockData.OAK_LOG.equals(currentBlock);
            boolean isLeaf = BlockData.OAK_LEAVES.equals(currentBlock);

            if (isLog || isLeaf) {
                world.setBlockTypeAt(current.x(), current.y(), current.z(), BlockData.AIR.getId());
                choppedBlocks.add(current);
                blocksBroken++;
                if (!isLeaf) axe.use();
                affectedChunks.add(new ChunkPos(current.x(), current.z()));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;

                            BlockPos neighbor = new BlockPos(currentBlock,
                                    current.x() + dx,
                                    current.y() + dy,
                                    current.z() + dz);

                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);
                                toProcess.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        if (blocksBroken > 0) {
            gamemaster.getGameUIService().logAction(cell);
            gamemaster.getSoundService().playBreakSound(
                    startBlock.getSoundGroup(), 1.0f, Settings.getMaxInteractionDistance()
            );

            for (ChunkPos chunk : affectedChunks) {
                gamemaster.getWorld().getGameMaster().rebuildChunkMeshAt(chunk.x, chunk.z);
            }
            return choppedBlocks;
        }
        return choppedBlocks;
    }

    public void update() {
        for (int i = saplings.size() - 1; i >= 0; i--) {
            TreeSapling sapling = saplings.get(i);
            if (sapling.tick()) {
                growTree(sapling);
                saplings.remove(i);
            }
        }

        saplings.removeIf(t -> world.getBlockTypeAt(
                t.getX(), t.getY(), t.getZ()) == BlockData.AIR.getId());
    }

    private void growTree(TreeSapling sapling) {
        int x = sapling.getX();
        int y = sapling.getY();
        int z = sapling.getZ();

        world.setBlockTypeAt(x, y, z, BlockData.AIR.getId());
        WorldGenerator.generateTree(x, z, random);
        world.getGameMaster().rebuildChunkMeshAt(x, z);
    }

    private record ChunkPos(int x, int z) {}
}