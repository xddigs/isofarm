package com.isofarm.wrld;

import com.isofarm.data.BlockData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a configurable island, including terrain, lakes, mountains and
 * vegetation. Worlds created with the same seed and settings are deterministic.
 */
public class WorldGenerator implements Generator {
    public static final int TREE_COUNT = 6;
    public static final int LAKE_COUNT = 2;
    public static final int LAKE_ORGANICITY_PERCENT = 60;
    public static final boolean GENERATE_MOUNTAINS = false;
    public static final int MAX_MOUNTAIN_HEIGHT = 25;
    public static final boolean GENERATE_LAKE_SAND_SHORES = true;

    private static final int ISLAND_CENTER_X = 0;
    private static final int ISLAND_CENTER_Z = 0;
    private static final float ISLAND_RADIUS = 16.0f;
    private static final int SURFACE_Y = 25;
    private static final int MAX_DEPTH = 50;
    private static final float LAKE_RADIUS = 4.0f;
    private static final float LAKE_SHORE_WIDTH = 1.5f;
    private static final int PLANT_ATTEMPTS = 16;

    private static World world;
    private static FluidSimulation fluidSimulation;
    private final long seed;
    private final List<Lake> lakes = new ArrayList<>();
    private final List<Tree> trees = new ArrayList<>();
    private final int mountainX;
    private final int mountainZ;

    /**
     * Creates a new {@code WorldGenerator} instance with a random seed.
     * @param world the world value
     * @param fluidSimulation the fluid simulation used for generated lakes
     */
    public WorldGenerator(World world, FluidSimulation fluidSimulation) {
        this(world, fluidSimulation, new Random().nextLong());
    }

    /**
     * Creates a new {@code WorldGenerator} instance.
     * @param world the world value
     * @param fluidSimulation the fluid simulation used for generated lakes
     * @param seed the seed value
     */
    public WorldGenerator(World world, FluidSimulation fluidSimulation, long seed) {
        WorldGenerator.world = world;
        WorldGenerator.fluidSimulation = fluidSimulation;
        this.seed = seed;

        Random random = new Random(seed);
        mountainX = random.nextInt(13) - 6;
        mountainZ = -10 - random.nextInt(7);
        chooseLakes(random);
        chooseTrees(random);
    }

    /**
     * Generates the island terrain and features contained in a chunk.
     * @param chunkX the chunk x value
     * @param chunkZ the chunk z value
     */
    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        Chunk chunk = world.getOrCreateChunk(chunkX, chunkZ);
        long chunkSeed = seed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
        Random random = new Random(chunkSeed);

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;
                float distance = distance(worldX, worldZ, ISLAND_CENTER_X, ISLAND_CENTER_Z);
                if (distance > ISLAND_RADIUS) continue;

                Lake lake = lakeAt(worldX, worldZ);
                boolean lakeShore = lake == null && isLakeShore(worldX, worldZ);
                int terrainY = terrainHeight(worldX, worldZ);
                int topY = lake == null ? terrainY : SURFACE_Y - 1;

                for (int y = topY; y >= Math.max(1, topY - MAX_DEPTH); y--) {
                    float depthFactor = (float) (y - (SURFACE_Y - MAX_DEPTH)) / MAX_DEPTH;
                    float allowedRadius = ISLAND_RADIUS * (0.3f + 0.7f * Math.clamp(depthFactor, 0, 1));
                    if (distance > allowedRadius && y <= SURFACE_Y) continue;

                    byte blockId;
                    if (y == topY) {
                        blockId = lake != null || lakeShore
                                ? BlockData.SAND.getId()
                                : BlockData.GRASS.getId();
                    }
                    else if (y >= topY - 3) blockId = BlockData.DIRT.getId();
                    else if (random.nextFloat() < 0.03f && y < topY - 5) blockId = BlockData.getRandomOre().getId();
                    else blockId = BlockData.STONE.getId();
                    chunk.setBlock(x, y, z, blockId);
                }

