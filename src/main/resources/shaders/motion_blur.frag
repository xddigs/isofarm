#version 330 core

in vec2 vTexCoord;

out vec4 FragColor;

uniform sampler2D uScene;
uniform vec2 uVelocity;
uniform float uStrength;

void main() {
    vec2 velocity = uVelocity * uStrength;
    vec4 color = texture(uScene, vTexCoord);
    float total = 1.0;
    const int samples = 8;

    for (int i = 1; i <= samples; i++) {
        float t = float(i) / float(samples);
        vec2 offset = velocity * t;
        color += texture(uScene, vTexCoord + offset);
        color += texture(uScene, vTexCoord - offset);
        total += 2.0;
    }

    FragColor = color / total;
}