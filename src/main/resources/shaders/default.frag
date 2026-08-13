#version 330 core
out vec4 FragColor;

in vec3 vNormal;
in vec2 vTexCoord;

uniform sampler2D uTexture;
uniform bool uUseTexture;

uniform vec3 uBaseColor;
uniform vec3 uLightDirection;
uniform vec3 uSunColor;
uniform float uLightIntensity;

void main() {
    vec4 objectColor = uUseTexture ? texture(uTexture, vTexCoord) : vec4(uBaseColor, 1.0);

    if (objectColor.a < 0.1) {
        discard;
    }

    vec3 ambient = 0.3 * uSunColor;

    vec3 norm = normalize(vNormal);
    vec3 lightDir = normalize(-uLightDirection);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * uSunColor * uLightIntensity;
    vec3 finalColor = (ambient + diffuse) * objectColor.rgb;
    FragColor = vec4(finalColor, objectColor.a);
}