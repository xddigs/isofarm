package com.isofarm.service;

import com.isofarm.data.BlockData;
import com.isofarm.data.Hit;
import com.isofarm.entity.WorldItem;
import com.isofarm.graphics.ParticleEngine;
import com.isofarm.item.Block;
import com.isofarm.item.Tool;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public final class Woodcutter {
    private static final int MAX_BLOCKS_TO_BREAK = 300;

    private Woodcutter() {}

    public static boolean chop(GameMaster gameMaster, Tool axe, ParticleEngine particles,
                               int startX, int startY, int startZ) {
        World world = gameMaster.getWorld();
        byte initialBlockId = world.getBlockTypeAt(startX, startY, startZ);
        if (!isTreeBlock(initialBlockId)) {
            return false;
        }

        Queue<Vector3i> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        Vector3i origin = new Vector3i(startX, startY, startZ);
        queue.add(origin);
        visited.add(startX + "," + startY + "," + startZ);

        int brokenCount = 0;

        while (!queue.isEmpty() && brokenCount < MAX_BLOCKS_TO_BREAK) {
            Vector3i current = queue.poll();
            int x = current.x;
            int y = current.y;
            int z = current.z;

            byte blockId = world.getBlockTypeAt(x, y, z);
            if (!isTreeBlock(blockId)) {
                continue;
            }

            BlockData data = getBlockData(blockId);
            Hit hit = new Hit(x, y, z, 0, 1, 0);

            world.setBlockTypeAt(hit, BlockData.AIR.getId());

            gameMaster.rebuildChunkMeshAt(x, z);
            particles.spawn(hit, data);

            // 3. Spawn Drop
            Vector3f pos = new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f);
            WorldItem dropEntity = new WorldItem(new Block(data, hit), 1, pos);
            gameMaster.addEntity(dropEntity);

            brokenCount++;
            axe.use();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        addNeighbor(queue, visited, world, x + dx, y + dy, z + dz);
                    }
                }
            }
        }

        return brokenCount > 0;
    }

    private static void addNeighbor(Queue<Vector3i> queue, Set<String> visited, World world, int x, int y, int z) {
        String key = x + "," + y + "," + z;
        if (!visited.add(key)) {
            return;
        }

        byte blockId = world.getBlockTypeAt(x, y, z);
        if (isTreeBlock(blockId)) {
            queue.add(new Vector3i(x, y, z));
        }
    }

    private static boolean isTreeBlock(byte blockId) {
        return blockId == BlockData.OAK_LOG.getId()
                || blockId == BlockData.OAK_LEAVES.getId();
    }

    private static BlockData getBlockData(byte blockId) {
        for (BlockData data : BlockData.values()) {
            if (data.getId() == blockId) {
                return data;
            }
        }
        return BlockData.OAK_LOG;
    }
}