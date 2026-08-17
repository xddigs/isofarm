package com.tilled.graphics;

import com.tilled.data.BlockData;
import com.tilled.utils.K;
import com.tilled.wrld.Chunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkMeshBuilder {

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
                    if (data == null) continue;
                    if (data == BlockData.CROP) continue;

                    float vx = x;
                    float vy = y;
                    float vz = z;

                    boolean isTilledSoil = data == BlockData.TILLED_DIRT;
                    float topY = vy + 1.0f;
                    if (isTilledSoil) {
                        topY -= 1.0f / K.World.DEFAULT_TEXTURE_SCALE;
                    }

                    if (isAir(chunk, x, y + 1, z)) {
                        vertexCount = addFace(positions, normals, uvs, indices, vertexCount,
                                new float[]{
                                        vx, topY, vz+1,   data.getTopAtlasOffset().x, data.getTopAtlasOffset().y + data.getAtlasScale().y,  0, 1, 0,
                                        vx+1, topY, vz+1, data.getTopAtlasOffset().x + data.getAtlasScale().x, data.getTopAtlasOffset().y + data.getAtlasScale().y, 0, 1, 0,
                                        vx+1, topY, vz,   data.getTopAtlasOffset().x + data.getAtlasScale().x, data.getTopAtlasOffset().y, 0, 1, 0,
                                        vx, topY, vz,     data.getTopAtlasOffset().x, data.getTopAtlasOffset().y, 0, 1, 0
                                });
                    }

                    if (isAir(chunk, x, y - 1, z)) {
                        vertexCount = addFace(positions, normals, uvs, indices, vertexCount,
                                new float[]{
                                        vx, vy, vz,       data.getBottomAtlasOffset().x, data.getBottomAtlasOffset().y, 0, -1, 0,
                                        vx+1, vy, vz,     data.getBottomAtlasOffset().x + data.getAtlasScale().x, data.getBottomAtlasOffset().y, 0, -1, 0,
                                        vx+1, vy, vz+1,   data.getBottomAtlasOffset().x + data.getAtlasScale().x, data.getBottomAtlasOffset().y + data.getAtlasScale().y, 0, -1, 0,
                                        vx, vy, vz+1,     data.getBottomAtlasOffset().x, data.getBottomAtlasOffset().y + data.getAtlasScale().y, 0, -1, 0
                                });
                    }

                    if (isAir(chunk, x, y, z + 1)) {
                        vertexCount = addFace(positions, normals, uvs, indices, vertexCount,
                                new float[]{
                                        vx, vy, vz+1,     data.getSideAtlasOffset().x, data.getSideAtlasOffset().y, 0, 0, 1,
                                        vx+1, vy, vz+1,   data.getSideAtlasOffset().x + data.getAtlasScale().x, data.getSideAtlasOffset().y, 0, 0, 1,
                                        vx+1, topY, vz+1, data.getSideAtlasOffset().x + data.getAtlasScale().x, data.getSideAtlasOffset().y + data.getAtlasScale().y, 0, 0, 1,
                                        vx, topY, vz+1,   data.getSideAtlasOffset().x, data.getSideAtlasOffset().y + data.getAtlasScale().y, 0, 0, 1
                                });
                    }

                    if (isAir(chunk, x, y, z - 1)) {
                        vertexCount = addFace(positions, normals, uvs, indices, vertexCount,
                                new float[]{
                                        vx+1, vy, vz,     data.getSideAtlasOffset().x, data.getSideAtlasOffset().y, 0, 0, -1,
                                        vx, vy, vz,       data.getSideAtlasOffset().x + data.getAtlasScale().x, data.getSideAtlasOffset().y, 0, 0, -1,
                                        vx, topY, vz,     data.getSideAtlasOffset().x + data.getAtlasScale().x, data.getSideAtlasOffset().y + data.getAtlasScale().y, 0, 0, -1,
                                        vx+1, topY, vz,   data.getSideAtlasOffset().x, data.getSideAtlasOffset().y + data.getAtlasScale().y, 0, 0, -1
                                });
                    }

                    if (isAir(chunk, x + 1, y, z)) {
                        vertexCount = addFace(positions, normals, uvs, indices, vertexCount,
                                new float[]{
                                        vx+1, vy, vz+1,   data.getSideAtlasOffset().x, data.getSideAtlasOffset().y, 1, 0, 0,
                                        vx+1, vy, vz,     data.getSideAtlasOffset().x + data.getAtlasScale().x, data.getSideAtlasOffset().y, 1, 0, 0,
                                        vx+1, topY, vz,   data.getSideAtlasOffset().x + data.getAtlasScale().x, data.getSideAtlasOffset().y + data.getAtlasScale().y, 1, 0, 0,
                                        vx+1, topY, vz+1, data.getSideAtlasOffset().x, data.getSideAtlasOffset().y + data.getAtlasScale().y, 1, 0, 0
                                });
                    }

                    if (isAir(chunk, x - 1, y, z)) {
                        vertexCount = addFace(positions, normals, uvs, indices, vertexCount,
                                new float[]{
                                        vx, vy, vz,       data.getSideAtlasOffset().x, data.getSideAtlasOffset().y, -1, 0, 0,
                                        vx, vy, vz+1,     data.getSideAtlasOffset().x + data.getAtlasScale().x, data.getSideAtlasOffset().y, -1, 0, 0,
                                        vx, topY, vz+1,   data.getSideAtlasOffset().x + data.getAtlasScale().x, data.getSideAtlasOffset().y + data.getAtlasScale().y, -1, 0, 0,
                                        vx, topY, vz,     data.getSideAtlasOffset().x, data.getSideAtlasOffset().y + data.getAtlasScale().y, -1, 0, 0
                                });
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

    private static boolean isAir(Chunk chunk, int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) return true;
        if (x < 0 || x >= Chunk.SIZE_X || z < 0 || z >= Chunk.SIZE_Z) return true;
        return chunk.getBlock(x, y, z) == 0;
    }

    private static BlockData getBlockDataById(byte id) {
        for (BlockData data : BlockData.values()) {
            if (data.getId() == id) return data;
        }
        return null;
    }

    private static int addFace(List<Float> positions, List<Float> normals, List<Float> uvs,
                               List<Integer> indices, int vertexCount, float[] faceData) {
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

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}