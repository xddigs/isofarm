package com.tilled.graphics;

public record LightingShader(Shader shader) {
    public void bind() {
        shader.bind();
    }

    public void unbind() {
        shader.unbind();
    }

    public void setCelestialLighting(CelestialLighting lighting) {
        shader.setUniform("uLightDirection", lighting.getDirection());
        shader.setUniform("uLightColor", lighting.getColor());
        shader.setUniform("uLightIntensity", lighting.getIntensity());
        shader.setUniform("uAmbientIntensity", lighting.getAmbientIntensity());
    }
}