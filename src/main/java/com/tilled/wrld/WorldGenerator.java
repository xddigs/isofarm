package com.tilled.wrld;

import com.tilled.data.BlockData;
import org.joml.SimplexNoise;

public class WorldGenerator {
    private final World world;
    private static final float NOISE_SCALE = 0.03f;
    private static final int BASE_HEIGHT = 20;
    private static final int HEIGHT_VARIATION = 10;

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
            }
        }
    }
}