                if (lake != null) {
                    chunk.setBlock(x, SURFACE_Y, z, fluidSimulation.getFluidType().getId());
                    chunk.setFluidLevel(x, SURFACE_Y, z, (byte) 8);
                }
            }
        }

        registerFluidSources(chunk, chunkX, chunkZ);
        generateTreesInChunk(chunkX, chunkZ, random);
        if (chunkX == 0 && chunkZ == 0) generatePlants(random);
    }

    /**
     * Returns the generated surface height at a world position.
     * @param x the world x value
     * @param z the world z value
     * @return the generated terrain height
     */
    private int terrainHeight(int x, int z) {
        if (!GENERATE_MOUNTAINS || MAX_MOUNTAIN_HEIGHT <= 0 || z >= 0) return SURFACE_Y;
        float influence = Math.max(0, 1 - distance(x, z, mountainX, mountainZ) / 11.0f);
        float organic = 0.85f + noise(x, z, 5) * 0.15f;
        return SURFACE_Y + Math.round(MAX_MOUNTAIN_HEIGHT * influence * influence * organic);
    }

    /**
     * Selects valid positions for the configured lakes.
     * @param random the random value
     */
    private void chooseLakes(Random random) {
        int requested = Math.max(0, LAKE_COUNT);
        for (int attempts = 0; lakes.size() < requested && attempts < requested * 100 + 100; attempts++) {
            int x = random.nextInt((int) ISLAND_RADIUS * 2 - 12) - (int) ISLAND_RADIUS + 6;
            int z = random.nextInt((int) ISLAND_RADIUS - 7); // Keep lakes away from the northern mountain.
            if (distance(x, z, 0, 0) > ISLAND_RADIUS - LAKE_RADIUS - 2 || overlapsLake(x, z)) continue;
            lakes.add(new Lake(x, z, LAKE_RADIUS));
        }
    }

    /**
     * Checks whether a prospective lake overlaps an existing lake.
     * @param x the lake center x value
     * @param z the lake center z value
     * @return {@code true} if the lake would overlap; otherwise {@code false}
     */
    private boolean overlapsLake(int x, int z) {
        for (Lake lake : lakes) {
            if (distance(x, z, lake.x, lake.z) < lake.radius * 2 + 3) return true;
        }
        return false;
    }

    /**
     * Selects valid positions for the configured trees.
     * @param random the random value
     */
    private void chooseTrees(Random random) {
        int requested = Math.max(0, TREE_COUNT);
        for (int attempts = 0; trees.size() < requested && attempts < requested * 200 + 200; attempts++) {
            int x = random.nextInt((int) ISLAND_RADIUS * 2 - 8) - (int) ISLAND_RADIUS + 4;
            int z = random.nextInt((int) ISLAND_RADIUS * 2 - 8) - (int) ISLAND_RADIUS + 4;
            int localX = Math.floorMod(x, Chunk.SIZE_X);
            int localZ = Math.floorMod(z, Chunk.SIZE_Z);
            if (distance(x, z, 0, 0) > ISLAND_RADIUS - 4 || lakeAt(x, z) != null || isLakeShore(x, z)
                    || nearTree(x, z) || localX < 2 || localX > 13 || localZ < 2 || localZ > 13) continue;
            trees.add(new Tree(x, terrainHeight(x, z), z));
        }
    }

    /**
     * Checks whether a position is too close to a selected tree.
     * @param x the world x value
     * @param z the world z value
     * @return {@code true} if a tree is nearby; otherwise {@code false}
     */
    private boolean nearTree(int x, int z) {
        for (Tree tree : trees) if (distance(x, z, tree.x, tree.z) < 5) return true;
        return false;
    }

    /**
     * Returns the lake occupying a world position.
     * @param x the world x value
     * @param z the world z value
     * @return the lake at the position, or {@code null} when none is present
     */
    private Lake lakeAt(int x, int z) {
        for (Lake lake : lakes) {
            if (distance(x, z, lake.x, lake.z) <= lakeBoundaryAt(lake, x, z)) return lake;
        }
        return null;
    }

    /**
     * Checks whether a position belongs to the conditional sand shore around a lake.
     * @param x the world x value
     * @param z the world z value
     * @return {@code true} if sand should be generated at the position; otherwise {@code false}
     */
    private boolean isLakeShore(int x, int z) {
        if (!GENERATE_LAKE_SAND_SHORES || lakeAt(x, z) != null) return false;
        for (Lake lake : lakes) {
            float distance = distance(x, z, lake.x, lake.z);
            float boundary = lakeBoundaryAt(lake, x, z);
            if (distance > boundary && distance <= boundary + LAKE_SHORE_WIDTH) return true;
        }
        return false;
    }

    /**
     * Returns the organic shoreline radius for a lake at a world position.
     * @param lake the lake value
     * @param x the world x value
     * @param z the world z value
     * @return the shoreline radius at the position
     */
    private float lakeBoundaryAt(Lake lake, int x, int z) {
        float organicity = Math.clamp(LAKE_ORGANICITY_PERCENT, 0, 100) / 100.0f;
        return lake.radius + noise(x, z, 3) * lake.radius * 0.55f * organicity;
    }

    /**
     * Registers generated lake blocks as fluid simulation sources.
     * @param chunk the generated chunk
     * @param chunkX the chunk x value
     * @param chunkZ the chunk z value
     */
    private void registerFluidSources(Chunk chunk, int chunkX, int chunkZ) {
        for (int x = 0; x < Chunk.SIZE_X; x++) for (int z = 0; z < Chunk.SIZE_Z; z++) {
            if (chunk.getBlock(x, SURFACE_Y, z) == fluidSimulation.getFluidType().getId()) {
                fluidSimulation.addSource(chunkX * Chunk.SIZE_X + x, SURFACE_Y,
                        chunkZ * Chunk.SIZE_Z + z);
            }
        }
    }

    /**
     * Generates every selected tree owned by a chunk.
     * @param chunkX the chunk x value
     * @param chunkZ the chunk z value
     * @param random the random value
     */
    private void generateTreesInChunk(int chunkX, int chunkZ, Random random) {
        for (Tree tree : trees) {
            if (Math.floorDiv(tree.x, Chunk.SIZE_X) == chunkX
                    && Math.floorDiv(tree.z, Chunk.SIZE_Z) == chunkZ) {
                generateTree(tree.x, tree.y, tree.z, random);
            }
        }
    }

    /**
     * Generates decorative plants around the center of the island.
     * @param random the random value
     */
    private void generatePlants(Random random) {
        for (int i = 0; i < PLANT_ATTEMPTS; i++) {
            int x = random.nextInt(14) - 7;
            int z = random.nextInt(14) - 7;
            int y = terrainHeight(x, z);
            if (lakeAt(x, z) != null || isLakeShore(x, z) || nearTree(x, z)) continue;
            BlockData[] plants = BlockData.allPlants();
            BlockData plant = plants[random.nextInt(plants.length)];
            if (plant != BlockData.OAK_BONSAI && world.getBlockTypeAt(x, y, z) == BlockData.GRASS.getId()
                    && world.getBlockTypeAt(x, y + 1, z) == BlockData.AIR.getId()) {
                world.setBlockTypeAt(x, y + 1, z, plant.getId());
                if (plant == BlockData.TALL_GRASS) generateCluster(x, y, z, plant, random);
            }
        }
    }

    /**
     * Generates a plant cluster around a surface position.
     * @param centerX the center x value
     * @param centerZ the center z value
     * @param plant the plant value
     * @param random the random value
     */
    public void generateCluster(int centerX, int centerZ, BlockData plant, Random random) {
        generateCluster(centerX, terrainHeight(centerX, centerZ), centerZ, plant, random);
    }

    /**
     * Generates a plant cluster at a known surface height.
     * @param centerX the center x value
     * @param surfaceY the center surface y value
     * @param centerZ the center z value
     * @param plant the plant value
     * @param random the random value
     */
    private void generateCluster(int centerX, int surfaceY, int centerZ, BlockData plant, Random random) {
        int placed = 0;
        for (int i = 0; i < 24 && placed < 8; i++) {
            int x = centerX + random.nextInt(5) - 2;
            int z = centerZ + random.nextInt(5) - 2;
            int y = terrainHeight(x, z);
            if (lakeAt(x, z) == null && !isLakeShore(x, z) && !nearTree(x, z)
                    && Math.abs(y - surfaceY) <= 2
                    && world.getBlockTypeAt(x, y, z) == BlockData.GRASS.getId()
                    && world.getBlockTypeAt(x, y + 1, z) == BlockData.AIR.getId()) {
                world.setBlockTypeAt(x, y + 1, z, plant.getId());
                placed++;
            }
        }
    }

    /**
     * Generates a tree on the highest solid block in a world column.
     * @param worldX the world x value
     * @param worldZ the world z value
     * @param random the random value
     */
    public static void generateTree(int worldX, int worldZ, Random random) {
        int surfaceY = findSurface(worldX, worldZ);
        generateTree(worldX, surfaceY, worldZ, random);
    }

    /**
     * Returns the highest solid surface in a world column.
     * @param x the world x value
     * @param z the world z value
     * @return the highest solid y value
     */
    private static int findSurface(int x, int z) {
        for (int y = Chunk.SIZE_Y - 2; y >= 0; y--) {
            byte block = world.getBlockTypeAt(x, y, z);
            BlockData data = BlockData.fromId(block);
            if (data != null && data != BlockData.AIR && !data.isFluid()) return y;
        }
        return SURFACE_Y;
    }

    /**
     * Generates a tree at a known surface height.
     * @param worldX the world x value
     * @param surfaceY the surface y value
     * @param worldZ the world z value
     * @param random the random value
     */
    private static void generateTree(int worldX, int surfaceY, int worldZ, Random random) {
        int trunkHeight = 4 + random.nextInt(5);
        for (int y = 1; y <= trunkHeight; y++)
            world.setBlockTypeAt(worldX, surfaceY + y, worldZ, BlockData.OAK_LOG.getId());

        int topY = surfaceY + trunkHeight;
        for (int y = topY - 2; y <= topY + 1; y++) {
            int radius = y == topY + 1 ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) == radius && Math.abs(dz) == radius && random.nextDouble() < 0.4) continue;
                if (world.getBlockTypeAt(worldX + dx, y, worldZ + dz) == BlockData.AIR.getId())
                    world.setBlockTypeAt(worldX + dx, y, worldZ + dz, BlockData.OAK_LEAVES.getId());
            }
        }
    }

    /**
     * Returns deterministic value noise for a world position.
     * @param x the world x value
     * @param z the world z value
     * @param scale the noise cell scale
     * @return a noise value between {@code -1} and {@code 1}
     */
    private float noise(int x, int z, int scale) {
        long value = seed + (long) Math.floorDiv(x, scale) * 341873128712L
                + (long) Math.floorDiv(z, scale) * 132897987541L;
        value = (value ^ value >>> 30) * 0xbf58476d1ce4e5b9L;
        value = (value ^ value >>> 27) * 0x94d049bb133111ebL;
        value ^= value >>> 31;
        return ((value & 0xffffL) / 32767.5f) - 1;
    }

    /**
     * Returns the horizontal distance between two positions.
     * @param x1 the first x value
     * @param z1 the first z value
     * @param x2 the second x value
     * @param z2 the second z value
     * @return the horizontal distance
     */
    private static float distance(int x1, int z1, int x2, int z2) {
        return (float) Math.hypot(x1 - x2, z1 - z2);
    }

    /** Stores the center and radius of a generated lake. */
    private record Lake(int x, int z, float radius) {}

    /** Stores the base position of a generated tree. */
    private record Tree(int x, int y, int z) {}
}
