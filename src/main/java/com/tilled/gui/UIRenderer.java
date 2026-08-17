package com.tilled.gui;

import com.tilled.data.UIElement;
import com.tilled.graphics.Mesh;
import com.tilled.graphics.Shader;
import com.tilled.graphics.SpriteSheet;
import com.tilled.graphics.Texture;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class UIRenderer {
    private final Shader shader;
    private final Mesh mesh;
    private final Matrix4f projection;
    private final Matrix4f model;

    private int screenWidth;
    private int screenHeight;

    public UIRenderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.projection = new Matrix4f();
        this.model = new Matrix4f();

        this.mesh = Mesh.createQuad();
        this.shader = new Shader(
                "shaders/ui.vert",
                "shaders/ui.frag"
        );

        updateProjection();
    }

    public void begin() {
        shader.bind();
        shader.setUniform("uProjection", projection);
    }

    public void end() {
        shader.unbind();
    }

    public void drawRect(float x, float y, float width, float height, Vector4f color) {
        model.identity()
                .translate(x, y, 0.0f)
                .scale(width, height, 1.0f);

        shader.setUniform("uModel", model);
        shader.setUniform("uColor", color);
        shader.setUniform("uUseTexture", false);

        mesh.render();
    }

    public void drawTexture(Texture texture, float x, float y,
                            float width, float height, Vector4f tint) {
        if (texture == null) {
            return;
        }

        texture.bind();

        model.identity()
                .translate(x + width * 0.5f, y + height * 0.5f, 0.0f)
                .scale(width, height, 1.0f);

        shader.setUniform("uModel", model);
        shader.setUniform("uColor", tint);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uFrameIndex", 0);
        shader.setUniform("uTotalFrames", 1);

        mesh.render();

        texture.unbind();
    }

    public void drawSprite(SpriteSheet spriteSheet, int frame, float x, float y,
                           float width, float height, Vector4f tint) {
        if (spriteSheet == null) {
            return;
        }

        spriteSheet.bind();

        model.identity()
                .translate(x + width * 0.5f, y + height * 0.5f, 0.0f)
                .scale(width, height, 1.0f);

        shader.setUniform("uModel", model);
        shader.setUniform("uColor", tint);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uFrameIndex", frame);
        shader.setUniform("uTotalFrames", spriteSheet.getTotalFrames());

        mesh.render();

        spriteSheet.unbind();
    }

    public void render(UIElement element) {
        if (element == null || !element.isActuallyVisible()) {
            return;
        }

        if (element.hasStaticSprite()) {
            drawTexture(element.getSprite(),
                    element.getAbsoluteX(),
                    element.getAbsoluteY(),
                    element.getAbsoluteWidth(),
                    element.getAbsoluteHeight(),
                    element.getTint());
        }

        if (element.hasSpriteSheet()) {
            drawSprite(element.getSpriteSheet(),
                    element.getSpriteFrame(),
                    element.getAbsoluteX(),
                    element.getAbsoluteY(),
                    element.getAbsoluteWidth(),
                    element.getAbsoluteHeight(),
                    element.getTint());
        }
        element.render(this);
    }

    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        updateProjection();
    }

    private void updateProjection() {
        projection.identity().ortho2D(
                0.0f,
                screenWidth,
                screenHeight,
                0.0f
        );
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public Shader getShader() {
        return shader;
    }

    public void dispose() {
        mesh.dispose();
        shader.dispose();
    }
}