#version 330 core

in vec2 vTexCoord;

out vec4 FragColor;

uniform sampler2D uTexture;
uniform bool uUseTexture;
uniform bool uUseFont;

uniform vec4 uColor;

void main() {
    if (uUseFont) {
        float alpha = texture(uTexture, vTexCoord).r;

        if (alpha <= 0.0) {
            discard;
        }

        FragColor = vec4(uColor.rgb, uColor.a * alpha);
        return;
    }

    vec4 color = uColor;

    if (uUseTexture) {
        color *= texture(uTexture, vTexCoord);
    }

    if (color.a <= 0.0) {
        discard;
    }

    FragColor = color;
}