package com.isofarm.graphics;

import com.isofarm.data.BlockData;
import com.isofarm.utils.K;
import com.isofarm.wrld.Chunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkMeshBuilder {

    private static final float PIXEL = 1.0f / K.World.DEFAULT_TEXTURE_SCALE;
    private static final float TILLED_HEIGHT = 1.0f - PIXEL;

    public static Mesh buildMesh(Chunk chunk) {
        List<Float> positions = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> uvs = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int vertexCount = 0;

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    byte blockId = chunk.getBlock(x, y, z);
                    if (blockId == 0) continue;
                    BlockData data = getBlockDataById(blockId);
                    if (data == null || data == BlockData.CROP) continue;

                    float vx = x;
                    float vy = y;
                    float vz = z;

                    float bottomY = vy;
                    float topY = getBlockTopY(data, vy);

                    float aboveBottomY = getBlockBottomY(chunk, x, y + 1, z);
                    if (shouldRenderFace(chunk, x, y + 1, z, data) || aboveBottomY > topY) {
                        vertexCount = addFace(positions, normals, uvs, indices, vertexCount, new float[]{vx, topY, vz + 1, data.getTopAtlasOffset().x, data.getTopAtlasOffset().y + data.getAtlasScale().y, 0, 1, 0,
                                vx + 1, topY, vz + 1, data.getTopAtlasOffset().x + data.getAtlasScale().x, data.getTopAtlasOffset().y + data.getAtlasScale().y, 0, 1, 0,
                                vx + 1, topY, vz, data.getTopAtlasOffset().x + data.getAtlasScale().x, data.getTopAtlasOffset().y, 0, 1, 0,
                                vx, topY, vz, data.getTopAtlasOffset().x, data.getTopAtlasOffset().y, 0, 1, 0});
                    }

                    float belowTopY = getBlockTopY(chunk, x, y - 1, z);
                    if (shouldRenderFace(chunk, x, y - 1, z, data) || belowTopY < bottomY) {
                        vertexCount = addFace(positions, normals, uvs, indices, vertexCount, new float[]{vx, bottomY, vz, data.getBottomAtlasOffset().x, data.getBottomAtlasOffset().y, 0, -1, 0,
                                vx + 1, bottomY, vz, data.getBottomAtlasOffset().x + data.getAtlasScale().x, data.getBottomAtlasOffset().y, 0, -1, 0,
                                vx + 1, bottomY, vz + 1, data.getBottomAtlasOffset().x + data.getAtlasScale().x, data.getBottomAtlasOffset().y + data.getAtlasScale().y, 0, -1, 0,
                                vx, bottomY, vz + 1, data.getBottomAtlasOffset().x, data.getBottomAtlasOffset().y + data.getAtlasScale().y, 0, -1, 0});
                    }

                    float neighborTop = getBlockTopY(chunk, x, y, z + 1);
                    if (shouldRenderFace(chunk, x, y, z + 1, data)) {
                        vertexCount = addSideFace(positions, normals, uvs, indices, vertexCount,
                                vx, vx + 1,
                                bottomY, topY,
                                vz + 1, vz + 1,
                                0, 0, 1,
                                data, 0.0f, 1.0f);

                    } else if (neighborTop < topY) {
                        float exposedBottom = Math.max(bottomY, neighborTop);
                        vertexCount = addSideFace(positions, normals, uvs, indices, vertexCount,
                                vx, vx + 1,
                                exposedBottom, topY,
                                vz + 1, vz + 1,
                                0, 0, 1,
                                data,
                                (exposedBottom - bottomY) / (topY - bottomY), 1.0f);
                    }

                    neighborTop = getBlockTopY(chunk, x, y, z - 1);
                    if (shouldRenderFace(chunk, x, y, z - 1, data)) {
                        vertexCount = addSideFace(positions, normals, uvs, indices, vertexCount,
                                vx + 1, vx,
                                bottomY, topY,
                                vz, vz,
                                0, 0, -1,
                                data, 0.0f, 1.0f);

                    } else if (neighborTop < topY) {
                        float exposedBottom = Math.max(bottomY, neighborTop);
                        vertexCount = addSideFace(positions, normals, uvs, indices, vertexCount,
                                vx + 1, vx,
                                exposedBottom, topY,
                                vz, vz,
                                0, 0, -1,
                                data,
                                (exposedBottom - bottomY) / (topY - bottomY), 1.0f);
                    }

                    neighborTop = getBlockTopY(chunk, x + 1, y, z);
                    if (shouldRenderFace(chunk, x + 1, y, z, data)) {
                        vertexCount = addSideFace(positions, normals, uvs, indices, vertexCount,
                                vx + 1, vx + 1,
                                bottomY, topY,
                                vz + 1, vz,
                                1, 0, 0,
                                data, 0.0f, 1.0f);

                    } else if (neighborTop < topY) {
                        float exposedBottom = Math.max(bottomY, neighborTop);
                        vertexCount = addSideFace(positions, normals, uvs, indices, vertexCount,
                                vx + 1, vx + 1,
                                exposedBottom, topY,
                                vz + 1, vz,
                                1, 0, 0,
                                data,
                                (exposedBottom - bottomY) / (topY - bottomY), 1.0f);
                    }

                    neighborTop = getBlockTopY(chunk, x - 1, y, z);
                    if (shouldRenderFace(chunk, x - 1, y, z, data)) {
                        vertexCount = addSideFace(positions, normals, uvs, indices, vertexCount,
                                vx, vx,
                                bottomY, topY,
                                vz, vz + 1,
                                -1, 0, 0,
                                data, 0.0f, 1.0f);

                    } else if (neighborTop < topY) {
                        float exposedBottom = Math.max(bottomY, neighborTop);
                        vertexCount = addSideFace(positions, normals, uvs, indices, vertexCount,
                                vx, vx,
                                exposedBottom, topY,
                                vz, vz + 1,
                                -1, 0, 0,
                                data,
                                (exposedBottom - bottomY) / (topY - bottomY), 1.0f);
                    }
                }
            }
        }

        float[] posArray = toFloatArray(positions);
        float[] normArray = toFloatArray(normals);
        float[] uvArray = toFloatArray(uvs);
        int[] indexArray = toIntArray(indices);

        return new Mesh(posArray, normArray, uvArray, indexArray);
    }

    private static float getBlockTopY(BlockData data, float y) {
        if (data == BlockData.TILLED_DIRT) return y + TILLED_HEIGHT;
        return y + 1.0f;
    }

    private static float getBlockBottomY(Chunk chunk, int x, int y, int z) {
        if (x < 0 || x >= Chunk.SIZE_X ||
                y < 0 || y >= Chunk.SIZE_Y ||
                z < 0 || z >= Chunk.SIZE_Z) {
            return y;
        }

        byte blockId = chunk.getBlock(x, y, z);
        if (blockId == 0) return y;

        BlockData data = getBlockDataById(blockId);
        if (data == null || data == BlockData.CROP) return y;
        return y;
    }

    private static float getBlockTopY(Chunk chunk, int x, int y, int z) {
        if (x < 0 || x >= Chunk.SIZE_X ||
                y < 0 || y >= Chunk.SIZE_Y ||
                z < 0 || z >= Chunk.SIZE_Z) return y;

        byte blockId = chunk.getBlock(x, y, z);
        if (blockId == 0) return y;

        BlockData data = getBlockDataById(blockId);
        if (data == null || data == BlockData.CROP) return y;
        return getBlockTopY(data, y);
    }

    private static boolean shouldRenderFace(Chunk chunk, int neighborX, int neighborY, int neighborZ,
                                            BlockData currentBlock) {
        if (isAir(chunk, neighborX, neighborY, neighborZ)) return true;
        byte neighborId = chunk.getBlock(neighborX, neighborY, neighborZ);
        BlockData neighborData = getBlockDataById(neighborId);

        if (neighborData == null) return true;
        if (neighborData.isTransparent()) {
            return neighborData != currentBlock;
        }

        return false;
    }

    private static boolean isAir(Chunk chunk, int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) return true;
        if (x < 0 || x >= Chunk.SIZE_X || z < 0 || z >= Chunk.SIZE_Z) return true;
        return chunk.getBlock(x, y, z) == 0;
    }

    private static BlockData getBlockDataById(byte id) {
        for (BlockData data : BlockData.values()) {
            if (data.getId() == id) {
                return data;
            }
        }

        return null;
    }

    private static int addFace(List<Float> positions, List<Float> normals,
                               List<Float> uvs, List<Integer> indices,
                               int vertexCount, float[] faceData) {
        for (int i = 0; i < 4; i++) {
            int base = i * 8;

            positions.add(faceData[base]);
            positions.add(faceData[base + 1]);
            positions.add(faceData[base + 2]);

            uvs.add(faceData[base + 3]);
            uvs.add(faceData[base + 4]);

            normals.add(faceData[base + 5]);
            normals.add(faceData[base + 6]);
            normals.add(faceData[base + 7]);
        }

        indices.add(vertexCount);
        indices.add(vertexCount + 1);
        indices.add(vertexCount + 2);

        indices.add(vertexCount + 2);
        indices.add(vertexCount + 3);
        indices.add(vertexCount);

        return vertexCount + 4;
    }

    private static int addSideFace(List<Float> positions, List<Float> normals,
                                   List<Float> uvs, List<Integer> indices, int vertexCount,
                                   float x1, float x2,
                                   float y1, float y2,
                                   float z1, float z2,
                                   float nx, float ny, float nz,
                                   BlockData data,
                                   float uvBottom, float uvTop) {
        float atlasX = data.getSideAtlasOffset().x;
        float atlasY = data.getSideAtlasOffset().y;

        float atlasWidth = data.getAtlasScale().x;
        float atlasHeight = data.getAtlasScale().y;

        float u1 = atlasX;
        float u2 = atlasX + atlasWidth;

        float v1 = atlasY + atlasHeight * uvBottom;
        float v2 = atlasY + atlasHeight * uvTop;

        return addFace(positions, normals, uvs, indices, vertexCount,
                new float[]{x1, y1, z1, u1, v1, nx, ny, nz,
                        x2, y1, z2, u2, v1, nx, ny, nz,
                        x2, y2, z2, u2, v2, nx, ny, nz,
                        x1, y2, z1, u1, v2, nx, ny, nz});
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];

        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }

        return arr;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }

        return arr;
    }
}