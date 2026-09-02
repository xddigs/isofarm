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
    private static final float STEP_TIME = 0.08f;

    private final World world;

    private final Queue<FluidPos> queue = new ArrayDeque<>();
    private final Set<FluidPos> queued = new HashSet<>();
    private final Set<Long> changedChunks = new HashSet<>();
    private final Set<FluidPos> sources = new HashSet<>();

    private float timer;

    public WaterSimulation(World world) {
        this.world = world;
    }

    public void addSource(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        long key = world.get2DKey(
                Math.floorDiv(pos.x(), Chunk.SIZE_X),
                Math.floorDiv(pos.z(), Chunk.SIZE_Z)
        );
        sources.add(pos);
        world.setBlockTypeAt(x, y, z, BlockData.WATER.getId());
        world.setWaterLevelAt(x, y, z, MAX_LEVEL);
        changedChunks.add(key);
        enqueue(pos);
    }

    public boolean removeWater(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        if (!isWater(pos)) return false;
        sources.remove(pos);
        world.setWaterLevelAt(x, y, z, (byte) 0);
        world.setBlockTypeAt(x, y, z, BlockData.AIR.getId());
        check(pos);
        enqueueNeighbours(pos);
        return true;
    }

    public void check(FluidPos pos) {
        long key = world.get2DKey(
                Math.floorDiv(pos.x(), Chunk.SIZE_X),
                Math.floorDiv(pos.z(), Chunk.SIZE_Z)
        );

        changedChunks.add(key);
    }

    public void update(float delta) {
        timer += delta;
        if (timer < STEP_TIME) return;
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

        byte level = world.getWaterLevelAt(pos.x(), pos.y(), pos.z());
        if (level <= 0) {
            return;
        }

        FluidPos below = new FluidPos(pos.x(), pos.y() - 1, pos.z());
        if (canContainWater(below)) {
            byte belowLevel = world.getWaterLevelAt(below.x(), below.y(), below.z());
            if (belowLevel < level) {
                setWater(below, level);
                enqueue(below);
                return;
            }
        }

        if (level == MIN_LEVEL) {
            return;
        }

        byte spreadLevel = (byte) (level - 1);
        spread(new FluidPos(pos.x() + 1, pos.y(), pos.z()), spreadLevel);
        spread(new FluidPos(pos.x() - 1, pos.y(), pos.z()), spreadLevel);
        spread(new FluidPos(pos.x(), pos.y(), pos.z() + 1), spreadLevel);
        spread(new FluidPos(pos.x(), pos.y(), pos.z() - 1), spreadLevel);
    }

    private void spread(FluidPos to, byte level) {
        if (level < MIN_LEVEL) {
            return;
        }

        if (!canContainWater(to)) return;
        byte current = world.getWaterLevelAt(to.x(), to.y(), to.z());
        if (current >= level) return;
        setWater(to, level);
        enqueue(to);
    }

    private void setWater(FluidPos pos, byte level) {
        byte current = world.getWaterLevelAt(pos.x(), pos.y(), pos.z());
        if (current == level && world.getBlockTypeAt(pos.x(), pos.y(), pos.z()) == BlockData.WATER.getId()) {
            return;
        }
        world.setBlockTypeAt(pos.x(), pos.y(), pos.z(), BlockData.WATER.getId());
        world.setWaterLevelAt(pos.x(), pos.y(), pos.z(), level);
        check(pos);
    }

    private boolean canContainWater(FluidPos pos) {
        if (pos.y() < 0 || pos.y() >= Chunk.SIZE_Y) {
            return false;
        }

        byte block = world.getBlockTypeAt(pos.x(), pos.y(), pos.z());

        if (block == BlockData.AIR.getId()) {
            return true;
        }

        return block == BlockData.WATER.getId();
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
        enqueue(new FluidPos(pos.x(), pos.y(), pos.z() + 1));
        enqueue(new FluidPos(pos.x(), pos.y(), pos.z() - 1));
        enqueue(new FluidPos(pos.x(), pos.y() + 1, pos.z()));
        enqueue(new FluidPos(pos.x(), pos.y() - 1, pos.z()));
    }

    public void onBlockDestroyed(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        long key = world.get2DKey(
                Math.floorDiv(pos.x(), Chunk.SIZE_X),
                Math.floorDiv(pos.z(), Chunk.SIZE_Z));
        changedChunks.add(key);
        enqueueNeighbours(pos);
    }

    private void rebuildChangedChunks() {
        if (changedChunks.isEmpty()) {
            return;
        }

        for (long key : changedChunks) {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) key;
            world.getGameMaster().rebuildChunkMeshAt(chunkX * Chunk.SIZE_X,
                    chunkZ * Chunk.SIZE_Z);
        }
        changedChunks.clear();
    }
}