package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.FluidPos;
import com.isofarm.data.Singleton;
import com.isofarm.graphics.ParticleEngine;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

@SuppressWarnings("all")
@Singleton
public class WaterSimulation {
    public static final WaterSimulation ws = new WaterSimulation();

    private static final byte MAX_LEVEL = 8;
    private static final byte MIN_LEVEL = 1;
    private static final float STEP_TIME = 0.25f;

    private final Queue<FluidPos> queue = new ArrayDeque<>();
    private final Set<FluidPos> queued = new HashSet<>();

    private final Set<Long> changedChunks = new HashSet<>();
    private final Set<FluidPos> sources = new HashSet<>();

    private float timer;

    private WaterSimulation() {}

    public boolean addSource(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return false;
        }

        FluidPos pos = new FluidPos(x, y, z);
        if (World.wrld.getBlockTypeAt(x, y, z) != BlockData.AIR.getId()) {
            return false;
        }
        sources.add(pos);
        setWater(pos, MAX_LEVEL);
        enqueue(pos);
        return true;
    }

    public boolean removeWater(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        if (!isWater(pos)) return false;
        sources.remove(pos);
        removeAndRebuildComponent(pos);
        return true;
    }

    public void update(float delta) {
        timer += delta;

        if (timer < STEP_TIME) {
            return;
        }

        timer -= STEP_TIME;
        int count = queue.size();

        while (count-- > 0) {
            FluidPos pos = queue.poll();
            if (pos == null) continue;
            queued.remove(pos);
            updateCell(pos);
        }
        rebuildChangedChunks();
    }

    private void updateCell(FluidPos pos) {
        if (!isWater(pos)) {
            return;
        }

        byte level = World.wrld.getWaterLevelAt(pos.x(), pos.y(), pos.z());
        if (level < MIN_LEVEL) {
            return;
        }

        FluidPos below = new FluidPos(pos.x(), pos.y() - 1, pos.z());
        if (canContainWater(below)) {
            byte belowLevel = World.wrld.getWaterLevelAt(below.x(), below.y(), below.z());
            if (belowLevel < MAX_LEVEL) {
                setWater(below, MAX_LEVEL);
                enqueue(below);
                return;
            }
        }

        if (level <= MIN_LEVEL) {
            return;
        }

        byte spreadLevel = (byte) (level - 1);
        spread(new FluidPos(pos.x() + 1, pos.y(), pos.z()), spreadLevel);
        spread(new FluidPos(pos.x() - 1, pos.y(), pos.z()), spreadLevel);
        spread(new FluidPos(pos.x(), pos.y(), pos.z() + 1), spreadLevel);
        spread(new FluidPos(pos.x(), pos.y(), pos.z() - 1), spreadLevel);
    }

    private void spread(FluidPos pos, byte level) {
        if (level < MIN_LEVEL) {
            return;
        }

        if (!canContainWater(pos)) {
            return;
        }

        byte currentLevel = World.wrld.getWaterLevelAt(pos.x(), pos.y(), pos.z());

        if (currentLevel >= level) {
            return;
        }

        setWater(pos, level);
        enqueue(pos);
    }

    private void setWater(FluidPos pos, byte level) {
        if (pos.y() < 0 || pos.y() >= Chunk.SIZE_Y) {
            return;
        }

        byte currentBlock = World.wrld.getBlockTypeAt(pos.x(), pos.y(), pos.z());
        if (!canContainWater(pos)) {
            return;
        }

        if (currentBlock != BlockData.AIR.getId() && currentBlock != BlockData.WATER.getId()) {
            BlockData data = BlockData.fromId(currentBlock);
            if (data == null || !data.isPlant()) {
                return;
            }

            World.wrld.removeBlockAt(pos.x(), pos.y(), pos.z());
        }

        byte currentLevel = World.wrld.getWaterLevelAt(pos.x(), pos.y(), pos.z());
        if (currentBlock == BlockData.WATER.getId() && currentLevel == level) {
            return;
        }

        World.wrld.setBlockTypeAt(pos.x(), pos.y(), pos.z(), BlockData.WATER.getId());
        World.wrld.setWaterLevelAt(pos.x(), pos.y(), pos.z(), level);
        mark(pos);
    }

    private void removeWaterCell(FluidPos pos) {
        if (!isWater(pos)) {
            return;
        }

        World.wrld.setWaterLevelAt(pos.x(), pos.y(), pos.z(), (byte) 0);
        World.wrld.setBlockTypeAt(pos.x(), pos.y(), pos.z(), BlockData.AIR.getId());
        mark(pos);
    }

    private void removeAndRebuildComponent(FluidPos removed) {
        Set<FluidPos> component = collect(removed);
        for (FluidPos pos : component) {
            if (sources.contains(pos)) {
                continue;
            }
            removeWaterCell(pos);
        }

        for (FluidPos pos : component) {
            if (!sources.contains(pos)) {
                continue;
            }

            setWater(pos, MAX_LEVEL);
            enqueue(pos);
        }

        for (FluidPos pos : component) {
            enqueueNeighbours(pos);
        }
    }

    private Set<FluidPos> collect(FluidPos start) {
        Set<FluidPos> component = new HashSet<>();
        Queue<FluidPos> searchQueue = new ArrayDeque<>();
        Set<FluidPos> visited = new HashSet<>();

        if (isWater(start)) {
            visited.add(start);
            searchQueue.add(start);
        }

        while (!searchQueue.isEmpty()) {
            FluidPos pos = searchQueue.poll();
            component.add(pos);
            addWaterNeighbour(new FluidPos(pos.x() + 1, pos.y(), pos.z()), searchQueue, visited);
            addWaterNeighbour(new FluidPos(pos.x() - 1, pos.y(), pos.z()), searchQueue, visited);
            addWaterNeighbour(new FluidPos(pos.x(), pos.y() + 1, pos.z()), searchQueue, visited);
            addWaterNeighbour(new FluidPos(pos.x(), pos.y() - 1, pos.z()), searchQueue, visited);
            addWaterNeighbour(new FluidPos(pos.x(), pos.y(), pos.z() + 1), searchQueue, visited);
            addWaterNeighbour(new FluidPos(pos.x(), pos.y(), pos.z() - 1), searchQueue, visited);
        }
        return component;
    }

    private void addWaterNeighbour(FluidPos pos,
                                   Queue<FluidPos> searchQueue, Set<FluidPos> visited) {
        if (visited.contains(pos)) {
            return;
        }

        if (!isWater(pos)) {
            return;
        }

        visited.add(pos);
        searchQueue.add(pos);
    }

    private boolean canContainWater(FluidPos pos) {
        if (pos.y() < 0 || pos.y() >= Chunk.SIZE_Y) {
            return false;
        }

        byte blockId = World.wrld.getBlockTypeAt(pos.x(), pos.y(), pos.z());
        if (blockId == BlockData.AIR.getId()) {
            return true;
        }

        if (blockId == BlockData.WATER.getId()) {
            return true;
        }

        BlockData data = BlockData.fromId(blockId);
        if (data.isPlant()) {
            ParticleEngine.peng.spawnPlant(new BlockPos(data,
                    pos.x(), pos.y(), pos.z()), data);
        }
        return data != null && data.isPlant();
    }

    private boolean isWater(FluidPos pos) {
        return World.wrld.getBlockTypeAt(pos.x(), pos.y(), pos.z()) == BlockData.WATER.getId();
    }

    private void enqueue(FluidPos pos) {
        if (queued.add(pos)) {
            queue.add(pos);
        }
    }

    private void enqueueNeighbours(FluidPos pos) {
        enqueue(pos);
        enqueue(new FluidPos(pos.x() + 1, pos.y(), pos.z()));
        enqueue(new FluidPos(pos.x() - 1, pos.y(), pos.z()));
        enqueue(new FluidPos(pos.x(), pos.y() + 1, pos.z()));
        enqueue(new FluidPos(pos.x(), pos.y() - 1, pos.z()));
        enqueue(new FluidPos(pos.x(), pos.y(), pos.z() + 1));
        enqueue(new FluidPos(pos.x(), pos.y(), pos.z() - 1));
    }

    public void onBlockDestroyed(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        mark(pos);
        enqueueNeighbours(pos);
    }

    private void mark(FluidPos pos) {
        long key = World.wrld.get2DKey(Math.floorDiv(pos.x(), Chunk.SIZE_X),
                Math.floorDiv(pos.z(), Chunk.SIZE_Z));
        changedChunks.add(key);
    }

    private void rebuildChangedChunks() {
        if (changedChunks.isEmpty()) {
            return;
        }

        for (long key : changedChunks) {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) key;
            GameMaster.game.rebuildChunkMeshAt(chunkX * Chunk.SIZE_X, chunkZ * Chunk.SIZE_Z);
        }

        changedChunks.clear();
    }
}