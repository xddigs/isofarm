package com.isofarm.wrld;

import com.isofarm.data.BlockData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates the starting island and the ocean that surrounds it. The island is
 * a continuous land mass rooted in the seabed rather than a floating volume.
 */
public final class IslandGenerator implements Generator {
    public static final int SEA_LEVEL = 25;
    public static final int OCEAN_DEPTH = 36;

    private static final int OCEAN_FLOOR_Y = SEA_LEVEL - OCEAN_DEPTH;
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;
    private static final float ISLAND_RADIUS = 18.0f;
    private static final float COAST_WIDTH = 3.0f;
    private static final float SEABED_TERRACE_WIDTH = 2.0f;
    private static final int ISLAND_HEIGHT = 7;
    private static final float LAKE_RADIUS = 3.5f;
    private static final float LAVA_RADIUS = 1.75f;
    private static final float LAVA_CHANCE = 0.35f;

    private final World world;
    private final FluidSimulation waterSimulation;
    private final long seed;
    private final Lake lake;
    private final LavaPool lavaPool;
    private final List<Tree> trees = new ArrayList<>();

    /**
     * Creates a generator with a random world seed.
     */
    public IslandGenerator(World world, FluidSimulation waterSimulation) {
        this(world, waterSimulation, new Random().nextLong());
    }

    /**
     * Creates a deterministic generator for the supplied seed.
     */
    public IslandGenerator(World world, FluidSimulation waterSimulation, long seed) {
        this.world = world;
        this.waterSimulation = waterSimulation;
        this.seed = seed;

        Random random = new Random(seed);
        lake = chooseLake(random);
        lavaPool = random.nextFloat() < LAVA_CHANCE ? chooseLavaPool(random) : null;
        chooseTrees(random);
    }

    /**
     * Generates terrain and features owned by one chunk.
     */
    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        Chunk chunk = world.getOrCreateChunk(chunkX, chunkZ);
        for (int localX = 0; localX < Chunk.SIZE_X; localX++) {
            for (int localZ = 0; localZ < Chunk.SIZE_Z; localZ++) {
                int worldX = chunkX * Chunk.SIZE_X + localX;
                int worldZ = chunkZ * Chunk.SIZE_Z + localZ;
                generateColumn(chunk, localX, localZ, worldX, worldZ);
            }
        }

