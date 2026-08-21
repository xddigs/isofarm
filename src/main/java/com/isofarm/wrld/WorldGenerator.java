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
        boolean hasGrown = false;
        boolean isLarge = random.nextBoolean();
        int treeHeight = random.nextInt(3) + 3;
        if (isLarge) treeHeight += random.nextInt(2);
        for (int y = 1; y <= treeHeight; y++) {
            world.setBlockTypeAt(x, groundY + y, z, BlockData.OAK_LOG.getId());
            if (y == treeHeight) {
                hasGrown = true;
            }
        }

        if (hasGrown) {
            generateLeaves(x, groundY + treeHeight, z,
                    treeHeight, isLarge);
        }
    }

    private void generateLeaves(int x, int topY, int z,
                                int treeHeight, boolean isLarge) {
        int radius = isLarge ? 2 : 1;

        for (int y = -treeHeight; y <= 0; y++) {
            int distanceFromTop = -y;
            int currentRadius = Math.min(radius, distanceFromTop / 2 + 1);

            for (int dx = -currentRadius; dx <= currentRadius; dx++) {
                for (int dz = -currentRadius; dz <= currentRadius; dz++) {
                    int distance = Math.abs(dx) + Math.abs(dz);
                    if (distance > currentRadius + 1) continue;

                    if (Math.abs(dx) == currentRadius &&
                            Math.abs(dz) == currentRadius &&
                            random.nextDouble() < 0.4) {
                        continue;
                    }

                    if (random.nextDouble() < 0.008) continue;
                    if (dx == 0 && dz == 0 && y < 0) continue;
                    world.setBlockTypeAt(x + dx, topY + y, z + dz, BlockData.LEAVES.getId());
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