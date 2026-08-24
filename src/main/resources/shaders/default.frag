#version 330 core

in vec3 vNormal;
in vec2 vTexCoord;
in vec3 vFragPos;
in vec4 vLightSpacePosition;

out vec4 FragColor;

uniform sampler2D uTexture;
uniform sampler2D uShadowMap;

uniform bool uUseTexture;
uniform vec3 uBaseColor;

uniform bool uUseFaceAtlas;

uniform vec2 uAtlasScale;
uniform vec2 uTopAtlasOffset;
uniform vec2 uBottomAtlasOffset;
uniform vec2 uSideAtlasOffset;
uniform vec2 uAtlasOffset;

uniform vec3 uSkyColor;
uniform vec3 uSunColor;
uniform float uLightIntensity;
uniform vec3 uLightDirection;
uniform float uAmbientIntensity;

uniform bool uIsMaskPass;
uniform float uParticleAlpha;

uniform bool uEnableShadows;

float calculateShadow(vec4 lightSpacePosition, vec3 normal) {
    vec3 projectionCoordinates = lightSpacePosition.xyz / lightSpacePosition.w;
    projectionCoordinates = projectionCoordinates * 0.5 + 0.5;

    if (projectionCoordinates.z > 1.0) {
        return 0.0;
    }

    if (projectionCoordinates.x < 0.0 ||
        projectionCoordinates.x > 1.0 ||
        projectionCoordinates.y < 0.0 ||
        projectionCoordinates.y > 1.0) {
        return 0.0;
    }

    vec3 lightDir = normalize(-uLightDirection);
    float normalDotLight = max(dot(normal, lightDir), 0.0);
    float bias = max(0.0001, 0.001 * (1.0 - normalDotLight));
    float currentDepth = projectionCoordinates.z;
    vec2 texelSize = 1.0 / vec2(textureSize(uShadowMap, 0));

    float shadow = 0.0;

    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            float closestDepth = texture(uShadowMap, projectionCoordinates.xy + vec2(x, y) * texelSize).r;
            shadow += (currentDepth - bias > closestDepth)? 1.0 : 0.0;
        }
    }

    return shadow / 9.0;
}

void main() {
    vec4 texColor =
    vec4(uBaseColor, 1.0);

    if (uUseTexture) {
        vec2 finalUV;
        if (uUseFaceAtlas) {
            vec2 atlasOffset;
            if (vNormal.y > 0.5) {
                atlasOffset = uTopAtlasOffset;
            } else if (vNormal.y < -0.5) {
                atlasOffset = uBottomAtlasOffset;
            } else {
                atlasOffset = uSideAtlasOffset;
            }

            finalUV =atlasOffset + vTexCoord * uAtlasScale;

        } else {
            finalUV = uAtlasOffset + vTexCoord * uAtlasScale;
        }

        texColor = texture(uTexture, finalUV);
        if (texColor.a < 0.1) {
            discard;
        }
    }

    if (uIsMaskPass) {
        FragColor = vec4(1.0);
        return;
    }

    vec3 normal = normalize(vNormal);
    vec3 lightDir = normalize(-uLightDirection);

    float diffuse =
    max(dot(normal, lightDir), 0.0);

    float shadow = 0.0;

    if (uEnableShadows) {
        shadow =
        calculateShadow(vLightSpacePosition,normal);
    }

    vec3 ambient = uSkyColor * uAmbientIntensity;
    vec3 directLight = uSunColor * diffuse * uLightIntensity * (1.0 - shadow);
    vec3 totalLight = ambient + directLight;
    FragColor = vec4(texColor.rgb * totalLight, texColor.a * uParticleAlpha);
}