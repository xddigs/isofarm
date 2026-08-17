package com.tilled.graphics;

import com.tilled.data.BlockData;
import com.tilled.data.Particle;
import com.tilled.service.Service;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleEngine implements Service<Particle> {
    private static final Random random = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private final Matrix4f modelMatrix;

    public ParticleEngine() {
        this.modelMatrix = new Matrix4f();
    }

    public void add(Particle particle) {
        particles.add(particle);
    }

    public void update(float delta) {
        for (Particle p : particles) {
            p.update(delta);
        }
        particles.removeIf(Particle::isDead);
    }

    public void render(Shader shader, Mesh quadMesh) {
        shader.setUniform("uParticleAlpha", 1.0f);
        if (particles.isEmpty()) {
            return;
        }

        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);

        SpriteSheet currentTexture = null;

        for (Particle p : particles) {
            if (p.getTexture() == null) {
                continue;
            }

            if (p.getTexture() != currentTexture) {
                if (currentTexture != null) {
                    currentTexture.unbind();
                }

                currentTexture = p.getTexture();
                currentTexture.bind();
            }

            modelMatrix.identity()
                    .translate(p.getX(), p.getY(), p.getZ())
                    .scale(p.getSize() * p.getAlpha());

            shader.setUniform("uModel", modelMatrix);
            shader.setUniform("uAtlasOffset", p.getUvOffset());
            shader.setUniform("uAtlasScale", p.getUvScale());

            quadMesh.render();
        }

        if (currentTexture != null) {
            currentTexture.unbind();
        }

        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", true);
        shader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
    }

    public void spawn(float blockX, float blockY, float blockZ, BlockData blockData, SpriteSheet blocksTexture) {
        Vector2f baseOffset = blockData.getSideAtlasOffset();
        Vector2f baseScale = blockData.getAtlasScale();

        int gridSize = 4;
        float subScaleX = baseScale.x / gridSize;
        float subScaleY = baseScale.y / gridSize;

        for (int ix = 0; ix < gridSize; ix++) {
            for (int iy = 0; iy < gridSize; iy++) {
                float px = blockX + (ix + 0.5f) / gridSize;
                float py = blockY + (iy + 0.5f) / gridSize;
                float pz = blockZ + random.nextFloat();

                float vx = (random.nextFloat() - 0.5f) * 2.5f;
                float vy = random.nextFloat() * 3.5f + 1.0f;
                float vz = (random.nextFloat() - 0.5f) * 2.5f;

                float size = 0.1f + random.nextFloat() * 0.05f;
                float maxLife = 0.4f + random.nextFloat() * 0.3f;

                Vector2f particleOffset = new Vector2f(
                        baseOffset.x + ix * subScaleX,
                        baseOffset.y + iy * subScaleY
                );
                Vector2f particleScale = new Vector2f(subScaleX, subScaleY);

                add(new Particle(px, py, pz, vx, vy, vz, size, maxLife,
                        particleOffset, particleScale, blocksTexture));
            }
        }
    }

    public void spawn(float x, float y, float z, SpriteSheet cropSheet, int frameIndex) {
        float frameWidthUV = 1.0f / cropSheet.getTotalFrames();
        float baseUvX = frameIndex * frameWidthUV;

        int gridX = 4;
        int gridY = 4;
        float subScaleX = frameWidthUV / gridX;
        float subScaleY = 1.0f / gridY;

        for (int ix = 0; ix < gridX; ix++) {
            for (int iy = 0; iy < gridY; iy++) {
                float px = x + (random.nextFloat() - 0.5f) * 0.6f;
                float py = y + random.nextFloat() * 0.4f;
                float pz = z + (random.nextFloat() - 0.5f) * 0.6f;

                float vx = (random.nextFloat() - 0.5f) * 2.0f;
                float vy = random.nextFloat() * 2.5f + 0.5f;
                float vz = (random.nextFloat() - 0.5f) * 2.0f;

                float size = 0.08f + random.nextFloat() * 0.04f;
                float maxLife = 0.3f + random.nextFloat() * 0.3f;

                Vector2f uvOffset = new Vector2f(
                        baseUvX + ix * subScaleX,
                        iy * subScaleY
                );
                Vector2f uvScale = new Vector2f(subScaleX, subScaleY);

                add(new Particle(px, py, pz, vx, vy, vz, size,
                        maxLife, uvOffset, uvScale, cropSheet));
            }
        }
    }

    public void clear() {
        particles.clear();
    }
}