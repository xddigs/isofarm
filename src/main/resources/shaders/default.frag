#version 330 core
out vec4 FragColor;

in vec3 vNormal;
in vec2 vTexCoord;

uniform sampler2D uTexture;
uniform bool uUseTexture;

uniform vec2 uAtlasScale = vec2(1.0, 1.0);
uniform vec2 uAtlasOffset = vec2(0.0, 0.0);

uniform vec3 uBaseColor;
uniform vec3 uLightDirection;
uniform vec3 uSunColor;
uniform float uLightIntensity;

uniform bool uIsMaskPass;

void main() {
    vec2 clampedTexCoord = clamp(vTexCoord, 0.0001, 0.9999);
    vec2 uv = clampedTexCoord * uAtlasScale + uAtlasOffset;

    vec4 objectColor = uUseTexture ? texture(uTexture, uv) : vec4(uBaseColor, 1.0);

    if (uUseTexture && objectColor.a < 0.1) {
        discard;
    }

    if (uIsMaskPass) {
        FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        return;
    }

    vec3 ambient = 0.25 * uSunColor;
    vec3 norm = normalize(vNormal);
    vec3 lightDir = normalize(-uLightDirection);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * uSunColor;

    vec3 totalLight = (ambient + diffuse) * uLightIntensity;
    vec3 finalColor = totalLight * objectColor.rgb;

    FragColor = vec4(finalColor, objectColor.a);
}