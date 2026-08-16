package com.tilled.graphics;

import com.tilled.data.RainDrop;
import com.tilled.utils.K;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public class RainEngine {
    private static final Random random = new Random();
    private static final int VERTICES_PER_DROP = 2;
    private static final int COMPONENTS_PER_VERTEX = 3;
    private final List<RainDrop> drops = new ArrayList<>();
    private final int vao;
    private final int vbo;
    private final float[] vertices = new float[K.World.RAIN_MAX_DROPS * VERTICES_PER_DROP * COMPONENTS_PER_VERTEX];

    public RainEngine() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, COMPONENTS_PER_VERTEX, GL_FLOAT, false,
                COMPONENTS_PER_VERTEX * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void update(float delta, Vector3f cameraPosition) {
        for (RainDrop drop : drops) {
            drop.update(delta);
        }

        drops.removeIf(drop -> drop.isDead(K.World.RAIN_MIN_Y));
        while (drops.size() < K.World.RAIN_MAX_DROPS) {
            spawn(cameraPosition);
        }
    }

    public void render(Shader shader, Matrix4f view, Matrix4f projection) {
        if (drops.isEmpty()) {
            return;
        }

        int vertexIndex = 0;
        for (RainDrop drop : drops) {
            float x = drop.getX();
            float y = drop.getY();
            float z = drop.getZ();
            float length = drop.getLength();
            float endX = x + K.World.RAIN_SLANT_X;
            float endY = y - length;
            float endZ = z + K.World.RAIN_SLANT_Z;

            vertices[vertexIndex++] = x;
            vertices[vertexIndex++] = y;
            vertices[vertexIndex++] = z;
            vertices[vertexIndex++] = endX;
            vertices[vertexIndex++] = endY;
            vertices[vertexIndex++] = endZ;
        }

        shader.bind();

        shader.setUniform("uView", view);
        shader.setUniform("uProjection", projection);
        shader.setUniform("uRainColor", K.Colors.RAIN);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(vertexIndex);
            buffer.put(vertices, 0, vertexIndex);
            buffer.flip();
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glLineWidth(K.World.RAIN_LINE_WIDTH);
        glDisable(GL_DEPTH_TEST);
        glDrawArrays(GL_LINES, 0, drops.size() * VERTICES_PER_DROP);
        glEnable(GL_DEPTH_TEST);
        glBindVertexArray(0);
        shader.unbind();
    }

    private void spawn(Vector3f cameraPosition) {
        float x = cameraPosition.x + (random.nextFloat() - 0.5f) * K.World.RAIN_SPAWN_RADIUS * 2.0f;
        float z = cameraPosition.z + (random.nextFloat() - 0.5f) * K.World.RAIN_SPAWN_RADIUS * 2.0f;
        float y = cameraPosition.y + K.World.RAIN_SPAWN_HEIGHT_OFFSET + random.nextFloat() * K.World.RAIN_SPAWN_HEIGHT_VARIATION;
        float velocity = K.World.RAIN_MIN_VELOCITY + random.nextFloat() * K.World.RAIN_VELOCITY_VARIATION;
        float length = K.World.RAIN_MIN_LENGTH + random.nextFloat() * K.World.RAIN_LENGTH_VARIATION;
        drops.add(new RainDrop(x, y, z, velocity, length));
    }

    public List<RainDrop> getDrops() {
        return drops;
    }

    public void clear() {
        drops.clear();
    }

    public void dispose() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}