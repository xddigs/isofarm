package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import com.isofarm.data.FluidPos;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

@SuppressWarnings("all")
public class WaterSimulation {
    private static final byte MAX_LEVEL = 8;
    private static final byte MIN_LEVEL = 1;
    private static final float STEP_TIME = 0.25f;

    private final World world;
    private final Queue<FluidPos> queue = new ArrayDeque<>();
    private final Set<FluidPos> queued = new HashSet<>();
    private final Set<Long> changedChunks = new HashSet<>();
    private final Set<FluidPos> sources = new HashSet<>();

    private float timer;

    public WaterSimulation(World world) {
        this.world = world;
    }

    public boolean addSource(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return false;
        }
        FluidPos pos = new FluidPos(x, y, z);
        if (world.getBlockTypeAt(x, y, z) != BlockData.AIR.getId()) {
            return false;
        }
        sources.add(pos);
        setWater(pos, MAX_LEVEL);
        enqueue(pos);
        return true;
    }

    public boolean removeWater(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        if (!isWater(pos)) {
            return false;
        }
        boolean wasSource = sources.remove(pos);
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
            if (pos == null) {
                continue;
            }
            queued.remove(pos);
            updateCell(pos);
        }
        rebuildChangedChunks();
    }

    private void updateCell(FluidPos pos) {
        if (!isWater(pos)) {
            return;
        }

        byte level = world.getWaterLevelAt(pos.x(), pos.y(), pos.z());
        if (level < MIN_LEVEL) {
            return;
        }

        FluidPos below = new FluidPos(pos.x(), pos.y() - 1, pos.z());
        if (canContainWater(below)) {
            byte belowLevel = world.getWaterLevelAt(below.x(), below.y(), below.z());
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
        if (level < MIN_LEVEL || !canContainWater(pos)) {
            return;
        }
        byte currentLevel = world.getWaterLevelAt(pos.x(), pos.y(), pos.z());
        if (currentLevel >= level) {
            return;
        }
        setWater(pos, level);
        enqueue(pos);
    }

    private void setWater(FluidPos pos, byte level) {
        byte currentLevel = world.getWaterLevelAt(pos.x(), pos.y(), pos.z());
        byte currentBlock = world.getBlockTypeAt(pos.x(), pos.y(), pos.z());
        if (currentBlock == BlockData.WATER.getId() && currentLevel == level) {
            return;
        }

        BlockData data = BlockData.fromId(currentBlock);
        if (data != null && data.isPlant()) {
            world.removeBlockAt(pos.x(), pos.y(), pos.z());
        }

        world.setBlockTypeAt(pos.x(), pos.y(), pos.z(), BlockData.WATER.getId());
        world.setWaterLevelAt(pos.x(), pos.y(), pos.z(), level);
        mark(pos);
    }

    private void removeWaterCell(FluidPos pos) {
        if (!isWater(pos)) {
            return;
        }
        world.setWaterLevelAt(pos.x(), pos.y(), pos.z(), (byte) 0);
        world.setBlockTypeAt(pos.x(), pos.y(), pos.z(), BlockData.AIR.getId());
        mark(pos);
    }

    private void removeAndRebuildComponent(FluidPos removed) {
        Set<FluidPos> component = collectWaterComponent(removed);

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

    private Set<FluidPos> collectWaterComponent(FluidPos start) {
        Set<FluidPos> component = new HashSet<>();
        Queue<FluidPos> searchQueue = new ArrayDeque<>();
        Set<FluidPos> visited = new HashSet<>();

        addWaterNeighbour(new FluidPos(start.x() + 1, start.y(), start.z()), searchQueue, visited);
        addWaterNeighbour(new FluidPos(start.x() - 1, start.y(), start.z()), searchQueue, visited);
        addWaterNeighbour(new FluidPos(start.x(), start.y() + 1, start.z()), searchQueue, visited);
        addWaterNeighbour(new FluidPos(start.x(), start.y() - 1, start.z()), searchQueue, visited);
        addWaterNeighbour(new FluidPos(start.x(), start.y(), start.z() + 1), searchQueue, visited);
        addWaterNeighbour(new FluidPos(start.x(), start.y(), start.z() - 1), searchQueue, visited);

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

    private void addWaterNeighbour(FluidPos pos, Queue<FluidPos> searchQueue, Set<FluidPos> visited) {
        if (visited.contains(pos) || !isWater(pos)) {
            return;
        }
        visited.add(pos);
        searchQueue.add(pos);
    }

    private boolean canContainWater(FluidPos pos) {
        if (pos.y() < 0 || pos.y() >= Chunk.SIZE_Y) {
            return false;
        }

        byte blockId = world.getBlockTypeAt(pos.x(), pos.y(), pos.z());
        if (blockId == BlockData.AIR.getId()) {
            return true;
        }

        if (blockId == BlockData.WATER.getId()) {
            return true;
        }

        BlockData data = BlockData.fromId(blockId);
        return data != null && data.isPlant();
    }

    private boolean isWater(FluidPos pos) {
        return world.getBlockTypeAt(pos.x(), pos.y(), pos.z()) == BlockData.WATER.getId();
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
        long key = world.get2DKey(
                Math.floorDiv(pos.x(), Chunk.SIZE_X),
                Math.floorDiv(pos.z(), Chunk.SIZE_Z)
        );
        changedChunks.add(key);
    }

    private void rebuildChangedChunks() {
        if (changedChunks.isEmpty()) {
            return;
        }
        for (long key : changedChunks) {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) key;
            world.getGameMaster().rebuildChunkMeshAt(
                    chunkX * Chunk.SIZE_X,
                    chunkZ * Chunk.SIZE_Z
            );
        }
        changedChunks.clear();
    }
}