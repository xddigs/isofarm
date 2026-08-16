package com.tilled.graphics;

import com.tilled.data.BlockData;
import com.tilled.data.Particle;
import com.tilled.service.Service;
import com.tilled.utils.K;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleEngine implements Service<Particle> {
    private final List<Particle> particles = new ArrayList<>();
    private static final Random random = new Random();

    public void add(Particle particle) {
        particles.add(particle);
    }

    public void update(float delta) {
        for (Particle p : particles) {
            p.update(delta);
        }
        particles.removeIf(Particle::isDead);
    }

    public void render(Shader shader, Mesh cubeMesh,
                       SpriteSheet blocksTexture, Matrix4f modelMatrix) {
        if (particles.isEmpty()) return;

        blocksTexture.bind();
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);

        for (Particle p : particles) {
            modelMatrix.identity()
                    .translate(p.getX(), p.getY(), p.getZ())
                    .scale(p.getSize() * p.getAlpha());

            shader.setUniform("uModel", modelMatrix);
            shader.setUniform("uAtlasOffset", p.getUvOffset());
            shader.setUniform("uAtlasScale", p.getUvScale());

            cubeMesh.render();
        }

        blocksTexture.unbind();
    }

    public void spawn(float blockX, float blockY, float blockZ,
                                    BlockData blockData) {
        int count = K.World.PARTICLES_COUNT;
        Vector2f baseOffset = blockData.getSideAtlasOffset();
        Vector2f baseScale = blockData.getAtlasScale();

        for (int i = 0; i < count; i++) {
            float px = blockX + random.nextFloat();
            float py = blockY + random.nextFloat();
            float pz = blockZ + random.nextFloat();

            float vx = (random.nextFloat() - 0.5f) * 2.5f;
            float vy = random.nextFloat() * 3.5f + 1.0f;
            float vz = (random.nextFloat() - 0.5f) * 2.5f;

            float size = 0.1f + random.nextFloat() * 0.05f;
            float maxLife = 0.4f + random.nextFloat() * 0.3f;

            float subUvX = baseOffset.x + (random.nextFloat() * 0.75f) * baseScale.x;
            float subUvY = baseOffset.y + (random.nextFloat() * 0.75f) * baseScale.y;
            Vector2f particleOffset = new Vector2f(subUvX, subUvY);
            Vector2f particleScale = new Vector2f(baseScale.x * 0.25f, baseScale.y * 0.25f);
            add(new Particle(px, py, pz, vx, vy, vz, size, maxLife, particleOffset, particleScale));
        }
    }

    public void spawn(float x, float y, float z,
                      SpriteSheet sheet, int frameIndex) {
        int count = K.World.PARTICLES_COUNT;
        float frameWidthUV = 1.0f / sheet.getTotalFrames();
        float baseUvX = frameIndex * frameWidthUV;

        for (int i = 0; i < count; i++) {
            float px = x + (random.nextFloat() - 0.5f) * 0.6f;
            float py = y + random.nextFloat() * 0.4f;
            float pz = z + (random.nextFloat() - 0.5f) * 0.6f;

            float vx = (random.nextFloat() - 0.5f) * 2.0f;
            float vy = random.nextFloat() * 2.5f + 0.5f;
            float vz = (random.nextFloat() - 0.5f) * 2.0f;

            float size = 0.08f + random.nextFloat() * 0.04f;
            float maxLife = 0.3f + random.nextFloat() * 0.3f;

            float subUvX = baseUvX + (random.nextFloat() * 0.7f) * frameWidthUV;
            float subUvY = random.nextFloat() * 0.7f;

            Vector2f uvOffset = new Vector2f(subUvX, subUvY);
            Vector2f uvScale = new Vector2f(frameWidthUV * 0.3f, 0.3f);
            add(new Particle(px, py, pz, vx, vy, vz, size, maxLife, uvOffset, uvScale));
        }
    }

    public void clear() {
        particles.clear();
    }
}