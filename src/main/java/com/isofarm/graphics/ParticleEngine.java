package com.isofarm.graphics;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.Particle;
import com.isofarm.service.Service;
import com.isofarm.utils.K;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

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

    public void render(Shader shader, Mesh quadMesh, CameraView camera) {
        if (particles.isEmpty()) return;

        shader.bind();
        shader.setUniform("uProjection", camera.getProjectionMatrix());
        shader.setUniform("uView", camera.getViewMatrix());
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);

        SpriteSheet currentTexture = null;
        for (Particle p : particles) {
            if (p.getTexture() != null) {
                if (p.getTexture() != currentTexture) {
                    if (currentTexture != null) {
                        currentTexture.unbind();
                    }
                    currentTexture = p.getTexture();
                    currentTexture.bind();
                }
            }

            modelMatrix.identity()
                    .translate(p.getX(), p.getY(), p.getZ())
                    .scale(p.getSize() * p.getAlpha());

            shader.setUniform("uModel", modelMatrix);
            shader.setUniform("uAtlasOffset", p.getUvOffset());
            shader.setUniform("uAtlasScale", p.getUvScale());
            shader.setUniform("uUVBounds", new Vector4f(
                    p.getUvOffset().x,
                    p.getUvOffset().y,
                    p.getUvOffset().x + p.getUvScale().x,
                    p.getUvOffset().y + p.getUvScale().y
            ));

            quadMesh.render();
        }

        if (currentTexture != null) {
            currentTexture.unbind();
        }

        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", true);
        shader.setUniform("uUVBounds", new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        shader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.unbind();
    }

    public void spawnBlock(BlockPos blockPos, BlockData blockData) {
        if (blockData == null || blockData == BlockData.AIR) return;

        TextureAtlas.TextureRegion region = blockData.getSideRegion();
        if (region == null) {
            region = blockData.getTopRegion();
        }

        Vector2f baseOffset = region != null ? region.offset() : new Vector2f(0.0f, 0.0f);
        Vector2f baseScale = region != null ? region.scale() : new Vector2f(1.0f, 1.0f);

        int totalParticles = K.World.MAX_PARTICLES;

        float particleUvWidth = baseScale.x / 4.0f;
        float particleUvHeight = baseScale.y / 4.0f;

        for (int i = 0; i < totalParticles; i++) {
            if (blockPos == null) continue;
            float px = blockPos.x() + random.nextFloat();
            float py = blockPos.y() + random.nextFloat();
            float pz = blockPos.z() + random.nextFloat();

            float vx = (random.nextFloat() - 0.5f) * 2.5f;
            float vy = random.nextFloat() * 2.5f + 1.0f;
            float vz = (random.nextFloat() - 0.5f) * 2.5f;

            float randomU = baseOffset.x + random.nextFloat() * (baseScale.x - particleUvWidth);
            float randomV = baseOffset.y + random.nextFloat() * (baseScale.y - particleUvHeight);

            Vector2f particleOffset = new Vector2f(randomU, randomV);
            Vector2f particleScale = new Vector2f(particleUvWidth, particleUvHeight);

            float size = 0.08f + random.nextFloat() * 0.06f;
            float maxLife = 0.3f + random.nextFloat() * 0.3f;

            add(new Particle(px, py, pz, vx, vy, vz, size, maxLife,
                    particleOffset, particleScale, null));
        }
    }

    public void spawnCrop(float x, float y, float z, SpriteSheet cropSheet, int frameIndex) {
        if (cropSheet == null) return;

        int totalParticles = K.World.MAX_PARTICLES;
        float frameWidthUV = 1.0f / cropSheet.getTotalFrames();
        float baseUvX = frameIndex * frameWidthUV;

        float particleUvWidth = frameWidthUV / 4.0f;
        float particleUvHeight = 1.0f / 4.0f;

        for (int i = 0; i < totalParticles; i++) {
            float px = x + random.nextFloat();
            float py = y + random.nextFloat();
            float pz = z + random.nextFloat();

            float vx = (random.nextFloat() - 0.5f) * 2.5f;
            float vy = random.nextFloat() * 2.5f + 1.0f;
            float vz = (random.nextFloat() - 0.5f) * 2.5f;

            float randomU = baseUvX + random.nextFloat() * (frameWidthUV - particleUvWidth);
            float randomV = random.nextFloat() * (1.0f - particleUvHeight);

            Vector2f particleOffset = new Vector2f(randomU, randomV);
            Vector2f particleScale = new Vector2f(particleUvWidth, particleUvHeight);
            float size = 0.08f + random.nextFloat() * 0.06f;
            float maxLife = 0.3f + random.nextFloat() * 0.3f;

            add(new Particle(px, py, pz, vx, vy, vz, size, maxLife,
                    particleOffset, particleScale, cropSheet));
        }
    }

    public void clear() {
        particles.clear();
    }
}