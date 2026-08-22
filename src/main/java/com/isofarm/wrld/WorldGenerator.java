package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import org.joml.SimplexNoise;

import java.util.Random;

public class WorldGenerator {
    private static final float CONTINENTAL_SCALE = 0.005f;
    private static final float DETAIL_SCALE = 0.025f;

    private static final int BASE_HEIGHT = 18;
    private static final int MOUNTAIN_HEIGHT = 150;

    private final World world;
    private static Random chunkRandom = new Random();

    public WorldGenerator(World world) {
        this.world = world;
    }

    public void generateChunk(int chunkX, int chunkZ) {
        Chunk chunk = world.getOrCreateChunk(chunkX, chunkZ);
        long chunkSeed = ((long) chunkX * 341873128712L) + ((long) chunkZ * 132897987541L);

        int[][] heightMap = new int[Chunk.SIZE_X][Chunk.SIZE_Z];
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;
                float continental = (SimplexNoise.noise(worldX * CONTINENTAL_SCALE, worldZ * CONTINENTAL_SCALE) + 1.0f) * 0.5f;
                float mountainFactor = (float) Math.pow(continental, 2.5f);
                float detailNoise = SimplexNoise.noise(worldX * DETAIL_SCALE, worldZ * DETAIL_SCALE);
                int height = (int) (BASE_HEIGHT + (detailNoise * 3.0f) + (mountainFactor * MOUNTAIN_HEIGHT));
                height = Math.clamp(height, 1, Chunk.SIZE_Y - 1);
                heightMap[x][z] = height;

                for (int y = 0; y <= height; y++) {
                    byte blockId = getBlockId(y, height, mountainFactor);
                    chunk.setBlock(x, y, z, blockId);
                }
            }
        }

        for (int x = 2; x < Chunk.SIZE_X - 2; x++) {
            for (int z = 2; z < Chunk.SIZE_Z - 2; z++) {
                int height = heightMap[x][z];
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;

                if (height < BASE_HEIGHT + 12 && chunkRandom.nextDouble() < 0.01 && canPlaceTree(heightMap, x, z)) {
                    generateCompactTree(worldX, height, worldZ, chunkRandom);
                }
            }
        }
    }

    private static byte getBlockId(int y, int height, float mountainFactor) {
        boolean isHighMountain = mountainFactor > 0.45f;

        if (y == height) {
            if (isHighMountain && y > BASE_HEIGHT + 14) {
                return BlockData.SNOW.getId();
            }
            return BlockData.GRASS.getId();
        } else if (y > height - 3) {
            return isHighMountain ? BlockData.STONE.getId() : BlockData.DIRT.getId();
        } else if (chunkRandom.nextDouble() < 0.01) {
            return BlockData.VOIDSTONE.getId();
        } else {
            return BlockData.STONE.getId();
        }
    }

    private boolean canPlaceTree(int[][] heightMap, int centerX, int centerZ) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
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
                    if (Math.abs(dx) == subRadius && Math.abs(dz) == subRadius && random.nextDouble() < 0.5) {
                        continue;
                    }

                    int targetX = worldX + dx;
                    int targetZ = worldZ + dz;
                    byte currentBlock = world.getBlockTypeAt(targetX, ly, targetZ);
                    if (currentBlock == 0) {
                        world.setBlockTypeAt(targetX, ly, targetZ, BlockData.OAK_LEAVES.getId());
                    }
                }
            }
        }
    }
}