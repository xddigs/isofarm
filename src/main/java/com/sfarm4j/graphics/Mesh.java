package com.sfarm4j.graphics;

import org.lwjgl.system.MemoryStack;
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

        try (MemoryStack stack = MemoryStack.stackPush()) {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            posVboId = glGenBuffers();
            FloatBuffer posBuffer = stack.mallocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            normalVboId = glGenBuffers();
            FloatBuffer normalBuffer = stack.mallocFloat(normals.length);
            normalBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            uvVboId = glGenBuffers();
            FloatBuffer texBuffer = stack.mallocFloat(textCoords.length);
            texBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, uvVboId);
            glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);

            eboId = glGenBuffers();
            IntBuffer idxBuffer = stack.mallocInt(indices.length);
            idxBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);

            glBindVertexArray(0);
        }

        log.info("Mesh created successfully [VAO ID: {}, Vertices: {}]", vaoId, vertexCount);
    }

    public static Mesh createQuad() {
        float[] positions = new float[] {
                -0.5f, 0.0f, -0.5f,
                -0.5f, 0.0f,  0.5f,
                0.5f, 0.0f,  0.5f,
                0.5f, 0.0f, -0.5f
        };

        float[] normals = new float[] {
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        };

        float[] textCoords = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        int[] indices = new int[] {
                0, 1, 3,
                3, 1, 2
        };

        return new Mesh(positions, normals, textCoords, indices);
    }

    public static Mesh createVerticalQuad() {
        float[] positions = new float[] {
                -0.5f, 1.0f, 0.0f,
                -0.5f, 0.0f, 0.0f,
                0.5f, 0.0f, 0.0f,
                0.5f, 1.0f, 0.0f
        };

        float[] normals = new float[] {
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f
        };

        float[] textCoords = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        int[] indices = new int[] {
                0, 1, 3,
                3, 1, 2
        };

        return new Mesh(positions, normals, textCoords, indices);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
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