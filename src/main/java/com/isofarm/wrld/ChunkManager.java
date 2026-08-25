package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import com.isofarm.data.SoilPosition;
import com.isofarm.graphics.ChunkMeshBuilder;
import com.isofarm.graphics.Mesh;
import com.isofarm.utils.Settings;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkManager {
    private static final float SOIL_GRASS_TIME = 10.0f;
    private final World world;
    private final WorldGenerator generator;
    private final Map<Chunk, Mesh> chunkMeshes;
    private final Map<SoilPosition, Float> soilTimers;
    private final ExecutorService meshExecutor;
    private final ConcurrentLinkedQueue<MeshBuildResult> completedMeshes = new ConcurrentLinkedQueue<>();
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;

    public ChunkManager(World world) {
        this.world = world;
        this.generator = new WorldGenerator(world);
        this.chunkMeshes = new HashMap<>();
        this.soilTimers = new HashMap<>();
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
        this.meshExecutor = Executors.newFixedThreadPool(threads);
    }

    public void update(float playerX, float playerZ, float delta) {
        processCompletedMeshes();
        updateSoil(delta);

        int playerChunkX = Math.floorDiv((int) playerX, Chunk.SIZE_X);
        int playerChunkZ = Math.floorDiv((int) playerZ, Chunk.SIZE_Z);

        if (playerChunkX != lastPlayerChunkX || playerChunkZ != lastPlayerChunkZ) {
            updateLoadedChunks(playerChunkX, playerChunkZ);
            lastPlayerChunkX = playerChunkX;
            lastPlayerChunkZ = playerChunkZ;
        }
    }

    public void buildSingleChunkMesh(int chunkX, int chunkZ) {
        Chunk chunk = world.getChunks().get(world.get2DKey(chunkX, chunkZ));
        if (chunk == null) return;

        if (!chunkMeshes.containsKey(chunk)) {
            ChunkMeshBuilder.MeshData data = ChunkMeshBuilder.buildMesh(world, chunk);
            Mesh mesh = ChunkMeshBuilder.createMesh(data);
            if (mesh != null) {
                chunkMeshes.put(chunk, mesh);
            }
        }
    }

    public void updateLoadedChunks(int centerChunkX, int centerChunkZ) {
        int r = Settings.getRenderDistance();
        int unloadDist = r + Settings.getUnloadMargin();

        chunkMeshes.entrySet().removeIf(entry -> {
            Chunk chunk = entry.getKey();

            int dx = Math.abs(chunk.getChunkX() - centerChunkX);
            int dz = Math.abs(chunk.getChunkZ() - centerChunkZ);

            if (dx > unloadDist || dz > unloadDist) {
                Mesh mesh = entry.getValue();

                if (mesh != null) {
                    mesh.dispose();
                }

                world.getChunks().remove(world.get2DKey(chunk.getChunkX(), chunk.getChunkZ()));
                cleanupSoilTimersForChunk(chunk);
                return true;
            }

            return false;
        });

        for (int cx = centerChunkX - r; cx <= centerChunkX + r; cx++) {
            for (int cz = centerChunkZ - r; cz <= centerChunkZ + r; cz++) {

                if ((cx - centerChunkX) * (cx - centerChunkX) + (cz - centerChunkZ) * (cz - centerChunkZ) > r * r) {
                    continue;
                }

                Chunk chunk = world.getOrCreateChunk(cx, cz);

                if (!chunkMeshes.containsKey(chunk)) {
                    generator.generateChunk(cx, cz);
                    updateGrass(cx, cz);
                }
            }
        }

        for (int cx = centerChunkX - r; cx <= centerChunkX + r; cx++) {
            for (int cz = centerChunkZ - r; cz <= centerChunkZ + r; cz++) {

                if ((cx - centerChunkX) * (cx - centerChunkX) + (cz - centerChunkZ) * (cz - centerChunkZ) > r * r) {
                    continue;
                }

                Chunk chunk = world.getChunks().get(world.get2DKey(cx, cz));
                if (chunk == null) continue;

                if (!chunkMeshes.containsKey(chunk)) {
                    queueMeshBuild(chunk);
                }
            }
        }
    }

    private void queueMeshBuild(Chunk chunk) {
        meshExecutor.submit(() -> {
            ChunkMeshBuilder.MeshData data = ChunkMeshBuilder.buildMesh(world, chunk);

            completedMeshes.add(new MeshBuildResult(chunk, data));
        });
    }

    private void processCompletedMeshes() {
        MeshBuildResult result;
        while ((result = completedMeshes.poll()) != null) {
            Chunk chunk = result.chunk();
            if (!world.getChunks().containsKey(world.get2DKey(chunk.getChunkX(), chunk.getChunkZ()))) {
                continue;
            }

            Mesh oldMesh = chunkMeshes.get(chunk);
            if (oldMesh != null) {
                oldMesh.dispose();
            }

            Mesh mesh = ChunkMeshBuilder.createMesh(result.data());
            chunkMeshes.put(chunk, mesh);
        }
    }

    public Map<Chunk, Mesh> getChunkMeshes() {
        return chunkMeshes;
    }

    public void rebuildChunkMeshAt(int worldX, int worldZ) {
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        Chunk chunk = world.getChunks().get(world.get2DKey(chunkX, chunkZ));

        if (chunk != null) {
            queueMeshBuild(chunk);
        }
    }

    private void updateGrass(int chunkX, int chunkZ) {
        int startX = chunkX * Chunk.SIZE_X;
        int startZ = chunkZ * Chunk.SIZE_Z;

        for (int localX = 0; localX < Chunk.SIZE_X; localX++) {
            for (int localZ = 0; localZ < Chunk.SIZE_Z; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;

                for (int y = Chunk.SIZE_Y - 2; y >= 0; y--) {
                    byte block = world.getBlockTypeAt(worldX, y, worldZ);

                    if (block == BlockData.AIR.getId()) {
                        continue;
                    }

                    if (block == BlockData.DIRT.getId() || block == BlockData.TILLED_DIRT.getId()) {
                        if (isExposedToAir(worldX, y, worldZ)) {
                            startSoilTimer(worldX, y, worldZ);
                        }
                    }

                    break;
                }
            }
        }
    }

    private void updateSoil(float delta) {
        var iterator = soilTimers.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            SoilPosition position = entry.getKey();

            int x = position.x();
            int y = position.y();
            int z = position.z();

            byte block = world.getBlockTypeAt(x, y, z);

            if (!isSoil(block)) {
                iterator.remove();
                continue;
            }

            if (!isExposedToAir(x, y, z)) {
                continue;
            }

            if (block == BlockData.TILLED_DIRT.getId() && hasWaterNearby(x, y, z)) {
                continue;
            }

            float remaining = entry.getValue() - delta;

            if (remaining <= 0.0f) {
                world.setBlockTypeAt(x, y, z, BlockData.GRASS.getId());
                rebuildChunkMeshAt(x, z);
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    private void startSoilTimer(int x, int y, int z) {
        SoilPosition position = new SoilPosition(x, y, z);
        soilTimers.putIfAbsent(position, SOIL_GRASS_TIME);
    }

    private boolean isSoil(byte block) {
        return block == BlockData.DIRT.getId() || block == BlockData.TILLED_DIRT.getId();
    }

    private boolean isExposedToAir(int x, int y, int z) {
        return world.getBlockTypeAt(x, y + 1, z) == BlockData.AIR.getId();
    }

    private boolean hasWaterNearby(int x, int y, int z) {
        return false;
    }

    private void cleanupSoilTimersForChunk(Chunk chunk) {
        int minX = chunk.getChunkX() * Chunk.SIZE_X;
        int maxX = minX + Chunk.SIZE_X;
        int minZ = chunk.getChunkZ() * Chunk.SIZE_Z;
        int maxZ = minZ + Chunk.SIZE_Z;

        soilTimers.keySet().removeIf(pos -> pos.x() >= minX && pos.x() < maxX && pos.z() >= minZ && pos.z() < maxZ);
    }

    public void shutdown() {
        meshExecutor.shutdownNow();
    }

    public void dispose() {
        meshExecutor.shutdownNow();
        completedMeshes.clear();
        chunkMeshes.values().forEach(Mesh::dispose);
        chunkMeshes.clear();
        soilTimers.clear();
    }

    public int getLastPlayerChunkX() {
        return lastPlayerChunkX;
    }

    public void setLastPlayerChunkX(int x) {
        this.lastPlayerChunkX = x;
    }

    public int getLastPlayerChunkZ() {
        return lastPlayerChunkZ;
    }

    public void setLastPlayerChunkZ(int z) {
        this.lastPlayerChunkZ = z;
    }

    public WorldGenerator getGenerator() {
        return generator;
    }

    private record MeshBuildResult(Chunk chunk, ChunkMeshBuilder.MeshData data) {
    }
}