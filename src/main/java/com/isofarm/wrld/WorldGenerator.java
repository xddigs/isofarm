package com.isofarm.wrld;

import com.isofarm.data.BlockData;
import org.joml.SimplexNoise;

import java.util.Random;

public class WorldGenerator {
    private static final Random random = new Random();
    private static final float NOISE_SCALE = 0.03f;
    private static final int BASE_HEIGHT = 20;
    private static final int HEIGHT_VARIATION = 5;
    private final World world;

    public WorldGenerator(World world) {
        this.world = world;
    }

    public void generateChunk(int chunkX, int chunkZ) {
        Chunk chunk = world.getOrCreateChunk(chunkX, chunkZ);

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int worldX = chunkX * Chunk.SIZE_X + x;
                int worldZ = chunkZ * Chunk.SIZE_Z + z;

                float noise = SimplexNoise.noise(worldX * NOISE_SCALE, worldZ * NOISE_SCALE);
                int height = (int) (BASE_HEIGHT + (noise * HEIGHT_VARIATION));
                height = Math.clamp(height, 1, Chunk.SIZE_Y - 1);

                for (int y = 0; y <= height; y++) {
                    byte blockId;
                    if (y == height) {
                        blockId = BlockData.GRASS.getId();
                    } else if (y > height - 3) {
                        blockId = BlockData.DIRT.getId();
                    } else {
                        blockId = BlockData.STONE.getId();
                    }
                    chunk.setBlock(x, y, z, blockId);
                }

                if (random.nextDouble() < 0.002 && !hasTreeNearby(worldX, worldZ, height)) {
                    generateTree(worldX, height, worldZ);
                }
            }
        }
    }

    private void generateTree(int x, int groundY, int z) {
        int treeHeight = random.nextInt(3) + 3;
        boolean largeTree = random.nextDouble() < 0.25;
        if (largeTree) treeHeight += random.nextInt(3) + 2;
        for (int y = 1; y <= treeHeight; y++) {
            world.setBlockTypeAt(x, groundY + y, z, BlockData.OAK_LOG.getId());
        }

        generateLeaves(x, groundY + treeHeight, z, largeTree);
    }

    private void generateLeaves(int x, int topY, int z, boolean largeTree) {
        int radius = largeTree ? 2 : 1;

        for (int y = -2; y <= 1; y++) {
            int currentRadius = radius;
            if (y == 1) {
                currentRadius = 1;
            }

            for (int dx = -currentRadius; dx <= currentRadius; dx++) {
                for (int dz = -currentRadius; dz <= currentRadius; dz++) {
                    if (Math.abs(dx) == currentRadius && Math.abs(dz) == currentRadius
                            && random.nextDouble() < 0.5) {
                        continue;
                    }

                    if (dx == 0 && dz == 0 && y <= 0) continue;
                    if (random.nextDouble() < 0.10) continue;

                    world.setBlockTypeAt(x + dx, topY + y, z + dz,
                            BlockData.LEAVES.getId());
                }
            }
        }
    }

    private boolean hasTreeNearby(int worldX, int worldZ, int height) {
        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;
            int offsetX = (i % 3) - 1;
            int offsetZ = (i / 3) - 1;
            int checkX = worldX + offsetX;
            int checkZ = worldZ + offsetZ;

            for (int y = Math.max(0, height - 1); y <= height + 6; y++) {
                byte block = world.getBlockTypeAt(checkX, y, checkZ);
                if (block == BlockData.OAK_LOG.getId()) {
                    return true;
                }
            }
        }
        return false;
    }
}