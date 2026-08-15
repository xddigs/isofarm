#version 330 core

in vec3 vNormal;
in vec2 vTexCoord;
in vec3 vFragPos;

out vec4 FragColor;

uniform sampler2D uTexture;
uniform bool uUseTexture;
uniform vec3 uBaseColor;

uniform bool uUseFaceAtlas;
uniform vec2 uAtlasScale;
uniform vec2 uTopAtlasOffset;
uniform vec2 uBottomAtlasOffset;
uniform vec2 uSideAtlasOffset;

uniform vec3 uSunColor;
uniform float uLightIntensity;
uniform vec3 uLightDirection;

uniform bool uIsMaskPass;

void main() {
    if (uIsMaskPass) {
        FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        return;
    }

    vec4 texColor = vec4(uBaseColor, 1.0);

    if (uUseTexture) {
        vec2 localUV = vTexCoord;
        vec2 atlasOffset = uSideAtlasOffset;

        if (uUseFaceAtlas) {
            if (vNormal.y > 0.5) {
                atlasOffset = uTopAtlasOffset;
            } else if (vNormal.y < -0.5) {
                atlasOffset = uBottomAtlasOffset;
            } else {
                localUV.y = 1.0 - vTexCoord.y;
            }
        }

        vec2 finalUV = atlasOffset + (localUV * uAtlasScale);
        texColor = texture(uTexture, finalUV);

        if (texColor.a < 0.1) {
            discard;
        }
    }

    vec3 norm = normalize(vNormal);
    vec3 lightDir = normalize(-uLightDirection);
    float diff = max(dot(norm, lightDir), 0.2);
    vec3 diffuse = diff * uSunColor * uLightIntensity;
    FragColor = vec4(texColor.rgb * diffuse, texColor.a);
}