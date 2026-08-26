package com.isofarm.graphics;

import com.isofarm.data.BlockData;
import com.isofarm.utils.K;
import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.World;

public class ChunkMeshBuilder {
    private static final float PIXEL = 1.0f / K.World.DEFAULT_TEXTURE_SCALE;
    private static final float TILLED_HEIGHT = 1.0f - PIXEL;
    private static final BlockData[] BLOCK_LUT = new BlockData[256];
    private static final int MAX_FLOATS = Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z * 24;
    private static final int MAX_INDICES = Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z * 36;
    private static final ThreadLocal<float[]> POS_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_FLOATS]);
    private static final ThreadLocal<float[]> NORM_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_FLOATS]);
    private static final ThreadLocal<float[]> UV_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_FLOATS]);
    private static final ThreadLocal<float[]> WATER_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_FLOATS / 3]);
    private static final ThreadLocal<int[]> INDEX_BUFFER = ThreadLocal.withInitial(() -> new int[MAX_INDICES]);

    static {
        for (BlockData data : BlockData.values()) {
            BLOCK_LUT[data.getId() & 0xFF] = data;
        }
    }

    public record MeshData(
            float[] positions,
            float[] normals,
            float[] uv,
            float[] water,
            int[] indices
    ) {}

    public static MeshData buildMesh(World world, Chunk chunk) {
        int posIdx = 0;
        int uvIdx = 0;
        int normIdx = 0;
        int waterIdx = 0;
        int elemIdx = 0;
        int vertexCount = 0;

        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        float[] posBuffer = POS_BUFFER.get();
        float[] normBuffer = NORM_BUFFER.get();
        float[] uvBuffer = UV_BUFFER.get();
        float[] waterBuffer = WATER_BUFFER.get();
        int[] indexBuffer = INDEX_BUFFER.get();

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    byte blockId = chunk.getBlock(x, y, z);
                    if (blockId == 0) {
                        continue;
                    }

                    BlockData data = BLOCK_LUT[blockId & 0xFF];
                    if (data == null || data.isPlant()) {
                        continue;
                    }

                    int worldX = chunkX * Chunk.SIZE_X + x;
                    int worldZ = chunkZ * Chunk.SIZE_Z + z;
                    float bottomY = y;
                    float topY = (data == BlockData.TILLED_DIRT || data.isFluid()) ? y + TILLED_HEIGHT : y + 1.0f;
                    float waterValue = data.isFluid() ? 1.0f : 0.0f;

                    boolean renderFace;
                    float aboveBottomY = getBlockBottomY(world, worldX, y + 1, worldZ);

                    boolean renderTopFace;
                    if (data.isFluid()) {
                        renderTopFace = shouldRenderWaterTop(world, worldX, y, worldZ);
                    } else {
                        renderTopFace = shouldRenderFace(world, worldX, y + 1, worldZ, data) || aboveBottomY > topY;
                    }

                    if (renderTopFace) {
                        TextureAtlas.TextureRegion region = data.getTopRegion();
                        if (region != null) {
                            float uMin = region.uvMin().x;
                            float vMin = region.uvMin().y;
                            float uMax = region.uvMax().x;
                            float vMax = region.uvMax().y;

                            posIdx = addQuadPos(posBuffer, posIdx, x, topY, z + 1, x + 1, topY, z + 1, x + 1, topY, z, x, topY, z);
                            uvIdx = addQuadUV(uvBuffer, uvIdx, uMin, vMax, uMax, vMax, uMax, vMin, uMin, vMin);
                            normIdx = addQuadNorm(normBuffer, normIdx, 0, 1, 0);
                            waterIdx = addQuadWater(waterBuffer, waterIdx, waterValue);
                            elemIdx = addQuadIndices(indexBuffer, elemIdx, vertexCount);
                            vertexCount += 4;
                        }
                    }

                    if (y > 0 && !data.equals(BlockData.WATER)) {
                        renderFace = shouldRenderFace(world, worldX, y - 1, worldZ, data);
                        float belowTopY = getBlockTopY(world, worldX, y - 1, worldZ);

                        if (renderFace || (belowTopY < bottomY && belowTopY > 0)) {
                            TextureAtlas.TextureRegion region = data.getBottomRegion();
                            if (region != null) {
                                float uMin = region.uvMin().x;
                                float vMin = region.uvMin().y;
                                float uMax = region.uvMax().x;
                                float vMax = region.uvMax().y;

                                posIdx = addQuadPos(posBuffer, posIdx, x, bottomY, z, x + 1, bottomY, z, x + 1, bottomY, z + 1, x, bottomY, z + 1);
                                uvIdx = addQuadUV(uvBuffer, uvIdx, uMin, vMin, uMax, vMin, uMax, vMax, uMin, vMax);
                                normIdx = addQuadNorm(normBuffer, normIdx, 0, -1, 0);
                                waterIdx = addQuadWater(waterBuffer, waterIdx, waterValue);
                                elemIdx = addQuadIndices(indexBuffer, elemIdx, vertexCount);
                                vertexCount += 4;
                            }
                        }
                    }

                    renderFace = shouldRenderFace(world, worldX, y, worldZ + 1, data);
                    if (renderFace || isPartialSideExposure(world, worldX, y, worldZ + 1, data)) {
                        float expBottom = getSideBottomY(world, worldX, y, worldZ + 1, bottomY, data);
                        float uvB = calculateSideUvBottom(expBottom, bottomY, topY);
                        int newVertexCount = addSideQuadDirect(posBuffer, normBuffer, uvBuffer, waterBuffer, indexBuffer, posIdx, normIdx, uvIdx, waterIdx, elemIdx, vertexCount, x, x + 1, expBottom, topY, z + 1, z + 1, 0, 0, 1, data, uvB, 1.0f);

                        if (newVertexCount != vertexCount) {
                            vertexCount = newVertexCount;
                            posIdx += 12;
                            normIdx += 12;
                            uvIdx += 8;
                            waterIdx += 4;
                            elemIdx += 6;
                        }
                    }

                    renderFace = shouldRenderFace(world, worldX, y, worldZ - 1, data);
                    if (renderFace || isPartialSideExposure(world, worldX, y, worldZ - 1, data)) {
                        float expBottom = getSideBottomY(world, worldX, y, worldZ - 1, bottomY, data);
                        float uvB = calculateSideUvBottom(expBottom, bottomY, topY);
                        int newVertexCount = addSideQuadDirect(posBuffer, normBuffer, uvBuffer, waterBuffer, indexBuffer, posIdx, normIdx, uvIdx, waterIdx, elemIdx, vertexCount, x + 1, x, expBottom, topY, z, z, 0, 0, -1, data, uvB, 1.0f);

                        if (newVertexCount != vertexCount) {
                            vertexCount = newVertexCount;
                            posIdx += 12;
                            normIdx += 12;
                            uvIdx += 8;
                            waterIdx += 4;
                            elemIdx += 6;
                        }
                    }

                    renderFace = shouldRenderFace(world, worldX + 1, y, worldZ, data);
                    if (renderFace || isPartialSideExposure(world, worldX + 1, y, worldZ, data)) {
                        float expBottom = getSideBottomY(world, worldX + 1, y, worldZ, bottomY, data);
                        float uvB = calculateSideUvBottom(expBottom, bottomY, topY);
                        int newVertexCount = addSideQuadDirect(posBuffer, normBuffer, uvBuffer, waterBuffer, indexBuffer, posIdx, normIdx, uvIdx, waterIdx, elemIdx, vertexCount, x + 1, x + 1, expBottom, topY, z + 1, z, 1, 0, 0, data, uvB, 1.0f);

                        if (newVertexCount != vertexCount) {
                            vertexCount = newVertexCount;
                            posIdx += 12;
                            normIdx += 12;
                            uvIdx += 8;
                            waterIdx += 4;
                            elemIdx += 6;
                        }
                    }

                    renderFace = shouldRenderFace(world, worldX - 1, y, worldZ, data);
                    if (renderFace || isPartialSideExposure(world, worldX - 1, y, worldZ, data)) {
                        float expBottom = getSideBottomY(world, worldX - 1, y, worldZ, bottomY, data);
                        float uvB = calculateSideUvBottom(expBottom, bottomY, topY);
                        int newVertexCount = addSideQuadDirect(posBuffer, normBuffer, uvBuffer, waterBuffer, indexBuffer, posIdx, normIdx, uvIdx, waterIdx, elemIdx, vertexCount, x, x, expBottom, topY, z, z + 1, -1, 0, 0, data, uvB, 1.0f);

                        if (newVertexCount != vertexCount) {
                            vertexCount = newVertexCount;
                            posIdx += 12;
                            normIdx += 12;
                            uvIdx += 8;
                            waterIdx += 4;
                            elemIdx += 6;
                        }
                    }
                }
            }
        }

        float[] finalPos = new float[posIdx];
        float[] finalNorm = new float[normIdx];
        float[] finalUv = new float[uvIdx];
        float[] finalWater = new float[waterIdx];
        int[] finalIndices = new int[elemIdx];

        System.arraycopy(posBuffer, 0, finalPos, 0, posIdx);
        System.arraycopy(normBuffer, 0, finalNorm, 0, normIdx);
        System.arraycopy(uvBuffer, 0, finalUv, 0, uvIdx);
        System.arraycopy(waterBuffer, 0, finalWater, 0, waterIdx);
        System.arraycopy(indexBuffer, 0, finalIndices, 0, elemIdx);

        return new MeshData(finalPos, finalNorm, finalUv, finalWater, finalIndices);
    }

    private static int addQuadWater(float[] buf, int idx, float value) {
        buf[idx] = value;
        buf[idx + 1] = value;
        buf[idx + 2] = value;
        buf[idx + 3] = value;
        return idx + 4;
    }

    public static Mesh createMesh(MeshData data) {
        return new Mesh(data.positions(), data.normals(), data.uv(), data.water(), data.indices());
    }

    private static float getBlockTopY(BlockData data, float y) {
        return (data == BlockData.TILLED_DIRT || data.isFluid()) ? y + TILLED_HEIGHT : y + 1.0f;
    }

    private static float getBlockBottomY(World world, int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.SIZE_Y || !world.isChunkLoadedAt(worldX, worldZ)) {
            return 0.0f;
        }

        byte blockId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (blockId == 0) {
            return 0.0f;
        }

        BlockData data = BLOCK_LUT[blockId & 0xFF];
        if (data == null) {
            return 0.0f;
        }
        return worldY;
    }

    private static float getBlockTopY(World world, int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.SIZE_Y || !world.isChunkLoadedAt(worldX, worldZ)) {
            return 0.0f;
        }

        byte blockId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (blockId == 0) {
            return 0.0f;
        }

        BlockData data = BLOCK_LUT[blockId & 0xFF];
        if (data == null) {
            return 0.0f;
        }

        return getBlockTopY(data, worldY);
    }

    private static boolean shouldRenderFace(World world, int worldX, int worldY, int worldZ, BlockData currentBlock) {
        if (worldY < 0) {
            return false;
        }

        if (worldY >= Chunk.SIZE_Y) {
            return true;
        }

        if (!world.isChunkLoadedAt(worldX, worldZ)) {
            return false;
        }

        byte neighborId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (neighborId == 0) {
            return true;
        }

        BlockData neighborData = BLOCK_LUT[neighborId & 0xFF];
        if (neighborData == null) {
            return true;
        }

        if (currentBlock.isFluid()) {
            if (neighborData.isFluid()) {
                return false;
            }
            return neighborData.isTransparent() && !neighborData.isSolid();
        }

        if (neighborData.isFluid()) {
            return true;
        }

        return neighborData.isTransparent() && neighborData != currentBlock;
    }

    private static boolean shouldRenderWaterTop(World world, int x, int y, int z) {
        byte aboveId = world.getBlockTypeAt(x, y + 1, z);
        if (aboveId == BlockData.AIR.getId()) return true;
        BlockData above = BLOCK_LUT[aboveId & 0xFF];
        if (above == null) return true;
        if (above == BlockData.WATER) return false;
        if (above.isSolid()) return false;
        return true;
    }

    private static boolean isPartialSideExposure(World world, int worldX, int worldY, int worldZ, BlockData currentBlock) {
        if (currentBlock.isFluid()) {
            return false;
        }

        if (!world.isChunkLoadedAt(worldX, worldZ) || worldY < 0 || worldY >= Chunk.SIZE_Y) {
            return false;
        }

        byte neighborId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (neighborId == 0) {
            return false;
        }

        BlockData neighborData = BLOCK_LUT[neighborId & 0xFF];
        if (neighborData == null) {
            return false;
        }

        if (neighborData == BlockData.TILLED_DIRT && currentBlock != BlockData.TILLED_DIRT) {
            return true;
        }
        return false;
    }

    private static float getSideBottomY(World world, int worldX, int worldY, int worldZ, float currentBottomY, BlockData currentBlock) {
        if (!world.isChunkLoadedAt(worldX, worldZ) || worldY < 0 || worldY >= Chunk.SIZE_Y) {
            return currentBottomY;
        }

        byte neighborId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (neighborId == 0) {
            return currentBottomY;
        }

        BlockData neighborData = BLOCK_LUT[neighborId & 0xFF];
        if (neighborData == null) {
            return currentBottomY;
        }

        if (neighborData == BlockData.TILLED_DIRT && currentBlock != BlockData.TILLED_DIRT) {
            return currentBottomY + TILLED_HEIGHT;
        }

        return currentBottomY;
    }

    private static float calculateSideUvBottom(float expBottom, float bottomY, float topY) {
        if (topY <= bottomY) {
            return 0.0f;
        }
        float exposedHeight = topY - expBottom;
        if (exposedHeight <= 0.0f) {
            return 1.0f;
        }
        float totalHeight = topY - bottomY;
        return Math.clamp(1.0f - (exposedHeight / totalHeight), 0.0f, 1.0f);
    }

    private static int addQuadPos(float[] buf, int idx, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        buf[idx] = x1;
        buf[idx + 1] = y1;
        buf[idx + 2] = z1;

        buf[idx + 3] = x2;
        buf[idx + 4] = y2;
        buf[idx + 5] = z2;

        buf[idx + 6] = x3;
        buf[idx + 7] = y3;
        buf[idx + 8] = z3;

        buf[idx + 9] = x4;
        buf[idx + 10] = y4;
        buf[idx + 11] = z4;

        return idx + 12;
    }

    private static int addQuadUV(float[] buf, int idx, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4) {
        buf[idx] = u1;
        buf[idx + 1] = v1;

        buf[idx + 2] = u2;
        buf[idx + 3] = v2;

        buf[idx + 4] = u3;
        buf[idx + 5] = v3;

        buf[idx + 6] = u4;
        buf[idx + 7] = v4;

        return idx + 8;
    }

    private static int addQuadNorm(float[] buf, int idx, float nx, float ny, float nz) {
        for (int i = 0; i < 4; i++) {
            buf[idx++] = nx;
            buf[idx++] = ny;
            buf[idx++] = nz;
        }
        return idx;
    }

    private static int addQuadIndices(int[] buf, int idx, int vertexCount) {
        buf[idx] = vertexCount;
        buf[idx + 1] = vertexCount + 1;
        buf[idx + 2] = vertexCount + 2;

        buf[idx + 3] = vertexCount + 2;
        buf[idx + 4] = vertexCount + 3;
        buf[idx + 5] = vertexCount;

        return idx + 6;
    }

    private static int addSideQuadDirect(float[] pos, float[] norm, float[] uv, float[] water, int[] idx, int posI, int normI, int uvI, int waterI, int elemI, int vertexCount, float x1, float x2, float y1, float y2, float z1, float z2, float nx, float ny, float nz, BlockData data, float uvB, float uvT) {
        TextureAtlas.TextureRegion region = data.getSideRegion();
        if (region == null) {
            return vertexCount;
        }

        addQuadPos(pos, posI, x1, y1, z1, x2, y1, z2, x2, y2, z2, x1, y2, z1);

        float u1 = region.uvMin().x;
        float u2 = region.uvMax().x;
        float heightUV = region.uvMax().y - region.uvMin().y;
        float v1 = region.uvMin().y + heightUV * uvB;
        float v2 = region.uvMin().y + heightUV * uvT;

        addQuadUV(uv, uvI, u1, v1, u2, v1, u2, v2, u1, v2);
        addQuadNorm(norm, normI, nx, ny, nz);
        addQuadWater(water, waterI, data.isFluid() ? 1.0f : 0.0f);
        addQuadIndices(idx, elemI, vertexCount);

        return vertexCount + 4;
    }
}