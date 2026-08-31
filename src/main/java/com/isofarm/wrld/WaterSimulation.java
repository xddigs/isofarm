package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import com.isofarm.data.FluidPos;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class WaterSimulation {
    private static final byte MAX_LEVEL = 8;
    private static final byte MIN_LEVEL = 1;
    private static final float STEP_TIME = 0.08f;

    private final World world;

    private final Queue<FluidPos> queue = new ArrayDeque<>();
    private final Set<FluidPos> queued = new HashSet<>();
    private final Set<FluidPos> changed = new HashSet<>();

    private float timer;

    public WaterSimulation(World world) {
        this.world = world;
    }

    public void addSource(int x, int y, int z) {
        FluidPos pos = new FluidPos(x, y, z);
        world.setBlockTypeAt(x, y, z, BlockData.WATER.getId());
        world.setWaterLevelAt(x, y, z, MAX_LEVEL);
        changed.add(pos);
        enqueue(pos);
    }

    public void remove(int x, int y, int z) {
        if (world.getBlockTypeAt(x, y, z) != BlockData.WATER.getId()) return;
        world.setWaterLevelAt(x, y, z, (byte) 0);
        world.setBlockTypeAt(x, y, z, BlockData.AIR.getId());
        FluidPos pos = new FluidPos(x, y, z);
        changed.add(pos);
        enqueueNeighbours(pos);
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
            if (belowLevel < MAX_LEVEL) {
                setWater(below, MAX_LEVEL);
                enqueue(below);
                enqueueNeighbours(below);

                return;
            }
        }

        if (level <= MIN_LEVEL) {
            return;
        }

        byte spreadLevel = (byte) (level - 1);
        spread(pos, new FluidPos(pos.x() + 1, pos.y(), pos.z()), spreadLevel);
        spread(pos, new FluidPos(pos.x() - 1, pos.y(), pos.z()), spreadLevel);
        spread(pos, new FluidPos(pos.x(), pos.y(), pos.z() + 1), spreadLevel);
        spread(pos, new FluidPos(pos.x(), pos.y(), pos.z() - 1), spreadLevel);
    }

    private void spread(FluidPos from, FluidPos to, byte level) {
        if (level < MIN_LEVEL) return;
        if (!canContainWater(to)) return;
        byte current = world.getWaterLevelAt(to.x(), to.y(), to.z());
        if (current >= level) {
            return;
        }

        setWater(to, level);
        enqueue(to);
        enqueueNeighbours(to);
    }

    private void setWater(FluidPos pos, byte level) {
        world.setBlockTypeAt(pos.x(), pos.y(), pos.z(), BlockData.WATER.getId());
        world.setWaterLevelAt(pos.x(), pos.y(), pos.z(), level);
        changed.add(pos);
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
        changed.add(pos);
        enqueueNeighbours(pos);
    }

    private void rebuildChangedChunks() {
        if (changed.isEmpty()) {
            return;
        }

        for (FluidPos pos : changed) {
            world.getGameMaster().rebuildChunkMeshAt(pos.x(), pos.z());
        }

        changed.clear();
    }
}