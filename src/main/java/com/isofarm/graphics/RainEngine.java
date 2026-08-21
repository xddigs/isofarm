package com.isofarm.graphics;

import com.isofarm.data.RainDrop;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.ChunkManager;
import com.isofarm.wrld.World;
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
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RainEngine {
    private static final Random RANDOM = new Random();
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

    public void update(float delta, Vector3f cameraPosition, World world,
                       ChunkManager ignoredChunkManager) {
        drops.forEach(drop -> drop.update(delta));
        drops.removeIf(drop -> {
            int blockX = (int) Math.floor(drop.getX());
            int blockZ = (int) Math.floor(drop.getZ());

            float floorY = world.getHighestY(blockX, blockZ).y();
            return drop.getY() <= floorY;
        });

        int targetAmount = K.World.RAIN_MAX_DROPS;
        if (drops.size() < targetAmount) {
            spawnRainInChunks(cameraPosition, world, targetAmount - drops.size());
        }
    }

    private void spawnRainInChunks(Vector3f cameraPosition, World world, int countToSpawn) {
        int centerChunkX = (int) Math.floor(cameraPosition.x / Chunk.SIZE_X);
        int centerChunkZ = (int) Math.floor(cameraPosition.z / Chunk.SIZE_Z);
        int renderDistance = Settings.renderDistance;

        for (int i = 0; i < countToSpawn; i++) {
            int chunkX = centerChunkX + RANDOM.nextInt(renderDistance * 2 + 1) - renderDistance;
            int chunkZ = centerChunkZ + RANDOM.nextInt(renderDistance * 2 + 1) - renderDistance;

            float x = (chunkX * Chunk.SIZE_X) + RANDOM.nextFloat() * Chunk.SIZE_X;
            float z = (chunkZ * Chunk.SIZE_Z) + RANDOM.nextFloat() * Chunk.SIZE_Z;

            float roofY = world.getHighestY((int) Math.floor(x), (int) Math.floor(z)).y();
            float spawnY = Math.max(cameraPosition.y + K.World.RAIN_SPAWN_HEIGHT_OFFSET, roofY + 10.0f);
            spawnY += RANDOM.nextFloat() * K.World.RAIN_SPAWN_HEIGHT_VARIATION;

            float velocity = K.World.RAIN_MIN_VELOCITY + RANDOM.nextFloat() * K.World.RAIN_VELOCITY_VARIATION;
            float length = K.World.RAIN_MIN_LENGTH + RANDOM.nextFloat() * K.World.RAIN_LENGTH_VARIATION;

            drops.add(new RainDrop(x, spawnY, z, velocity, length));
        }
    }

    public void render(Shader shader, Matrix4f view, Matrix4f projection) {
        if (drops.isEmpty()) return;
        int vertexIndex = 0;

        for (RainDrop drop : drops) {
            float x = drop.getX();
            float y = drop.getY();
            float z = drop.getZ();
            float length = drop.getLength();

            vertices[vertexIndex++] = x;
            vertices[vertexIndex++] = y;
            vertices[vertexIndex++] = z;

            vertices[vertexIndex++] = x + K.World.RAIN_SLANT_X;
            vertices[vertexIndex++] = y - length;
            vertices[vertexIndex++] = z + K.World.RAIN_SLANT_Z;
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
        glEnable(GL_DEPTH_TEST);
        glDepthMask(false);

        glDrawArrays(GL_LINES, 0, drops.size() * VERTICES_PER_DROP);

        glDepthMask(true);
        glBindVertexArray(0);
        shader.unbind();
    }

    public void clear() {
        drops.clear();
    }

    public void dispose() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}