        registerWaterSources(chunk, chunkX, chunkZ);
        registerLavaSources(chunkX, chunkZ);
        generateTreesInChunk(chunkX, chunkZ);
    }

    private void generateColumn(Chunk chunk, int localX, int localZ, int worldX, int worldZ) {
        boolean island = isIsland(worldX, worldZ);
        boolean inLake = island && isInLake(worldX, worldZ);
        boolean inLava = island && isInLavaPool(worldX, worldZ);

        int topY = island ? terrainHeight(worldX, worldZ) : seabedHeight(worldX, worldZ);
        if (inLake) topY = lake.surfaceY - 2;
        if (inLava) topY = lavaPool.surfaceY - 1;

        for (int y = OCEAN_FLOOR_Y; y <= topY; y++) {
            byte block;
            if (y == topY) {
                block = islandSurfaceBlock(worldX, worldZ, inLake, inLava);
            } else if (y >= topY - 3) {
                block = BlockData.DIRT.getId();
            } else {
                block = BlockData.STONE.getId();
            }
            chunk.setBlock(localX, y, localZ, block);
        }

        if (inLake) {
            fillFluidColumn(chunk, localX, localZ, topY + 1, lake.surfaceY,
                    waterSimulation.getFluidType().getId());
        } else if (inLava) {
            chunk.setBlock(localX, lavaPool.surfaceY, localZ, BlockData.LAVA.getId());
            chunk.setFluidLevel(localX, lavaPool.surfaceY, localZ, (byte) 8);
        } else if (!island) {
            // The ocean begins as one perfectly level source layer. WaterSimulation
            // progressively drops it onto the generated seabed.
            chunk.setBlock(localX, SEA_LEVEL, localZ, waterSimulation.getFluidType().getId());
            chunk.setFluidLevel(localX, SEA_LEVEL, localZ, (byte) 8);
        }
    }

    private byte islandSurfaceBlock(int x, int z, boolean inLake, boolean inLava) {
        if (inLake || inLava || isCoast(x, z)) return BlockData.SAND.getId();
        return BlockData.GRASS.getId();
    }

    private int terrainHeight(int x, int z) {
        float boundary = islandBoundary(x, z);
        float inland = Math.clamp(1.0f - distance(x, z, CENTER_X, CENTER_Z) / boundary,
                0.0f, 1.0f);
        return SEA_LEVEL + Math.round(ISLAND_HEIGHT * inland * inland
                + noise(x, z, 5) * inland);
    }

    /**
     * Raises the seabed in broad terraces near the island and lets it descend
     * naturally to the configured 24-block ocean depth.
     */
    private int seabedHeight(int x, int z) {
        float outside = Math.max(0.0f,
                distance(x, z, CENTER_X, CENTER_Z) - islandBoundary(x, z));
        int descent = 1 + (int) (outside / SEABED_TERRACE_WIDTH);
        return Math.max(OCEAN_FLOOR_Y, SEA_LEVEL - descent);
    }

    private boolean isIsland(int x, int z) {
        return distance(x, z, CENTER_X, CENTER_Z) <= islandBoundary(x, z);
    }

    private float islandBoundary(int x, int z) {
        return ISLAND_RADIUS + noise(x, z, 5) * 1.6f + noise(x, z, 11) * 1.2f;
    }

    private boolean isCoast(int x, int z) {
        float edgeDistance = islandBoundary(x, z) - distance(x, z, CENTER_X, CENTER_Z);
        return edgeDistance <= COAST_WIDTH || terrainHeight(x, z) <= SEA_LEVEL + 1;
    }

    private Lake chooseLake(Random random) {
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = random.nextInt(19) - 9;
            int z = random.nextInt(19) - 9;
            float centerDistance = distance(x, z, CENTER_X, CENTER_Z);
            if (centerDistance < 6.0f || centerDistance > 9.0f || isCoast(x, z)) continue;
            int surfaceY = Math.max(SEA_LEVEL + 1, terrainHeight(x, z) - 1);
            return new Lake(x, z, surfaceY);
        }
        return new Lake(7, 0, Math.max(SEA_LEVEL + 1, terrainHeight(7, 0) - 1));
    }

    private LavaPool chooseLavaPool(Random random) {
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = random.nextInt(21) - 10;
            int z = random.nextInt(21) - 10;
            if (!isIsland(x, z) || isCoast(x, z)
                    || distance(x, z, CENTER_X, CENTER_Z) < 5.0f
                    || distance(x, z, lake.x, lake.z) < LAKE_RADIUS + LAVA_RADIUS + 3.0f) {
                continue;
            }
            return new LavaPool(x, z, terrainHeight(x, z));
        }
        return null;
    }

    private void chooseTrees(Random random) {
        int requested = 4 + random.nextInt(3);
        for (int attempt = 0; trees.size() < requested && attempt < 500; attempt++) {
            int x = random.nextInt(27) - 13;
            int z = random.nextInt(27) - 13;
            if (!isIsland(x, z) || isCoast(x, z) || isInLake(x, z) || isInLavaPool(x, z)
                    || distance(x, z, CENTER_X, CENTER_Z) < 4.0f
                    || distance(x, z, lake.x, lake.z) < LAKE_RADIUS + 2.0f
                    || nearTree(x, z)) continue;
            trees.add(new Tree(x, terrainHeight(x, z), z,
                    4 + random.nextInt(4), random.nextLong()));
        }
    }

    private boolean nearTree(int x, int z) {
        for (Tree tree : trees) {
            if (distance(x, z, tree.x, tree.z) < 5.0f) return true;
        }
        return false;
    }

    private boolean isInLake(int x, int z) {
        return distance(x, z, lake.x, lake.z)
                <= LAKE_RADIUS + noise(x, z, 3) * 0.75f;
    }

    private boolean isInLavaPool(int x, int z) {
        return lavaPool != null && distance(x, z, lavaPool.x, lavaPool.z)
                <= LAVA_RADIUS + noise(x, z, 2) * 0.3f;
    }

    private void fillFluidColumn(Chunk chunk, int x, int z, int bottomY, int topY, byte fluidId) {
        for (int y = bottomY; y <= topY; y++) {
            chunk.setBlock(x, y, z, fluidId);
            chunk.setFluidLevel(x, y, z, (byte) 8);
        }
    }

    private void registerWaterSources(Chunk chunk, int chunkX, int chunkZ) {
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;
                if (isIsland(worldX, worldZ)) {
                    if (isInLake(worldX, worldZ)
                            && chunk.getBlock(x, lake.surfaceY, z) == waterSimulation.getFluidType().getId()) {
                        waterSimulation.addSource(worldX, lake.surfaceY, worldZ);
                    }
                    continue;
                }

                if (chunk.getBlock(x, SEA_LEVEL, z) == waterSimulation.getFluidType().getId()) {
                    waterSimulation.addSource(worldX, SEA_LEVEL, worldZ);
                }
            }
        }
    }

    private void registerLavaSources(int chunkX, int chunkZ) {
        if (lavaPool == null) return;
        for (int x = (int) Math.floor(lavaPool.x - LAVA_RADIUS - 1);
             x <= (int) Math.ceil(lavaPool.x + LAVA_RADIUS + 1); x++) {
            for (int z = (int) Math.floor(lavaPool.z - LAVA_RADIUS - 1);
                 z <= (int) Math.ceil(lavaPool.z + LAVA_RADIUS + 1); z++) {
                if (!isInLavaPool(x, z)
                        || Math.floorDiv(x, Chunk.SIZE_X) != chunkX
                        || Math.floorDiv(z, Chunk.SIZE_Z) != chunkZ) continue;
                LavaSimulation.ls.addSource(x, lavaPool.surfaceY, z);
            }
        }
    }

    private void generateTreesInChunk(int chunkX, int chunkZ) {
        for (Tree tree : trees) {
            if (Math.floorDiv(tree.x, Chunk.SIZE_X) != chunkX
                    || Math.floorDiv(tree.z, Chunk.SIZE_Z) != chunkZ) continue;
            generateTree(tree);
        }
    }

    private void generateTree(Tree tree) {
        Random random = new Random(tree.seed);
        for (int y = 1; y <= tree.height; y++) {
            world.setBlockTypeAt(tree.x, tree.surfaceY + y, tree.z, BlockData.OAK_LOG.getId());
        }

        int topY = tree.surfaceY + tree.height;
        for (int y = topY - 2; y <= topY + 1; y++) {
            int radius = y == topY + 1 ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius
                            && random.nextFloat() < 0.4f) continue;
                    if (world.getBlockTypeAt(tree.x + dx, y, tree.z + dz) == BlockData.AIR.getId()) {
                        world.setBlockTypeAt(tree.x + dx, y, tree.z + dz,
                                BlockData.OAK_LEAVES.getId());
                    }
                }
            }
        }
    }

    private float noise(int x, int z, int scale) {
        long value = seed + (long) Math.floorDiv(x, scale) * 341873128712L
                + (long) Math.floorDiv(z, scale) * 132897987541L;
        value = (value ^ value >>> 30) * 0xbf58476d1ce4e5b9L;
        value = (value ^ value >>> 27) * 0x94d049bb133111ebL;
        value ^= value >>> 31;
        return ((value & 0xffffL) / 32767.5f) - 1.0f;
    }

    private static float distance(int x1, int z1, int x2, int z2) {
        return (float) Math.hypot(x1 - x2, z1 - z2);
    }

    private record Lake(int x, int z, int surfaceY) {}

    private record LavaPool(int x, int z, int surfaceY) {}

    private record Tree(int x, int surfaceY, int z, int height, long seed) {}
}
