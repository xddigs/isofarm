#version 330 core

in vec2 vTexCoord;

out vec4 FragColor;

uniform sampler2D uTexture;
uniform bool uUseTexture;

uniform vec4 uColor;

void main() {
    vec4 color = uColor;

    if (uUseTexture) {
        color *= texture(uTexture, vTexCoord);
    }

    if (color.a <= 0.0) {
        discard;
    }

    FragColor = color;
}