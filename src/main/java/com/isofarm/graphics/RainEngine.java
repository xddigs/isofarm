package com.isofarm.graphics;

import com.isofarm.data.RainDrop;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.Chunk;
import com.isofarm.wrld.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class RainEngine {
    private static final Random RANDOM = new Random();
    private static final int VERTICES_PER_DROP = 2;
    private static final int COMPONENTS_PER_VERTEX = 3;
    private static final int DROPS_PER_CHUNK = 25;

    private final List<RainDrop> drops = new ArrayList<>();
    private final Map<Long, Integer> chunkDropCounts = new HashMap<>();
    private final int vao;
    private final int vbo;
    private final FloatBuffer vertexBuffer;
    private final float[] vertices = new float[K.World.RAIN_MAX_DROPS *
            VERTICES_PER_DROP * COMPONENTS_PER_VERTEX];

    public RainEngine() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.length);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, COMPONENTS_PER_VERTEX, GL_FLOAT, false,
                COMPONENTS_PER_VERTEX * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void update(float delta, Vector3f cameraPosition, World world) {
        drops.forEach(drop -> drop.update(delta));
        drops.removeIf(drop -> {
            int blockX = (int) Math.floor(drop.getX());
            int blockZ = (int) Math.floor(drop.getZ());
            float floorY = world.getHighestY(blockX, blockZ).y();

            if (drop.getY() <= floorY) {
                long key = chunkKey(Math.floorDiv(blockX, Chunk.SIZE_X),
                        Math.floorDiv(blockZ, Chunk.SIZE_Z));

                Integer count = chunkDropCounts.get(key);

                if (count != null) {
                    if (count <= 1) chunkDropCounts.remove(key);
                    else chunkDropCounts.put(key, count - 1);
                }

                return true;
            }

            return false;
        });

        maintainRainChunks(cameraPosition, world);
    }

    private void maintainRainChunks(Vector3f cameraPosition, World world) {
        if (drops.size() >= K.World.RAIN_MAX_DROPS) return;
        int centerChunkX = (int) Math.floor(cameraPosition.x / Chunk.SIZE_X);
        int centerChunkZ = (int) Math.floor(cameraPosition.z / Chunk.SIZE_Z);
        int distance = Settings.renderDistance;

        for (int chunkX = centerChunkX - distance; chunkX <= centerChunkX + distance; chunkX++) {
            for (int chunkZ = centerChunkZ - distance; chunkZ <= centerChunkZ + distance; chunkZ++) {
                if (drops.size() >= K.World.RAIN_MAX_DROPS) return;
                spawnMissingDrops(chunkX, chunkZ, world);
            }
        }
    }

    private void spawnMissingDrops(int chunkX, int chunkZ, World world) {
        if (drops.size() >= K.World.RAIN_MAX_DROPS) return;

        long key = chunkKey(chunkX, chunkZ);
        int current = chunkDropCounts.getOrDefault(key, 0);
        int missing = DROPS_PER_CHUNK - current;

        if (missing <= 0) return;

        int available = K.World.RAIN_MAX_DROPS - drops.size();
        int amount = Math.min(missing, available);

        for (int i = 0; i < amount; i++) {
            spawnDrop(chunkX, chunkZ, world);
        }

        chunkDropCounts.put(key, current + amount);
    }

    private void spawnDrop(int chunkX, int chunkZ, World world) {
        float x = chunkX * Chunk.SIZE_X + RANDOM.nextFloat() * Chunk.SIZE_X;
        float z = chunkZ * Chunk.SIZE_Z + RANDOM.nextFloat() * Chunk.SIZE_Z;

        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);

        float groundY = world.getHighestY(blockX, blockZ).y();
        float spawnY = groundY + K.World.RAIN_SPAWN_HEIGHT_OFFSET + RANDOM.nextFloat() * K.World.RAIN_SPAWN_HEIGHT_VARIATION;
        float velocity = K.World.RAIN_MIN_VELOCITY + RANDOM.nextFloat() * K.World.RAIN_VELOCITY_VARIATION;
        float length = K.World.RAIN_MIN_LENGTH + RANDOM.nextFloat() * K.World.RAIN_LENGTH_VARIATION;
        drops.add(new RainDrop(x, spawnY, z, velocity, length));
    }

    private long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }

    public void render(Shader shader, Matrix4f view, Matrix4f projection) {
        if (drops.isEmpty()) return;

        int renderedDrops = Math.min(drops.size(), K.World.RAIN_MAX_DROPS);
        int vertexIndex = 0;

        for (int i = 0; i < renderedDrops; i++) {
            RainDrop drop = drops.get(i);

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

        vertexBuffer.clear();
        vertexBuffer.put(vertices, 0, vertexIndex);
        vertexBuffer.flip();

        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glLineWidth(K.World.RAIN_LINE_WIDTH);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(false);

        glDrawArrays(GL_LINES, 0, renderedDrops * VERTICES_PER_DROP);

        glDepthMask(true);
        glBindVertexArray(0);
        shader.unbind();
    }

    public void clear() {
        drops.clear();
        chunkDropCounts.clear();
    }

    public void dispose() {
        vertexBuffer.clear();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}