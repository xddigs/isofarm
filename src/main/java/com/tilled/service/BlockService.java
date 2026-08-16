package com.tilled.service;

import com.tilled.data.Block;
import com.tilled.data.BlockData;
import com.tilled.graphics.Mesh;
import com.tilled.graphics.Shader;
import com.tilled.graphics.Sunlight;
import com.tilled.utils.K;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class BlockService implements Service<Block> {
    private final Map<String, Block> blocks = new HashMap<>();

    public BlockService() {}

    public void setBlock(BlockData type, int x, int z) {
        blocks.put(getKey(x, z), new Block(type, x, z));
    }

    public Block find(int x, int z) {
        return blocks.get(getKey(x, z));
    }

    public boolean isEmpty(int x, int z) {
        return find(x, z) == null;
    }

    public boolean isUnlocked(int x, int z) {
        Block block = find(x, z);
        return block != null && block.isUnlocked();
    }

    public boolean unlockBlock(int x, int z) {
        Block block = find(x, z);
        if (block == null || block.isUnlocked()) return false;

        if (!hasUnlockedNeighbor(x, z)) return false;

        block.setUnlocked(true);
        return true;
    }

    public boolean expandBlock(int x, int z) {
        if (blocks.containsKey(getKey(x, z))) {
            return false;
        }

        if (!hasUnlockedNeighbor(x, z)) {
            return false;
        }

        Block block = new Block(BlockData.TILLED_DIRT, x, z);
        block.setUnlocked(true);
        blocks.put(getKey(x, z), block);
        return true;
    }

    public void renderAll(Shader shader, Mesh blockMesh, Matrix4f modelMatrix, Sunlight sunlight) {
        for (Block block : blocks.values()) {
            BlockData blockData = block.getType();

            if (!block.isUnlocked()) {
                shader.setUniform("uBaseColor", K.Colors.CELL_BLOCKED);
            } else {
                shader.setUniform("uLightDirection", sunlight.getDirection());
                shader.setUniform("uLightColor", sunlight.getColor());
                shader.setUniform("uLightIntensity", sunlight.getIntensity());
            }

            shader.setUniform("uAtlasScale", blockData.getAtlasScale());
            shader.setUniform("uTopAtlasOffset", blockData.getTopAtlasOffset());
            shader.setUniform("uBottomAtlasOffset", blockData.getBottomAtlasOffset());
            shader.setUniform("uSideAtlasOffset", blockData.getSideAtlasOffset());

            float worldX = block.getX() * K.World.TILE_SIZE;
            float worldZ = block.getZ() * K.World.TILE_SIZE;

            modelMatrix.identity()
                    .translate(new Vector3f(worldX, 0.0f, worldZ))
                    .scale(K.World.TILE_SIZE);

            shader.setUniform("uModel", modelMatrix);
            blockMesh.render();
        }
    }

    private boolean hasUnlockedNeighbor(int x, int z) {
        return isUnlocked(x + 1, z) ||
                isUnlocked(x - 1, z) ||
                isUnlocked(x, z + 1) ||
                isUnlocked(x, z - 1);
    }

    private String getKey(int x, int z) {
        return x + "," + z;
    }
}