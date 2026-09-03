#version 330 core

in vec2 vTexCoord;

uniform sampler2D uTexture;
uniform bool uAlphaTest;

void main() {
    if (uAlphaTest) {
        float alpha = texture(uTexture, vTexCoord).a;
        if (alpha < 0.01) {
            discard;
        }
    }
}