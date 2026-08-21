package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import org.joml.SimplexNoise;

import java.util.Random;

public class WorldGenerator {
    private static final float NOISE_SCALE = 0.03f;
    private static final int BASE_HEIGHT = 20;
    private static final int HEIGHT_VARIATION = 4;
    private final World world;

    public WorldGenerator(World world) {
        this.world = world;
    }

    public void generateChunk(int chunkX, int chunkZ) {
        Chunk chunk = world.getOrCreateChunk(chunkX, chunkZ);
        long chunkSeed = ((long) chunkX * 341873128712L) + ((long) chunkZ * 132897987541L);
        Random chunkRandom = new Random(chunkSeed);

        int[][] heightMap = new int[Chunk.SIZE_X][Chunk.SIZE_Z];
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;

                float noise = SimplexNoise.noise(worldX * NOISE_SCALE, worldZ * NOISE_SCALE);
                int height = (int) (BASE_HEIGHT + (noise * HEIGHT_VARIATION));
                height = Math.clamp(height, 1, Chunk.SIZE_Y - 1);
                heightMap[x][z] = height;

                for (int y = 0; y <= height; y++) {
                    byte blockId = getBlockId(y, height);
                    chunk.setBlock(x, y, z, blockId);
                }
            }
        }

        for (int x = 2; x < Chunk.SIZE_X - 2; x++) {
            for (int z = 2; z < Chunk.SIZE_Z - 2; z++) {
                int height = heightMap[x][z];
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;

                if (chunkRandom.nextDouble() < 0.01 && canPlaceTree(heightMap, x, z)) {
                    generateCompactTree(worldX, height, worldZ, chunkRandom);
                }
            }
        }
    }

    private static byte getBlockId(int y, int height) {
        byte blockId;
        if (y == height) {
            blockId = BlockData.GRASS.getId();
        } else if (y > height - 3) {
            blockId = BlockData.DIRT.getId();
        } else if (y > height - 7) {
            blockId = BlockData.STONE.getId();
        } else if (y > height - 12) {
            blockId = BlockData.VOIDSTONE.getId();
        } else {
            blockId = BlockData.AIR.getId();
        }
        return blockId;
    }

    private boolean canPlaceTree(int[][] heightMap, int centerX, int centerZ) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int nx = centerX + dx;
                int nz = centerZ + dz;
                if (nx >= 0 && nx < Chunk.SIZE_X && nz >= 0 && nz < Chunk.SIZE_Z) {
                    if (Math.abs(heightMap[nx][nz] - heightMap[centerX][centerZ]) > 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void generateCompactTree(int worldX, int groundY, int worldZ, Random random) {
        int trunkHeight = 4 + random.nextInt(2);
        int topY = groundY + trunkHeight;

        for (int y = 1; y <= trunkHeight; y++) {
            world.setBlockTypeAt(worldX, groundY + y, worldZ, BlockData.OAK_LOG.getId());
        }

        int leafRadius = 2;
        for (int ly = topY - 2; ly <= topY + 1; ly++) {
            int subRadius = (ly == topY + 1) ? 1 : leafRadius;

            for (int dx = -subRadius; dx <= subRadius; dx++) {
                for (int dz = -subRadius; dz <= subRadius; dz++) {

                    if (Math.abs(dx) == subRadius && Math.abs(dz) == subRadius) {
                        if (random.nextDouble() < 0.5) continue;
                    }

                    int targetX = worldX + dx;
                    int targetZ = worldZ + dz;
                    byte currentBlock = world.getBlockTypeAt(targetX, ly, targetZ);
                    if (currentBlock == 0) { // Asumiendo 0 = AIRE / Vacío
                        world.setBlockTypeAt(targetX, ly, targetZ, BlockData.OAK_LEAVES.getId());
                    }
                }
            }
        }
    }
}