package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import org.joml.SimplexNoise;

import java.util.Random;

@SuppressWarnings("all")
public class WorldGenerator {
    private static final float CONTINENTAL_SCALE = 0.005f;
    private static final float DETAIL_SCALE = 0.025f;

    private static final int BASE_HEIGHT = 18;
    private static final int MOUNTAIN_HEIGHT = 150;

    private static final int COPPER_MAX_HEIGHT = 45;
    private static final float COPPER_CHANCE = 0.015f;

    private final World world;

    private final long seed;
    private final float offsetX;
    private final float offsetZ;
    private final Random chunkRandom;

    public WorldGenerator(World world) {
        this(world, new Random().nextLong());
    }

    public WorldGenerator(World world, long seed) {
        this.world = world;
        this.seed = seed;

        this.chunkRandom = new Random(seed);
        this.offsetX = (seed & 0xFFFFL) * 311.7f;
        this.offsetZ = ((seed >> 16) & 0xFFFFL) * 137.3f;
    }

    public void generateChunk(int chunkX, int chunkZ) {
        Chunk chunk = world.getOrCreateChunk(chunkX, chunkZ);
        int[][] heightMap = new int[Chunk.SIZE_X][Chunk.SIZE_Z];

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;

                float sampleX = worldX + offsetX;
                float sampleZ = worldZ + offsetZ;

                float continental = (SimplexNoise.noise(sampleX * CONTINENTAL_SCALE,
                        sampleZ * CONTINENTAL_SCALE) + 1.0f) * 0.5f;

                float mountainFactor = (float) Math.pow(continental, 2.5f);
                float detailNoise = SimplexNoise.noise(sampleX * DETAIL_SCALE, sampleZ * DETAIL_SCALE);

                int height = (int) (BASE_HEIGHT + (detailNoise * 3.0f) + (mountainFactor * MOUNTAIN_HEIGHT));
                height = Math.clamp(height, 1, Chunk.SIZE_Y - 1);
                heightMap[x][z] = height;

                for (int y = 0; y <= height; y++) {
                    byte blockId = getBlockId(y, height, mountainFactor);
                    if (generateCopper(worldX, worldZ, y, height)) {
                        blockId = BlockData.COPPER_ORE.getId();
                    }
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
                    generateCompactTree(worldX, height, worldZ);
                }
            }
        }
    }

    private byte getBlockId(int y, int height, float mountainFactor) {
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

    private void generateCompactTree(int worldX, int groundY, int worldZ) {
        int trunkHeight = 4 + chunkRandom.nextInt(2);
        int topY = groundY + trunkHeight;

        for (int y = 1; y <= trunkHeight; y++) {
            world.setBlockTypeAt(worldX, groundY + y, worldZ, BlockData.OAK_LOG.getId());
        }

        int leafRadius = 2;
        for (int ly = topY - 2; ly <= topY + 1; ly++) {
            int subRadius = (ly == topY + 1) ? 1 : leafRadius;

            for (int dx = -subRadius; dx <= subRadius; dx++) {
                for (int dz = -subRadius; dz <= subRadius; dz++) {
                    if (Math.abs(dx) == subRadius && Math.abs(dz) == subRadius &&
                            chunkRandom.nextDouble() < 0.5) {
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

    private boolean generateCopper(int worldX, int worldZ, int y,
                                   int surfaceHeight) {
        if (y >= surfaceHeight || y > COPPER_MAX_HEIGHT || y >= surfaceHeight - 3) {
            return false;
        }

        long hash = seed;
        hash ^= worldX * 341873128712L;
        hash ^= worldZ * 132897987541L;
        hash ^= y * 42317861L;
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdl;
        hash ^= (hash >>> 33);

        long positiveHash = hash & Long.MAX_VALUE;
        return (positiveHash / (double) Long.MAX_VALUE) < COPPER_CHANCE;
    }
}