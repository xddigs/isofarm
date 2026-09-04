package com.isofarm.wrld;

import com.isofarm.data.*;
import com.isofarm.graphics.ParticleEngine;
import com.isofarm.graphics.ResourceManager;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Provides water simulation behavior.
 */
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

    /**
     * Creates a new {@code WaterSimulation} instance.
     */
    private WaterSimulation() {}

    /**
     * Adds the source.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the add source result
     */
    public boolean addSource(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return false;
        }

        FluidPos pos = new FluidPos(x, y, z);
        byte block = World.wrld.getBlockTypeAt(x, y, z);
        if (block != BlockData.AIR.getId() && block != BlockData.WATER.getId()) {
            return false;
        }

        sources.add(pos);
        setWater(pos, MAX_LEVEL);
        enqueue(pos);
        return true;
    }

    /**
     * Removes the water.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the remove water result
     */
    public boolean removeWater(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        if (!isWater(pos)) {
            return false;
        }

        sources.remove(pos);
        removeAndRebuild(pos);
        return true;
    }

    /**
     * Checks whether the given water cell is a source.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return {@code true} if the cell is a water source; otherwise {@code false}
     */
    public boolean isSource(int x, int y, int z) {
        return sources.contains(new FluidPos(x, y, z));
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
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

    /**
     * Updates the cell.
     * @param pos the pos value
     */
    private void updateCell(FluidPos pos) {
        if (!isWater(pos)) {
            return;
        }

        byte level = World.wrld.getWaterLevelAt(pos.x(), pos.y(), pos.z());
        if (level < MIN_LEVEL) {
            return;
        }

        renewSource(pos);
        if (sources.contains(pos)) {
            level = MAX_LEVEL;
        }

        FluidPos below = new FluidPos(pos.x(), pos.y() - 1, pos.z());

        if (canContainWater(below)) {
            byte belowLevel = World.wrld.getWaterLevelAt(below.x(),
                    below.y(), below.z());

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

    /**
     * Converts a supported flowing cell into a source when two horizontal
     * neighbouring source blocks feed it, matching voxel infinite-water pools.
     * @param pos the candidate water cell
     */
    private void renewSource(FluidPos pos) {
        if (sources.contains(pos) || !hasSourceSupport(pos)) {
            return;
        }

        int adjacentSources = 0;
        if (sources.contains(new FluidPos(pos.x() + 1, pos.y(), pos.z()))) adjacentSources++;
        if (sources.contains(new FluidPos(pos.x() - 1, pos.y(), pos.z()))) adjacentSources++;
        if (sources.contains(new FluidPos(pos.x(), pos.y(), pos.z() + 1))) adjacentSources++;
        if (sources.contains(new FluidPos(pos.x(), pos.y(), pos.z() - 1))) adjacentSources++;

        if (adjacentSources >= 2) {
            sources.add(pos);
            setWater(pos, MAX_LEVEL);
            enqueueNeighbours(pos);
        }
    }

    /** Checks whether an infinite source may rest at the given position. */
    private boolean hasSourceSupport(FluidPos pos) {
        if (pos.y() <= 0) {
            return false;
        }

        FluidPos below = new FluidPos(pos.x(), pos.y() - 1, pos.z());
        return sources.contains(below)
                || World.wrld.isBlockSolid(below.x(), below.y(), below.z());
    }

    /**
     * Performs the spread operation.
     * @param pos the pos value
     * @param level the level value
     */
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

    /**
     * Sets the water.
     * @param pos the pos value
     * @param level the level value
     */
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

    /**
     * Removes the water cell.
     * @param pos the pos value
     */
    private void removeWaterCell(FluidPos pos) {
        if (!isWater(pos)) return;
        World.wrld.setWaterLevelAt(pos.x(), pos.y(), pos.z(), (byte) 0);
        World.wrld.setBlockTypeAt(pos.x(), pos.y(), pos.z(), BlockData.AIR.getId());
        mark(pos);
    }

    /**
     * Removes the and rebuild.
     * @param removed the removed value
     */
    private void removeAndRebuild(FluidPos removed) {
        Set<FluidPos> component = collect(removed);
        Set<FluidPos> componentSources = new HashSet<>();

        for (FluidPos pos : component) {
            if (sources.contains(pos)) {
                componentSources.add(pos);
            }
        }

        for (FluidPos pos : component) {
            removeWaterCell(pos);
        }

        queue.removeAll(component);
        for (FluidPos pos : component) {
            queued.remove(pos);
        }

        for (FluidPos source : componentSources) {
            setWater(source, MAX_LEVEL);
        }

        Queue<FluidPos> rebuildQueue = new ArrayDeque<>();
        Set<FluidPos> rebuilt = new HashSet<>();

        for (FluidPos source : componentSources) {
            rebuildQueue.add(source);
            rebuilt.add(source);
        }

        while (!rebuildQueue.isEmpty()) {
            FluidPos pos = rebuildQueue.poll();
            if (!isWater(pos)) {
                continue;
            }

            byte level = World.wrld.getWaterLevelAt(pos.x(), pos.y(), pos.z());
            FluidPos below = new FluidPos(pos.x(), pos.y() - 1, pos.z());

            if (component.contains(below) && canContainWater(below)) {
                byte belowLevel = World.wrld.getWaterLevelAt(below.x(), below.y(), below.z());
                if (belowLevel < MAX_LEVEL) {
                    setWater(below, MAX_LEVEL);
                    if (rebuilt.add(below)) {
                        rebuildQueue.add(below);
                    }
                }
            }

            if (level <= MIN_LEVEL) {
                continue;
            }

            byte spreadLevel = (byte) (level - 1);
            FluidPos east = new FluidPos(pos.x() + 1, pos.y(), pos.z());
            FluidPos west = new FluidPos(pos.x() - 1, pos.y(), pos.z());
            FluidPos south = new FluidPos(pos.x(), pos.y(), pos.z() + 1);
            FluidPos north = new FluidPos(pos.x(), pos.y(), pos.z() - 1);
            rebuild(east, spreadLevel, component, rebuilt, rebuildQueue);
            rebuild(west, spreadLevel, component, rebuilt, rebuildQueue);
            rebuild(south, spreadLevel, component, rebuilt, rebuildQueue);
            rebuild(north, spreadLevel, component, rebuilt, rebuildQueue);
        }

        for (FluidPos pos : component) {
            if (isWater(pos)) {
                enqueue(pos);
            } else {
                enqueueNeighbours(pos);
            }
        }
    }

    /**
     * Performs the rebuild operation.
     * @param pos the pos value
     * @param level the level value
     * @param component the component value
     * @param rebuilt the rebuilt value
     * @param rebuildQueue the rebuild queue value
     */
    private void rebuild(FluidPos pos, byte level, Set<FluidPos> component,
                         Set<FluidPos> rebuilt, Queue<FluidPos> rebuildQueue) {
        if (level < MIN_LEVEL) {
            return;
        }

        if (!component.contains(pos)) {
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

        if (rebuilt.add(pos)) {
            rebuildQueue.add(pos);
        }
    }

    /**
     * Performs the collect operation.
     * @param start the start value
     * @return the collect result
     */
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

    /**
     * Adds the water neighbour.
     * @param pos the pos value
     * @param searchQueue the search queue value
     * @param visited the visited value
     */
    private void addWaterNeighbour(FluidPos pos, Queue<FluidPos> searchQueue, Set<FluidPos> visited) {

        if (visited.contains(pos)) {
            return;
        }

        if (!isWater(pos)) {
            return;
        }

        visited.add(pos);
        searchQueue.add(pos);
    }

    /**
     * Checks whether the contain water condition is met.
     * @param pos the pos value
     * @return {@code true} if contain water; otherwise {@code false}
     */
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
        Crop crop = World.wrld.getCropAt(pos.x(), pos.y(), pos.z());
        if (data != null && data.isPlant()) {
            ParticleEngine.peng.spawnPlant(new BlockPos(data, pos.x(), pos.y(), pos.z()), data);
        } else if (crop != null) {
            int frameIndex = crop.getStage().getFrameIndex();
            ParticleEngine.peng.spawnCrop(pos.x(), pos.y(), pos.z(),
                    ResourceManager.rem.getCropSpritesheets().get(crop), frameIndex);
        }

        return data != null && (data.isPlant() || crop != null);
    }

    /**
     * Checks whether the water condition is met.
     * @param pos the pos value
     * @return {@code true} if water; otherwise {@code false}
     */
    private boolean isWater(FluidPos pos) {
        return World.wrld.getBlockTypeAt(pos.x(), pos.y(), pos.z()) == BlockData.WATER.getId();
    }

    /**
     * Performs the enqueue operation.
     * @param pos the pos value
     */
    private void enqueue(FluidPos pos) {
        if (queued.add(pos)) {
            queue.add(pos);
        }
    }

    /**
     * Performs the enqueue neighbours operation.
     * @param pos the pos value
     */
    private void enqueueNeighbours(FluidPos pos) {
        enqueue(pos);
        enqueue(new FluidPos(pos.x() + 1, pos.y(), pos.z()));
        enqueue(new FluidPos(pos.x() - 1, pos.y(), pos.z()));
        enqueue(new FluidPos(pos.x(), pos.y() + 1, pos.z()));
        enqueue(new FluidPos(pos.x(), pos.y() - 1, pos.z()));
        enqueue(new FluidPos(pos.x(), pos.y(), pos.z() + 1));
        enqueue(new FluidPos(pos.x(), pos.y(), pos.z() - 1));
    }

    /**
     * Performs the on block destroyed operation.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public void onBlockDestroyed(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        mark(pos);
        enqueueNeighbours(pos);
    }

    /**
     * Updates the simulation after a block replaces an empty or water cell.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public void onBlockPlaced(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        sources.remove(pos);
        World.wrld.setWaterLevelAt(x, y, z, (byte) 0);
        mark(pos);
        enqueueNeighbours(pos);
    }

    /**
     * Performs the mark operation.
     * @param pos the pos value
     */
    private void mark(FluidPos pos) {
        long key = World.wrld.get2DKey(Math.floorDiv(pos.x(), Chunk.SIZE_X),
                Math.floorDiv(pos.z(), Chunk.SIZE_Z));
        changedChunks.add(key);
    }

    /**
     * Performs the rebuild changed chunks operation.
     */
    private void rebuildChangedChunks() {
        if (changedChunks.isEmpty()) {
            return;
        }

        for (long key : changedChunks) {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) key;
            GameMaster.game.rebuildChunkMeshAt(chunkX * Chunk.SIZE_X,
                    chunkZ * Chunk.SIZE_Z);
        }
        changedChunks.clear();
    }
}
