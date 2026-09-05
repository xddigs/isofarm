#version 330 core

in vec3 vNormal;
in vec2 vTexCoord;
in vec3 vFragPos;
in vec4 vLightSpacePosition;
in float vIsWater;

out vec4 FragColor;

uniform sampler2D uTexture;
uniform vec4 uWaterUVBounds;
uniform sampler2D uShadowMap;

uniform bool uUseTexture;
uniform vec3 uBaseColor;
uniform bool uUseFaceAtlas;

uniform vec3 uSkyColor;
uniform vec3 uSunColor;
uniform float uLightIntensity;
uniform vec3 uLightDirection;
uniform float uAmbientIntensity;

uniform bool uIsMaskPass;
uniform float uParticleAlpha;
uniform bool uEnableShadows;
uniform bool uIsSprite;
uniform bool uIsSubmergedEntity;
uniform bool uIsWater;
uniform vec4 uLavaUVBounds;

float calculateShadow(vec4 lightSpacePosition, vec3 normal) {
    vec3 projectionCoordinates = lightSpacePosition.xyz / lightSpacePosition.w;
    projectionCoordinates = projectionCoordinates * 0.5 + 0.5;

    if (projectionCoordinates.x < 0.0 ||
        projectionCoordinates.x > 1.0 ||
        projectionCoordinates.y < 0.0 ||
        projectionCoordinates.y > 1.0 ||
        projectionCoordinates.z < 0.0 ||
        projectionCoordinates.z > 1.0) {
        return 0.0;
    }

    vec3 lightDir = normalize(-uLightDirection);
    float normalDotLight = max(dot(normal, lightDir), 0.0);
    float bias = max(0.002, 0.008 * (1.0 - normalDotLight));
    float currentDepth = projectionCoordinates.z;
    vec2 texelSize = 1.0 / vec2(textureSize(uShadowMap, 0));

    float shadow = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            float closestDepth = texture(uShadowMap, projectionCoordinates.xy + vec2(x, y) * texelSize).r;
            shadow += (currentDepth - bias > closestDepth) ? 1.0 : 0.0;
        }
    }
    return shadow / 9.0;
}

void main() {
    vec4 texColor = vec4(uBaseColor, 1.0);

    if (uUseTexture) {
        vec2 sampleUV = vTexCoord;
        bool isWaterSurface = uIsWater && abs(normalize(vNormal).y) > 0.5 &&
                              vTexCoord.x >= uWaterUVBounds.x && vTexCoord.x <= uWaterUVBounds.z &&
                              vTexCoord.y >= uWaterUVBounds.y && vTexCoord.y <= uWaterUVBounds.w;
        if (isWaterSurface) {
            // A greedy quad may cover many blocks; repeat the atlas tile once
            // per world block instead of stretching it over the whole coast.
            sampleUV = mix(uWaterUVBounds.xy, uWaterUVBounds.zw, fract(vFragPos.xz));
        }
        texColor = texture(uTexture, sampleUV);
        if (texColor.a < 0.01 && !uIsWater) {
            discard;
        }
    }

    if (uIsMaskPass) {
        FragColor = vec4(1.0);
        return;
    }

    vec3 normal = normalize(vNormal);
    if (!gl_FrontFacing) {
        normal = -normal;
    }

    vec3 lightDir = normalize(-uLightDirection);
    float diffuse = max(dot(normal, lightDir), 0.0);

    if (uIsSprite) {
        diffuse = 0.95;
    }

    float shadow = 0.0;
    if (uEnableShadows && !uIsSprite) {
        shadow = calculateShadow(vLightSpacePosition, normal);
    }

    vec3 ambient = uSkyColor * uAmbientIntensity;
    vec3 directLight = uSunColor * diffuse * uLightIntensity * (1.0 - shadow);
    vec3 totalLight = ambient + directLight;
    float alpha = texColor.a * uParticleAlpha;
    vec3 finalColor = texColor.rgb * totalLight;

    if (vIsWater > 0.5 && uIsWater) {
        bool isLava = vTexCoord.x > uLavaUVBounds.x && vTexCoord.x < uLavaUVBounds.z &&
                      vTexCoord.y > uLavaUVBounds.y && vTexCoord.y < uLavaUVBounds.w;
        alpha = isLava ? 1.0 : 0.80;
    }

    if (uIsSubmergedEntity) {
        finalColor *= vec3(0.65, 0.85, 1.0);
        alpha = uParticleAlpha;
    }

    FragColor = vec4(finalColor, alpha);
}
