package com.isofarm.graphics;

import com.isofarm.data.BlockData;
import com.isofarm.utils.K;
import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.World;

public class ChunkMeshBuilder {

    private static final float PIXEL = 1.0f / K.World.DEFAULT_TEXTURE_SCALE;
    private static final float TILLED_HEIGHT = 1.0f - PIXEL;
    private static final BlockData[] BLOCK_LUT = new BlockData[256];
    static {
        for (BlockData data : BlockData.values()) {
            BLOCK_LUT[data.getId() & 0xFF] = data;
        }
    }

    private static final int MAX_FLOATS = Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z * 24;
    private static final int MAX_INDICES = Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z * 36;

    private static final float[] posBuffer = new float[MAX_FLOATS];
    private static final float[] normBuffer = new float[MAX_FLOATS];
    private static final float[] uvBuffer = new float[MAX_FLOATS];
    private static final int[] indexBuffer = new int[MAX_INDICES];

    public static synchronized Mesh buildMesh(World world, Chunk chunk) {
        int posIdx = 0;
        int uvIdx = 0;
        int normIdx = 0;
        int elemIdx = 0;
        int vertexCount = 0;

        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    byte blockId = chunk.getBlock(x, y, z);
                    if (blockId == 0) continue;

                    BlockData data = BLOCK_LUT[blockId & 0xFF];
                    if (data == null || data == BlockData.CROP) continue;

                    int worldX = chunkX * Chunk.SIZE_X + x;
                    int worldZ = chunkZ * Chunk.SIZE_Z + z;

                    float bottomY = (float) y;
                    float topY = (data == BlockData.TILLED_DIRT) ? (float) y + TILLED_HEIGHT : (float) y + 1.0f;

                    boolean renderFace = shouldRenderFace(world, worldX, y + 1, worldZ, data);
                    float aboveBottomY = getBlockBottomY(world, worldX, y + 1, worldZ);
                    if (renderFace || aboveBottomY > topY) {
                        float uMin = data.getTopAtlasOffset().x;
                        float vMin = data.getTopAtlasOffset().y;
                        float uMax = uMin + data.getAtlasScale().x;
                        float vMax = vMin + data.getAtlasScale().y;

                        posIdx = addQuadPos(posBuffer, posIdx, (float) x, topY, (float) z + 1, (float) x + 1, topY, (float) z + 1, (float) x + 1, topY, (float) z, (float) x, topY, (float) z);
                        uvIdx = addQuadUV(uvBuffer, uvIdx, uMin, vMax, uMax, vMax, uMax, vMin, uMin, vMin);
                        normIdx = addQuadNorm(normBuffer, normIdx, 0, 1, 0);
                        elemIdx = addQuadIndices(indexBuffer, elemIdx, vertexCount);
                        vertexCount += 4;
                    }

                    if (y > 0) {
                        renderFace = shouldRenderFace(world, worldX, y - 1, worldZ, data);
                        float belowTopY = getBlockTopY(world, worldX, y - 1, worldZ);
                        if (renderFace || (belowTopY < bottomY && belowTopY > 0)) {
                            float uMin = data.getBottomAtlasOffset().x;
                            float vMin = data.getBottomAtlasOffset().y;
                            float uMax = uMin + data.getAtlasScale().x;
                            float vMax = vMin + data.getAtlasScale().y;

                            posIdx = addQuadPos(posBuffer, posIdx, (float) x, bottomY, (float) z, (float) x + 1, bottomY, (float) z, (float) x + 1, bottomY, (float) z + 1, (float) x, bottomY, (float) z + 1);
                            uvIdx = addQuadUV(uvBuffer, uvIdx, uMin, vMin, uMax, vMin, uMax, vMax, uMin, vMax);
                            normIdx = addQuadNorm(normBuffer, normIdx, 0, -1, 0);
                            elemIdx = addQuadIndices(indexBuffer, elemIdx, vertexCount);
                            vertexCount += 4;
                        }
                    }

                    renderFace = shouldRenderFace(world, worldX, y, worldZ + 1, data);
                    float neighborTop = getBlockTopY(world, worldX, y, worldZ + 1);
                    if (renderFace) {
                        float expBottom = Math.max(bottomY, neighborTop);
                        float uvB = (neighborTop > bottomY) ? (neighborTop - bottomY) / (topY - bottomY) : 0.0f;

                        vertexCount = addSideQuadDirect(posBuffer, normBuffer, uvBuffer, indexBuffer,
                                posIdx, normIdx, uvIdx, elemIdx, vertexCount,
                                (float) x, (float) x + 1, expBottom, topY, (float) z + 1, (float) z + 1, 0, 0, 1, data, uvB, 1.0f);
                        posIdx += 12; normIdx += 12; uvIdx += 8; elemIdx += 6;
                    }

                    renderFace = shouldRenderFace(world, worldX, y, worldZ - 1, data);
                    neighborTop = getBlockTopY(world, worldX, y, worldZ - 1);
                    if (renderFace) {
                        float expBottom = Math.max(bottomY, neighborTop);
                        float uvB = (neighborTop > bottomY) ? (neighborTop - bottomY) / (topY - bottomY) : 0.0f;

                        vertexCount = addSideQuadDirect(posBuffer, normBuffer, uvBuffer, indexBuffer,
                                posIdx, normIdx, uvIdx, elemIdx, vertexCount,
                                (float) x + 1, (float) x, expBottom, topY, (float) z, (float) z, 0, 0, -1, data, uvB, 1.0f);
                        posIdx += 12; normIdx += 12; uvIdx += 8; elemIdx += 6;
                    }

                    renderFace = shouldRenderFace(world, worldX + 1, y, worldZ, data);
                    neighborTop = getBlockTopY(world, worldX + 1, y, worldZ);
                    if (renderFace) {
                        float expBottom = Math.max(bottomY, neighborTop);
                        float uvB = (neighborTop > bottomY) ? (neighborTop - bottomY) / (topY - bottomY) : 0.0f;

                        vertexCount = addSideQuadDirect(posBuffer, normBuffer, uvBuffer, indexBuffer,
                                posIdx, normIdx, uvIdx, elemIdx, vertexCount,
                                (float) x + 1, (float) x + 1, expBottom, topY, (float) z + 1, (float) z, 1, 0, 0, data, uvB, 1.0f);
                        posIdx += 12; normIdx += 12; uvIdx += 8; elemIdx += 6;
                    }

                    renderFace = shouldRenderFace(world, worldX - 1, y, worldZ, data);
                    neighborTop = getBlockTopY(world, worldX - 1, y, worldZ);
                    if (renderFace) {
                        float expBottom = Math.max(bottomY, neighborTop);
                        float uvB = (neighborTop > bottomY) ? (neighborTop - bottomY) / (topY - bottomY) : 0.0f;

                        vertexCount = addSideQuadDirect(posBuffer, normBuffer, uvBuffer, indexBuffer,
                                posIdx, normIdx, uvIdx, elemIdx, vertexCount,
                                (float) x, (float) x, expBottom, topY, (float) z, (float) z + 1, -1, 0, 0, data, uvB, 1.0f);
                        posIdx += 12; normIdx += 12; uvIdx += 8; elemIdx += 6;
                    }
                }
            }
        }

        float[] finalPos = new float[posIdx];
        float[] finalNorm = new float[normIdx];
        float[] finalUv = new float[uvIdx];
        int[] finalIndices = new int[elemIdx];

        System.arraycopy(posBuffer, 0, finalPos, 0, posIdx);
        System.arraycopy(normBuffer, 0, finalNorm, 0, normIdx);
        System.arraycopy(uvBuffer, 0, finalUv, 0, uvIdx);
        System.arraycopy(indexBuffer, 0, finalIndices, 0, elemIdx);

        return new Mesh(finalPos, finalNorm, finalUv, finalIndices);
    }

    private static float getBlockTopY(BlockData data, float y) {
        return (data == BlockData.TILLED_DIRT) ? y + TILLED_HEIGHT : y + 1.0f;
    }

    private static float getBlockBottomY(World world, int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.SIZE_Y) {
            return 0.0f;
        }

        if (!world.isChunkLoadedAt(worldX, worldZ)) {
            return 0.0f;
        }
        byte blockId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (blockId == 0) return 0.0f;
        BlockData data = BLOCK_LUT[blockId & 0xFF];
        if (data == null || data == BlockData.CROP) {
            return 0.0f;
        }
        return worldY;
    }

    private static float getBlockTopY(World world, int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.SIZE_Y) {
            return 0.0f;
        }

        if (!world.isChunkLoadedAt(worldX, worldZ)) {
            return 0.0f;
        }

        byte blockId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (blockId == 0) return 0.0f;
        BlockData data = BLOCK_LUT[blockId & 0xFF];
        if (data == null || data == BlockData.CROP) {
            return 0.0f;
        }
        return getBlockTopY(data, worldY);
    }

    private static boolean shouldRenderFace(World world, int worldX, int worldY,
                                            int worldZ, BlockData currentBlock) {
        if (worldY < 0) return false;
        if (worldY >= Chunk.SIZE_Y) return true;
        if (!world.isChunkLoadedAt(worldX, worldZ)) return false;

        byte neighborId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (neighborId == 0) return true;

        BlockData neighborData = BLOCK_LUT[neighborId & 0xFF];
        if (neighborData == null) return true;

        return neighborData.isTransparent() && neighborData != currentBlock;
    }

    private static int addQuadPos(float[] buf, int idx, float x1, float y1, float z1,
                                  float x2, float y2, float z2, float x3, float y3, float z3,
                                  float x4, float y4, float z4) {
        buf[idx] = x1; buf[idx+1] = y1; buf[idx+2] = z1;
        buf[idx+3] = x2; buf[idx+4] = y2; buf[idx+5] = z2;
        buf[idx+6] = x3; buf[idx+7] = y3; buf[idx+8] = z3;
        buf[idx+9] = x4; buf[idx+10] = y4; buf[idx+11] = z4;
        return idx + 12;
    }

    private static int addQuadUV(float[] buf, int idx, float u1, float v1, float u2,
                                 float v2, float u3, float v3, float u4, float v4) {
        buf[idx] = u1; buf[idx+1] = v1;
        buf[idx+2] = u2; buf[idx+3] = v2;
        buf[idx+4] = u3; buf[idx+5] = v3;
        buf[idx+6] = u4; buf[idx+7] = v4;
        return idx + 8;
    }

    private static int addQuadNorm(float[] buf, int idx, float nx, float ny, float nz) {
        for (int i = 0; i < 4; i++) {
            buf[idx++] = nx; buf[idx++] = ny; buf[idx++] = nz;
        }
        return idx;
    }

    private static int addQuadIndices(int[] buf, int idx, int vertexCount) {
        buf[idx] = vertexCount; buf[idx+1] = vertexCount + 1; buf[idx+2] = vertexCount + 2;
        buf[idx+3] = vertexCount + 2; buf[idx+4] = vertexCount + 3; buf[idx+5] = vertexCount;
        return idx + 6;
    }

    private static int addSideQuadDirect(float[] pos, float[] norm, float[] uv, int[] idx,
                                         int posI, int normI, int uvI, int elemI, int vertexCount,
                                         float x1, float x2, float y1, float y2, float z1, float z2,
                                         float nx, float ny, float nz, BlockData data, float uvB, float uvT) {
        addQuadPos(pos, posI, x1, y1, z1, x2, y1, z2, x2, y2, z2, x1, y2, z1);

        float u1 = data.getSideAtlasOffset().x;
        float u2 = u1 + data.getAtlasScale().x;
        float v1 = data.getSideAtlasOffset().y + data.getAtlasScale().y * uvB;
        float v2 = data.getSideAtlasOffset().y + data.getAtlasScale().y * uvT;
        addQuadUV(uv, uvI, u1, v1, u2, v1, u2, v2, u1, v2);

        addQuadNorm(norm, normI, nx, ny, nz);
        addQuadIndices(idx, elemI, vertexCount);

        return vertexCount + 4;
    }
}