package com.isofarm.wrld;

import com.isofarm.data.BlockData;

import java.util.Random;

public class WorldGenerator {
    private static final int ISLAND_CENTER_X = 0;
    private static final int ISLAND_CENTER_Z = 0;
    private static final float ISLAND_RADIUS = 16.0f;
    private static final int SURFACE_Y = 25;
    private static final int MAX_DEPTH = 24;

    private static World world;
    private final long seed;

    private final int poolMinX;
    private final int poolMinZ;
    private int treeX, treeZ;

    public WorldGenerator(World world) {
        this(world, new Random().nextLong());
    }

    public WorldGenerator(World world, long seed) {
        WorldGenerator.world = world;
        this.seed = seed;

        Random islandRandom = new Random(seed);
        this.poolMinX = islandRandom.nextInt(10) - 5;
        this.poolMinZ = islandRandom.nextInt(10) - 5;

        do {
            this.treeX = islandRandom.nextInt(14) - 7;
            this.treeZ = islandRandom.nextInt(14) - 7;
        } while (isInsidePool(treeX, treeZ));
    }

    private boolean isInsidePool(int x, int z) {
        return (x >= poolMinX && x <= poolMinX + 1) && (z >= poolMinZ && z <= poolMinZ + 1);
    }

    public void generateChunk(int chunkX, int chunkZ) {
        Chunk chunk = world.getOrCreateChunk(chunkX, chunkZ);
        long chunkSeed = seed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
        Random chunkRandom = new Random(chunkSeed);

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;

                float dx = worldX - ISLAND_CENTER_X;
                float dz = worldZ - ISLAND_CENTER_Z;
                float distToCenter = (float) Math.sqrt(dx * dx + dz * dz);

                boolean isWaterBlock = isInsidePool(worldX, worldZ);

                for (int y = SURFACE_Y; y >= SURFACE_Y - MAX_DEPTH; y--) {
                    float depthFactor = (float) (y - (SURFACE_Y - MAX_DEPTH)) / MAX_DEPTH;
                    float currentAllowedRadius = (ISLAND_RADIUS * (0.3f + 0.7f * depthFactor));

                    if (distToCenter <= currentAllowedRadius) {
                        byte blockId;
                        if (y == SURFACE_Y) {
                            blockId = isWaterBlock ? BlockData.WATER.getId() : BlockData.GRASS.getId();
                        } else if (y >= SURFACE_Y - 3) {
                            blockId = BlockData.DIRT.getId();
                        } else {
                            if (chunkRandom.nextFloat() < 0.03f && y < SURFACE_Y - 5) {
                                blockId = BlockData.getRandomOre().getId();
                            } else {
                                blockId = BlockData.STONE.getId();
                            }
                        }

                        if (blockId != BlockData.AIR.getId()) {
                            chunk.setBlock(x, y, z, blockId);
                        }
                    }
                }
            }
        }

        if (chunkX == 0 && chunkZ == 0) {
            generateVegetation(chunkRandom);
        }
    }

    private void generateVegetation(Random random) {
        generateTree(treeX, treeZ, random);
        for (int i = 0; i < 16; i++) {
            int fx = random.nextInt(14) - 7;
            int fz = random.nextInt(14) - 7;

            if (!isInsidePool(fx, fz) && (fx != treeX || fz != treeZ)) {
                BlockData[] plants = BlockData.allPlants();
                BlockData plant = plants[random.nextInt(plants.length)];

                if (!plant.equals(BlockData.OAK_BONSAI) &&
                        world.getBlockTypeAt(fx, SURFACE_Y, fz) != BlockData.AIR.getId() &&
                        world.getBlockTypeAt(fx, SURFACE_Y + 1, fz) == BlockData.AIR.getId()) {
                    world.setBlockTypeAt(fx, SURFACE_Y + 1, fz, plant.getId());
                    if (plant.equals(BlockData.TALL_GRASS)) {
                        generateCluster(fx, fz, plant, random);
                    }
                }
            }
        }
    }

    public void generateCluster(int centerX, int centerZ, BlockData plant, Random random) {
        int clusterRadius = 2;
        int clusterSize = 4 + random.nextInt(5);

        int placed = 0;

        for (int i = 0; i < clusterSize * 3 && placed < clusterSize; i++) {
            int x = centerX + random.nextInt(clusterRadius * 2 + 1) - clusterRadius;
            int z = centerZ + random.nextInt(clusterRadius * 2 + 1) - clusterRadius;
            if (isInsidePool(x, z)) {
                continue;
            }

            if (x == treeX && z == treeZ) {
                continue;
            }

            if (world.getBlockTypeAt(x, SURFACE_Y, z) == BlockData.AIR.getId()) {
                continue;
            }

            if (world.getBlockTypeAt(x, SURFACE_Y + 1, z) != BlockData.AIR.getId()) {
                continue;
            }

            world.setBlockTypeAt(x, SURFACE_Y + 1, z, plant.getId());
            placed++;
        }
    }

    public static void generateTree(int worldX, int worldZ, Random random) {
        int trunkHeight = 4 + random.nextInt(5);

        for (int y = 1; y <= trunkHeight; y++) {
            world.setBlockTypeAt(worldX, SURFACE_Y + y, worldZ, BlockData.OAK_LOG.getId());
        }

        int topY = SURFACE_Y + trunkHeight;
        int leafRadius = 2;

        for (int ly = topY - 2; ly <= topY + 1; ly++) {
            int subRadius = (ly == topY + 1) ? 1 : leafRadius;

            for (int dx = -subRadius; dx <= subRadius; dx++) {
                for (int dz = -subRadius; dz <= subRadius; dz++) {
                    if (Math.abs(dx) == subRadius && Math.abs(dz) == subRadius && random.nextDouble() < 0.4) {
                        continue;
                    }

                    int targetX = worldX + dx;
                    int targetZ = worldZ + dz;
                    byte currentBlock = world.getBlockTypeAt(targetX, ly, targetZ);
                    if (currentBlock == BlockData.AIR.getId()) {
                        world.setBlockTypeAt(targetX, ly, targetZ, BlockData.OAK_LEAVES.getId());
                    }
                }
            }
        }
    }
}