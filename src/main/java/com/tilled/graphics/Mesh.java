package com.tilled.graphics;

import com.tilled.utils.K;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private static final Logger log = LoggerFactory.getLogger(Mesh.class);
    private final int vaoId;
    private final int posVboId;
    private final int normalVboId;
    private final int uvVboId;
    private final int eboId;
    private final int vertexCount;

    public Mesh(float[] positions, float[] normals, float[] textCoords, int[] indices) {
        this.vertexCount = indices.length;

        FloatBuffer posBuffer = null;
        FloatBuffer normalBuffer = null;
        FloatBuffer texBuffer = null;
        IntBuffer idxBuffer = null;

        try {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            posVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            normalBuffer = MemoryUtil.memAllocFloat(normals.length);
            normalBuffer.put(normals).flip();
            normalVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            texBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            texBuffer.put(textCoords).flip();
            uvVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, uvVboId);
            glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);

            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();
            eboId = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);

            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) MemoryUtil.memFree(posBuffer);
            if (normalBuffer != null) MemoryUtil.memFree(normalBuffer);
            if (texBuffer != null) MemoryUtil.memFree(texBuffer);
            if (idxBuffer != null) MemoryUtil.memFree(idxBuffer);
        }

        log.info("Mesh created successfully [VAO ID: {}, Vertices: {}]", vaoId, vertexCount);
    }

    public int getIndicesCount() {
        return vertexCount;
    }

    public static Mesh createMesh(float depth) {
        float[] positions = getFloats(depth);

        float[] normals = new float[] {
                0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,
                0.0f, 0.0f,-1.0f,  0.0f, 0.0f,-1.0f,  0.0f, 0.0f,-1.0f,  0.0f, 0.0f,-1.0f,
                -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                0.0f,-1.0f, 0.0f,  0.0f,-1.0f, 0.0f,  0.0f,-1.0f, 0.0f,  0.0f,-1.0f, 0.0f
        };

        float[] textCoords = new float[] {
                0,0, 0,1, 1,1, 1,0,
                0,0, 0,1, 1,1, 1,0,
                0,0, 0,1, 1,1, 1,0,
                0,0, 0,1, 1,1, 1,0,
                0,0, 0,1, 1,1, 1,0,
                0,0, 0,1, 1,1, 1,0
        };

        int[] indices = new int[36];
        for (int i = 0; i < 6; i++) {
            int v = i * 4;
            int idx = i * 6;
            indices[idx]     = v;
            indices[idx + 1] = v + 1;
            indices[idx + 2] = v + 3;
            indices[idx + 3] = v + 3;
            indices[idx + 4] = v + 1;
            indices[idx + 5] = v + 2;
        }

        return new Mesh(positions, normals, textCoords, indices);
    }

    public static Mesh screenQuad() {
        float[] positions = new float[] {
                -1.0f,  1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                1.0f,  1.0f, 0.0f
        };
        float[] normals = new float[12];
        float[] texCoords = new float[] {
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
        };
        int[] indices = new int[] { 0, 1, 2, 2, 3, 0 };
        return new Mesh(positions, normals, texCoords, indices);
    }

    private static float[] getFloats(float depth) {
        float height = 1.0f;

        return new float[] {
                -0.5f,  0.0f,    -0.5f,
                -0.5f,  0.0f,     0.5f,
                0.5f,  0.0f,     0.5f,
                0.5f,  0.0f,    -0.5f,

                -0.5f,  0.0f,     0.5f,
                -0.5f, -height,   0.5f,
                0.5f, -height,   0.5f,
                0.5f,  0.0f,     0.5f,

                0.5f,  0.0f,     0.5f,
                0.5f, -height,   0.5f,
                0.5f, -height,  -0.5f,
                0.5f,  0.0f,   -0.5f,

                0.5f,  0.0f,   -0.5f,
                0.5f, -height, -0.5f,
                -0.5f, -height, -0.5f,
                -0.5f,  0.0f,   -0.5f,

                -0.5f,  0.0f,   -0.5f,
                -0.5f, -height, -0.5f,
                -0.5f, -height,  0.5f,
                -0.5f,  0.0f,    0.5f,

                -0.5f, -height,  0.5f,
                -0.5f, -height, -0.5f,
                0.5f, -height, -0.5f,
                0.5f, -height,  0.5f
        };
    }

    public static Mesh selection() {
        float eps = 0.002f;
        float[] positions = getPositions(eps);

        float[] normals = new float[24];
        float[] textCoords = new float[16];

        int[] indices = new int[] {
                0, 1,  1, 2,  2, 3,  3, 0,
                4, 5,  5, 6,  6, 7,  7, 4,
                0, 4,  1, 5,  2, 6,  3, 7
        };

        return new Mesh(positions, normals, textCoords, indices);
    }

    private static float[] getPositions(float eps) {
        float minX = -eps;
        float maxX = 1.0f + eps;

        float minY = -eps;
        float maxY = 1.0f + eps;

        float minZ = -eps;
        float maxZ = 1.0f + eps;

        return new float[] {
                minX, maxY, minZ,
                maxX, maxY, minZ,
                maxX, maxY, maxZ,
                minX, maxY, maxZ,
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, minY, maxZ,
                minX, minY, maxZ
        };
    }

    public void renderLines() {
        glBindVertexArray(vaoId);
        glLineWidth(K.Render.LINE_WIDTH);
        glDrawElements(GL_LINES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public static Mesh quadVertical() {
        float[] positions = new float[] {
                -0.25f, 0.0f, 0.0f,
                0.25f, 0.0f, 0.0f,
                0.25f, 1.0f, 0.0f,
                -0.25f, 1.0f, 0.0f
        };

        float[] normals = new float[] {
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f
        };

        float[] texCoords = new float[] {
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f
        };

        int[] indices = new int[] {
                0, 1, 2,
                2, 3, 0
        };

        return new Mesh(positions, normals, texCoords, indices);
    }

    public static Mesh createCrop() {
        float[] positions = new float[] {
                -0.4f, 0.0f, -0.25f,
                0.4f, 0.0f, -0.25f,
                0.4f, 0.8f, -0.25f,
                -0.4f, 0.8f, -0.25f,

                -0.4f, 0.0f,  0.25f,
                0.4f, 0.0f,  0.25f,
                0.4f, 0.8f,  0.25f,
                -0.4f, 0.8f,  0.25f,

                -0.25f, 0.0f, -0.4f,
                -0.25f, 0.0f,  0.4f,
                -0.25f, 0.8f,  0.4f,
                -0.25f, 0.8f, -0.4f,

                0.25f, 0.0f, -0.4f,
                0.25f, 0.0f,  0.4f,
                0.25f, 0.8f,  0.4f,
                0.25f, 0.8f, -0.4f
        };

        float[] normals = new float[] {
                0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,
                0.0f, 0.0f,  1.0f,  0.0f, 0.0f,  1.0f,  0.0f, 0.0f,  1.0f,  0.0f, 0.0f,  1.0f,
                -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f
        };

        float[] texCoords = new float[] {
                0.0f, 0.0f,  1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f,
                0.0f, 0.0f,  1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f,
                0.0f, 0.0f,  1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f,
                0.0f, 0.0f,  1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f
        };

        int[] indices = new int[] {
                0, 1, 2,  2, 3, 0,    2, 1, 0,  0, 3, 2,
                4, 5, 6,  6, 7, 4,    6, 5, 4,  4, 7, 6,
                8, 9, 10, 10, 11, 8,  10, 9, 8, 8, 11, 10,
                12, 13, 14, 14, 15, 12, 14, 13, 12, 12, 15, 14
        };

        return new Mesh(positions, normals, texCoords, indices);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void dispose() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(normalVboId);
        glDeleteBuffers(uvVboId);
        glDeleteBuffers(eboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);

        log.info("Mesh resources cleaned up [VAO ID: {}]", vaoId);
    }
}