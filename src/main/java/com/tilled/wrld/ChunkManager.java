package com.tilled.wrld;

import com.tilled.graphics.ChunkMeshBuilder;
import com.tilled.graphics.Mesh;
import com.tilled.utils.Settings;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final World world;
    private final WorldGenerator generator;
    private final Map<Chunk, Mesh> chunkMeshes;
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;

    public ChunkManager(World world) {
        this.world = world;
        this.generator = new WorldGenerator(world);
        this.chunkMeshes = new HashMap<>();
    }

    public void update(float playerX, float playerZ) {
        int playerChunkX = Math.floorDiv((int) playerX, Chunk.SIZE_X);
        int playerChunkZ = Math.floorDiv((int) playerZ, Chunk.SIZE_Z);

        if (playerChunkX != lastPlayerChunkX || playerChunkZ != lastPlayerChunkZ) {
            updateLoadedChunks(playerChunkX, playerChunkZ);
            lastPlayerChunkX = playerChunkX;
            lastPlayerChunkZ = playerChunkZ;
        }
    }

    public void updateLoadedChunks(int centerChunkX, int centerChunkZ) {
        int r = Settings.renderDistance;
        int unloadDist = r + Settings.unloadMargin;
        chunkMeshes.entrySet().removeIf(entry -> {
            Chunk chunk = entry.getKey();
            int dx = Math.abs(chunk.getChunkX() - centerChunkX);
            int dz = Math.abs(chunk.getChunkZ() - centerChunkZ);

            if (dx > unloadDist || dz > unloadDist) {
                Mesh mesh = entry.getValue();
                if (mesh != null) {
                    mesh.dispose();
                }
                world.getChunks().remove(chunk.getChunkX(), chunk.getChunkZ());
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

                    Mesh mesh = ChunkMeshBuilder.buildMesh(chunk);
                    chunkMeshes.put(chunk, mesh);
                }
            }
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
            Mesh oldMesh = chunkMeshes.get(chunk);
            if (oldMesh != null) oldMesh.dispose();
            chunkMeshes.put(chunk, ChunkMeshBuilder.buildMesh(chunk));
        }
    }

    public void dispose() {
        chunkMeshes.values().forEach(Mesh::dispose);
        chunkMeshes.clear();
    }

    public int getLastPlayerChunkX() { return lastPlayerChunkX; }
    public void setLastPlayerChunkX(int x) { this.lastPlayerChunkX = x; }
    public int getLastPlayerChunkZ() { return lastPlayerChunkZ; }
    public void setLastPlayerChunkZ(int z) { this.lastPlayerChunkZ = z; }
}
