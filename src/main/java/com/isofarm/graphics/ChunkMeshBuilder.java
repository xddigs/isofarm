package com.isofarm.graphics;

import com.isofarm.data.BlockData;
import com.isofarm.utils.K;
import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.World;

/**
 * Provides chunk mesh builder behavior.
 */
public class ChunkMeshBuilder {
    private static final float PIXEL = 1.0f / K.World.DEFAULT_TEXTURE_SCALE;
    private static final float TILLED_HEIGHT = 1.0f - PIXEL;
    private static final BlockData[] BLOCK_LUT = new BlockData[256];
    private static final int MAX_POSITION_FLOATS = Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z * 72;
    private static final int MAX_NORMAL_FLOATS = Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z * 72;
    private static final int MAX_UV_FLOATS = Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z * 48;
    private static final int MAX_INDICES = Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z * 36;
    private static final ThreadLocal<float[]> POS_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_POSITION_FLOATS]);
    private static final ThreadLocal<float[]> NORMAL_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_NORMAL_FLOATS]);
    private static final ThreadLocal<float[]> UV_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_UV_FLOATS]);
    private static final ThreadLocal<int[]> INDEX_BUFFER = ThreadLocal.withInitial(() -> new int[MAX_INDICES]);
    private static final ThreadLocal<float[]> WATER_POS_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_POSITION_FLOATS]);
    private static final ThreadLocal<float[]> WATER_NORMAL_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_NORMAL_FLOATS]);
    private static final ThreadLocal<float[]> WATER_UV_BUFFER = ThreadLocal.withInitial(() -> new float[MAX_UV_FLOATS]);
    private static final ThreadLocal<int[]> WATER_INDEX_BUFFER = ThreadLocal.withInitial(() -> new int[MAX_INDICES]);

    static {
        for (BlockData data : BlockData.values()) {
            BLOCK_LUT[data.getId() & 0xFF] = data;
        }
    }

    /**
     * Stores raw mesh data data.
     */
    public record RawMeshData(float[] positions, float[] normals, float[] uv, int[] indices) {}
    /**
     * Stores chunk mesh data data.
     */
    public record ChunkMeshData(RawMeshData solidData, RawMeshData waterData) {}

    /**
     * Stores chunk render mesh data.
     */
    public record ChunkRenderMesh(Mesh solidMesh, Mesh waterMesh) {
        /**
         * Performs the dispose operation.
         */
        public void dispose() {
            if (solidMesh != null) solidMesh.dispose();
            if (waterMesh != null) waterMesh.dispose();
        }
    }

    /**
     * Performs the build mesh operation.
     * @param world the world value
     * @param chunk the chunk value
     * @return the build mesh result
     */
    public static ChunkMeshData buildMesh(World world, Chunk chunk) {
        int posIdx = 0, normIdx = 0, uvIdx = 0, elemIdx = 0, vertexCount = 0;
        int wPosIdx = 0, wNormIdx = 0, wUvIdx = 0, wElemIdx = 0, wVertexCount = 0;

        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        float[] posBuf = POS_BUFFER.get();
        float[] normBuf = NORMAL_BUFFER.get();
        float[] uvBuf = UV_BUFFER.get();
        int[] idxBuf = INDEX_BUFFER.get();

        float[] wPosBuf = WATER_POS_BUFFER.get();
        float[] wNormBuf = WATER_NORMAL_BUFFER.get();
        float[] wUvBuf = WATER_UV_BUFFER.get();
        int[] wIdxBuf = WATER_INDEX_BUFFER.get();

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    byte blockId = chunk.getBlock(x, y, z);
                    if (blockId == 0) continue;

                    BlockData data = BLOCK_LUT[blockId & 0xFF];
                    if (data == null || data.isPlant()) continue;

                    int worldX = chunkX * Chunk.SIZE_X + x;
                    int worldZ = chunkZ * Chunk.SIZE_Z + z;
                    float bottomY = y;
                    boolean isWater = data.isFluid();
                    float topY;
                    if (data.isFluid()) {
                        byte level = chunk.getFluidLevel(x, y, z);
                        int waterLevel = (level <= 0) ? 8 : level;
                        boolean hasWaterAbove = y < Chunk.SIZE_Y - 1
                                && chunk.getBlock(x, y + 1, z) == blockId;
                        topY = hasWaterAbove ? (y + 1.0f) : (y + (waterLevel / 8.0f) * TILLED_HEIGHT);
                    } else {
                        topY = y + 1.0f;
                    }

                    boolean renderTopFace = isWater
                            ? shouldRenderWaterTop(world, worldX, y, worldZ, data)
                            : (shouldRenderFace(world, worldX, y + 1, worldZ, data) || getBlockBottomY(world, worldX, y + 1, worldZ) > topY);

                    if (renderTopFace) {
                        TextureAtlas.TextureRegion region = data.getTopRegion();
                        if (region != null) {
                            if (isWater) {
                                float y00 = getWaterCornerHeight(world, worldX, y, worldZ, data);
                                float y10 = getWaterCornerHeight(world, worldX + 1, y, worldZ, data);
                                float y11 = getWaterCornerHeight(world, worldX + 1, y, worldZ + 1, data);
                                float y01 = getWaterCornerHeight(world, worldX, y, worldZ + 1, data);

                                wPosIdx = addQuadPos(wPosBuf, wPosIdx, x, y01, z + 1, x + 1, y11, z + 1, x + 1, y10, z, x, y00, z);

                                wUvIdx = addQuadUV(wUvBuf, wUvIdx, region.uvMin().x, region.uvMax().y, region.uvMax().x, region.uvMax().y, region.uvMax().x, region.uvMin().y, region.uvMin().x, region.uvMin().y);
                                wNormIdx = addQuadNorm(wNormBuf, wNormIdx, 0, 1, 0);
                                wElemIdx = addQuadIndices(wIdxBuf, wElemIdx, wVertexCount);
                                wVertexCount += 4;
                            } else {
                                posIdx = addQuadPos(posBuf, posIdx, x, topY, z + 1, x + 1, topY, z + 1, x + 1, topY, z, x, topY, z);
                                uvIdx = addQuadUV(uvBuf, uvIdx, region.uvMin().x, region.uvMax().y, region.uvMax().x, region.uvMax().y, region.uvMax().x, region.uvMin().y, region.uvMin().x, region.uvMin().y);
                                normIdx = addQuadNorm(normBuf, normIdx, 0, 1, 0);
                                elemIdx = addQuadIndices(idxBuf, elemIdx, vertexCount);
                                vertexCount += 4;
                            }
                        }
                    }

                    if (y > 0 && !isWater) {
                        boolean renderFace = shouldRenderFace(world, worldX, y - 1, worldZ, data);
                        float belowTopY = getBlockTopY(world, worldX, y - 1, worldZ);
                        if (renderFace || (belowTopY < bottomY && belowTopY > 0)) {
                            TextureAtlas.TextureRegion region = data.getBottomRegion();
                            if (region != null) {
                                posIdx = addQuadPos(posBuf, posIdx, x, bottomY, z, x + 1, bottomY, z, x + 1, bottomY, z + 1, x, bottomY, z + 1);
                                uvIdx = addQuadUV(uvBuf, uvIdx, region.uvMin().x, region.uvMin().y, region.uvMax().x, region.uvMin().y, region.uvMax().x, region.uvMax().y, region.uvMin().x, region.uvMax().y);
                                normIdx = addQuadNorm(normBuf, normIdx, 0, -1, 0);
                                elemIdx = addQuadIndices(idxBuf, elemIdx, vertexCount);
                                vertexCount += 4;
                            }
                        }
                    }

                    if (shouldRenderFace(world, worldX, y, worldZ + 1, data) || isPartialSideExposure(world, worldX, y, worldZ + 1, data)) {
                        float expBottom = getSideBottomY(world, worldX, y, worldZ + 1, bottomY, data);
                        float uvB = calculateSideUvBottom(expBottom, bottomY, topY);
                        if (isWater) {
                            int next = addSideQuadDirect(wPosBuf, wNormBuf, wUvBuf, wIdxBuf, wPosIdx, wNormIdx, wUvIdx, wElemIdx, wVertexCount, x, x + 1, expBottom, topY, z + 1, z + 1, 0, 0, 1, data, uvB, 1.0f);
                            if (next != wVertexCount) { wVertexCount = next; wPosIdx += 12; wNormIdx += 12; wUvIdx += 8; wElemIdx += 6; }
                        } else {
                            int next = addSideQuadDirect(posBuf, normBuf, uvBuf, idxBuf, posIdx, normIdx, uvIdx, elemIdx, vertexCount, x, x + 1, expBottom, topY, z + 1, z + 1, 0, 0, 1, data, uvB, 1.0f);
                            if (next != vertexCount) { vertexCount = next; posIdx += 12; normIdx += 12; uvIdx += 8; elemIdx += 6; }
                        }
                    }

                    if (shouldRenderFace(world, worldX, y, worldZ - 1, data) || isPartialSideExposure(world, worldX, y, worldZ - 1, data)) {
                        float expBottom = getSideBottomY(world, worldX, y, worldZ - 1, bottomY, data);
                        float uvB = calculateSideUvBottom(expBottom, bottomY, topY);
                        if (isWater) {
                            int next = addSideQuadDirect(wPosBuf, wNormBuf, wUvBuf, wIdxBuf, wPosIdx, wNormIdx, wUvIdx, wElemIdx, wVertexCount, x + 1, x, expBottom, topY, z, z, 0, 0, -1, data, uvB, 1.0f);
                            if (next != wVertexCount) { wVertexCount = next; wPosIdx += 12; wNormIdx += 12; wUvIdx += 8; wElemIdx += 6; }
                        } else {
                            int next = addSideQuadDirect(posBuf, normBuf, uvBuf, idxBuf, posIdx, normIdx, uvIdx, elemIdx, vertexCount, x + 1, x, expBottom, topY, z, z, 0, 0, -1, data, uvB, 1.0f);
                            if (next != vertexCount) { vertexCount = next; posIdx += 12; normIdx += 12; uvIdx += 8; elemIdx += 6; }
                        }
                    }

                    if (shouldRenderFace(world, worldX + 1, y, worldZ, data) || isPartialSideExposure(world, worldX + 1, y, worldZ, data)) {
                        float expBottom = getSideBottomY(world, worldX + 1, y, worldZ, bottomY, data);
                        float uvB = calculateSideUvBottom(expBottom, bottomY, topY);
                        if (isWater) {
                            int next = addSideQuadDirect(wPosBuf, wNormBuf, wUvBuf, wIdxBuf, wPosIdx, wNormIdx, wUvIdx, wElemIdx, wVertexCount, x + 1, x + 1, expBottom, topY, z + 1, z, 1, 0, 0, data, uvB, 1.0f);
                            if (next != wVertexCount) { wVertexCount = next; wPosIdx += 12; wNormIdx += 12; wUvIdx += 8; wElemIdx += 6; }
                        } else {
                            int next = addSideQuadDirect(posBuf, normBuf, uvBuf, idxBuf, posIdx, normIdx, uvIdx, elemIdx, vertexCount, x + 1, x + 1, expBottom, topY, z + 1, z, 1, 0, 0, data, uvB, 1.0f);
                            if (next != vertexCount) { vertexCount = next; posIdx += 12; normIdx += 12; uvIdx += 8; elemIdx += 6; }
                        }
                    }

                    if (shouldRenderFace(world, worldX - 1, y, worldZ, data) || isPartialSideExposure(world, worldX - 1, y, worldZ, data)) {
                        float expBottom = getSideBottomY(world, worldX - 1, y, worldZ, bottomY, data);
                        float uvB = calculateSideUvBottom(expBottom, bottomY, topY);
                        if (isWater) {
                            int next = addSideQuadDirect(wPosBuf, wNormBuf, wUvBuf, wIdxBuf, wPosIdx, wNormIdx, wUvIdx, wElemIdx, wVertexCount, x, x, expBottom, topY, z, z + 1, -1, 0, 0, data, uvB, 1.0f);
                            if (next != wVertexCount) { wVertexCount = next; wPosIdx += 12; wNormIdx += 12; wUvIdx += 8; wElemIdx += 6; }
                        } else {
                            int next = addSideQuadDirect(posBuf, normBuf, uvBuf, idxBuf, posIdx, normIdx, uvIdx, elemIdx, vertexCount, x, x, expBottom, topY, z, z + 1, -1, 0, 0, data, uvB, 1.0f);
                            if (next != vertexCount) { vertexCount = next; posIdx += 12; normIdx += 12; uvIdx += 8; elemIdx += 6; }
                        }
                    }
                }
            }
        }

        RawMeshData solidData = buildRawData(posBuf, posIdx, normBuf, normIdx, uvBuf, uvIdx, idxBuf, elemIdx);
        RawMeshData waterData = buildRawData(wPosBuf, wPosIdx, wNormBuf, wNormIdx, wUvBuf, wUvIdx, wIdxBuf, wElemIdx);
        return new ChunkMeshData(solidData, waterData);
    }

    /**
     * Performs the build raw data operation.
     * @param pBuf the p buf value
     * @param pIdx the p idx value
     * @param nBuf the n buf value
     * @param nIdx the n idx value
     * @param uBuf the u buf value
     * @param uIdx the u idx value
     * @param iBuf the i buf value
     * @param iIdx the i idx value
     * @return the build raw data result
     */
    private static RawMeshData buildRawData(float[] pBuf, int pIdx, float[] nBuf, int nIdx, float[] uBuf, int uIdx, int[] iBuf, int iIdx) {
        if (iIdx == 0) return null;
        float[] pos = new float[pIdx]; System.arraycopy(pBuf, 0, pos, 0, pIdx);
        float[] norm = new float[nIdx]; System.arraycopy(nBuf, 0, norm, 0, nIdx);
        float[] uv = new float[uIdx]; System.arraycopy(uBuf, 0, uv, 0, uIdx);
        int[] idx = new int[iIdx]; System.arraycopy(iBuf, 0, idx, 0, iIdx);
        return new RawMeshData(pos, norm, uv, idx);
    }

    /**
     * Creates and returns the mesh.
     * @param data the data value
     * @return the created mesh
     */
    public static ChunkRenderMesh createMesh(ChunkMeshData data) {
        Mesh solid = data.solidData() != null ? new Mesh(data.solidData().positions(), data.solidData().normals(), data.solidData().uv(), data.solidData().indices()) : null;
        Mesh water = data.waterData() != null ? new Mesh(data.waterData().positions(), data.waterData().normals(), data.waterData().uv(), data.waterData().indices()) : null;
        return new ChunkRenderMesh(solid, water);
    }

    /**
     * Returns the block top y.
     * @param data the data value
     * @param y the y value
     * @return the block top y
     */
    private static float getBlockTopY(BlockData data, float y) { return (data == BlockData.TILLED_DIRT ||
            data.isFluid()) ? y + TILLED_HEIGHT : y + 1.0f; }
    /**
     * Returns the block bottom y.
     * @param world the world value
     * @param worldX the world x value
     * @param worldY the world y value
     * @param worldZ the world z value
     * @return the block bottom y
     */
    private static float getBlockBottomY(World world, int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.SIZE_Y || !world.isChunkLoadedAt(worldX, worldZ)) return 0.0f;
        byte blockId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (blockId == 0) return 0.0f;
        BlockData data = BLOCK_LUT[blockId & 0xFF];
        return data == null ? 0.0f : worldY;
    }

    /**
     * Returns the block top y.
     * @param world the world value
     * @param worldX the world x value
     * @param worldY the world y value
     * @param worldZ the world z value
     * @return the block top y
     */
    private static float getBlockTopY(World world, int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.SIZE_Y || !world.isChunkLoadedAt(worldX, worldZ)) return 0.0f;
        byte blockId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (blockId == 0) return 0.0f;
        BlockData data = BLOCK_LUT[blockId & 0xFF];
        return data == null ? 0.0f : getBlockTopY(data, worldY);
    }

    /**
     * Performs the should render face operation.
     * @param world the world value
     * @param worldX the world x value
     * @param worldY the world y value
     * @param worldZ the world z value
     * @param currentBlock the current block value
     * @return the should render face result
     */
    private static boolean shouldRenderFace(World world, int worldX, int worldY, int worldZ, BlockData currentBlock) {
        if (worldY < 0) return false;
        if (worldY >= Chunk.SIZE_Y) return true;
        if (!world.isChunkLoadedAt(worldX, worldZ)) return false;

        byte neighborId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (neighborId == 0) return true;

        BlockData neighborData = BLOCK_LUT[neighborId & 0xFF];
        if (neighborData == null) return true;

        if (currentBlock.isFluid()) {
            return !neighborData.isSolid() && neighborData != currentBlock;
        }

        if (neighborData.isFluid()) return true;
        return neighborData.isTransparent() && neighborData != currentBlock;
    }

    /**
     * Performs the should render water top operation.
     * @param world the world value
     * @param worldX the world x value
     * @param y the y value
     * @param worldZ the world z value
     * @param currentFluid the fluid being rendered
     * @return the should render water top result
     */
    private static boolean shouldRenderWaterTop(World world, int worldX, int y, int worldZ,
                                                BlockData currentFluid) {
        if (y >= Chunk.SIZE_Y - 1) return true;
        byte aboveId = world.getBlockTypeAt(worldX, y + 1, worldZ);
        if (aboveId == 0) return true;
        BlockData above = BLOCK_LUT[aboveId & 0xFF];
        if (above == null) return true;
        if (above == currentFluid) return false;
        return !above.isSolid();
    }

    /**
     * Returns the water corner height.
     * @param world the world value
     * @param wx the wx value
     * @param wy the wy value
     * @param wz the wz value
     * @param currentFluid the fluid being rendered
     * @return the water corner height
     */
    private static float getWaterCornerHeight(World world, int wx, int wy, int wz,
                                              BlockData currentFluid) {
        float totalHeight = 0;
        int count = 0;
        for (int dx = -1; dx <= 0; dx++) {
            for (int dz = -1; dz <= 0; dz++) {
                int nx = wx + dx;
                int nz = wz + dz;
                if (world.isChunkLoadedAt(nx, nz)) {
                    byte blockId = world.getBlockTypeAt(nx, wy, nz);
                    BlockData bData = BLOCK_LUT[blockId & 0xFF];
                    if (bData == currentFluid) {
                        byte lvl = world.getFluidLevelAt(nx, wy, nz);
                        float level = (lvl <= 0) ? 8 : lvl;
                        totalHeight += wy + (level / 8.0f) * TILLED_HEIGHT;
                        count++;
                    } else if (bData != null && bData.isSolid()) {
                        totalHeight += wy + 1.0f;
                        count++;
                    }
                }
            }
        }
        return count > 0 ? (totalHeight / count) : (wy + TILLED_HEIGHT);
    }

    /**
     * Checks whether the partial side exposure condition is met.
     * @param world the world value
     * @param worldX the world x value
     * @param worldY the world y value
     * @param worldZ the world z value
     * @param currentBlock the current block value
     * @return {@code true} if partial side exposure; otherwise {@code false}
     */
    private static boolean isPartialSideExposure(World world, int worldX, int worldY, int worldZ, BlockData currentBlock) {
        if (currentBlock.isFluid() || !world.isChunkLoadedAt(worldX, worldZ) || worldY < 0 || worldY >= Chunk.SIZE_Y) return false;
        byte neighborId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (neighborId == 0) return false;
        BlockData neighborData = BLOCK_LUT[neighborId & 0xFF];
        return neighborData == BlockData.TILLED_DIRT && currentBlock != BlockData.TILLED_DIRT;
    }

    /**
     * Returns the side bottom y.
     * @param world the world value
     * @param worldX the world x value
     * @param worldY the world y value
     * @param worldZ the world z value
     * @param currentBottomY the current bottom y value
     * @param currentBlock the current block value
     * @return the side bottom y
     */
    private static float getSideBottomY(World world, int worldX, int worldY, int worldZ, float currentBottomY, BlockData currentBlock) {
        if (!world.isChunkLoadedAt(worldX, worldZ) || worldY < 0 || worldY >= Chunk.SIZE_Y) return currentBottomY;
        byte neighborId = world.getBlockTypeAt(worldX, worldY, worldZ);
        if (neighborId == 0) return currentBottomY;
        BlockData neighborData = BLOCK_LUT[neighborId & 0xFF];
        if (neighborData == BlockData.TILLED_DIRT && currentBlock != BlockData.TILLED_DIRT) return currentBottomY + TILLED_HEIGHT;
        return currentBottomY;
    }

    /**
     * Calculates and returns the side uv bottom.
     * @param expBottom the exp bottom value
     * @param bottomY the bottom y value
     * @param topY the top y value
     * @return the calculate side uv bottom result
     */
    private static float calculateSideUvBottom(float expBottom, float bottomY, float topY) {
        if (topY <= bottomY) return 0.0f;
        float exposedHeight = topY - expBottom;
        if (exposedHeight <= 0.0f) return 1.0f;
        float totalHeight = topY - bottomY;
        return Math.clamp(1.0f - (exposedHeight / totalHeight), 0.0f, 1.0f);
    }

    /**
     * Adds the quad pos.
     * @param buf the buf value
     * @param idx the idx value
     * @param x1 the x1 value
     * @param y1 the y1 value
     * @param z1 the z1 value
     * @param x2 the x2 value
     * @param y2 the y2 value
     * @param z2 the z2 value
     * @param x3 the x3 value
     * @param y3 the y3 value
     * @param z3 the z3 value
     * @param x4 the x4 value
     * @param y4 the y4 value
     * @param z4 the z4 value
     * @return the add quad pos result
     */
    private static int addQuadPos(float[] buf, int idx, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        buf[idx] = x1; buf[idx + 1] = y1; buf[idx + 2] = z1;
        buf[idx + 3] = x2; buf[idx + 4] = y2; buf[idx + 5] = z2;
        buf[idx + 6] = x3; buf[idx + 7] = y3; buf[idx + 8] = z3;
        buf[idx + 9] = x4; buf[idx + 10] = y4; buf[idx + 11] = z4;
        return idx + 12;
    }

    /**
     * Adds the quad uv.
     * @param buf the buf value
     * @param idx the idx value
     * @param u1 the u1 value
     * @param v1 the v1 value
     * @param u2 the u2 value
     * @param v2 the v2 value
     * @param u3 the u3 value
     * @param v3 the v3 value
     * @param u4 the u4 value
     * @param v4 the v4 value
     * @return the add quad uv result
     */
    private static int addQuadUV(float[] buf, int idx, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4) {
        buf[idx] = u1; buf[idx + 1] = v1;
        buf[idx + 2] = u2; buf[idx + 3] = v2;
        buf[idx + 4] = u3; buf[idx + 5] = v3;
        buf[idx + 6] = u4; buf[idx + 7] = v4;
        return idx + 8;
    }

    /**
     * Adds the quad norm.
     * @param buf the buf value
     * @param idx the idx value
     * @param nx the nx value
     * @param ny the ny value
     * @param nz the nz value
     * @return the add quad norm result
     */
    private static int addQuadNorm(float[] buf, int idx, float nx, float ny, float nz) {
        for (int i = 0; i < 4; i++) { buf[idx++] = nx; buf[idx++] = ny; buf[idx++] = nz; }
        return idx;
    }

    /**
     * Adds the quad indices.
     * @param buf the buf value
     * @param idx the idx value
     * @param vertexCount the vertex count value
     * @return the add quad indices result
     */
    private static int addQuadIndices(int[] buf, int idx, int vertexCount) {
        buf[idx] = vertexCount; buf[idx + 1] = vertexCount + 1; buf[idx + 2] = vertexCount + 2;
        buf[idx + 3] = vertexCount + 2; buf[idx + 4] = vertexCount + 3; buf[idx + 5] = vertexCount;
        return idx + 6;
    }

    /**
     * Adds the side quad direct.
     * @param pos the pos value
     * @param norm the norm value
     * @param uv the uv value
     * @param idx the idx value
     * @param posI the pos i value
     * @param normI the norm i value
     * @param uvI the uv i value
     * @param elemI the elem i value
     * @param vertexCount the vertex count value
     * @param x1 the x1 value
     * @param x2 the x2 value
     * @param y1 the y1 value
     * @param y2 the y2 value
     * @param z1 the z1 value
     * @param z2 the z2 value
     * @param nx the nx value
     * @param ny the ny value
     * @param nz the nz value
     * @param data the data value
     * @param uvB the uv b value
     * @param uvT the uv t value
     * @return the add side quad direct result
     */
    private static int addSideQuadDirect(float[] pos, float[] norm, float[] uv, int[] idx,
                                         int posI, int normI, int uvI, int elemI, int vertexCount, float x1,
                                         float x2, float y1, float y2, float z1, float z2, float nx, float ny,
                                         float nz, BlockData data, float uvB, float uvT) {
        TextureAtlas.TextureRegion region = data.getSideRegion();
        if (region == null) return vertexCount;
        addQuadPos(pos, posI, x1, y1, z1, x2, y1, z2, x2, y2, z2, x1, y2, z1);
        float u1 = region.uvMin().x; float u2 = region.uvMax().x;
        float heightUV = region.uvMax().y - region.uvMin().y;
        float v1 = region.uvMin().y + heightUV * (1.0f - uvB);
        float v2 = region.uvMin().y + heightUV * (1.0f - uvT);
        addQuadUV(uv, uvI, u1, v1, u2, v1, u2, v2, u1, v2);
        addQuadNorm(norm, normI, nx, ny, nz);
        addQuadIndices(idx, elemI, vertexCount);
        return vertexCount + 4;
    }
}
