#version 330 core

in vec2 vTexCoord;

out vec4 FragColor;

uniform sampler2D screenTexture;
uniform vec2 uResolution;
uniform vec2 uDirection;
uniform float uBlurRadius;

void main() {
    vec2 texelSize = 1.0 / uResolution;
    vec3 result = vec3(0.0);
    float totalWeight = 0.0;

    for (int i = -10; i <= 10; i++) {
        float offset = float(i);
        float weight = exp(-(offset * offset) / (2.0 * uBlurRadius * uBlurRadius));
        vec2 uv = vTexCoord +
        uDirection * texelSize * offset;

        result += texture(screenTexture, uv).rgb * weight;
        totalWeight += weight;
    }

    result /= totalWeight;

    FragColor = vec4(result, 1.0);
}