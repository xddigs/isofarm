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
uniform vec2 uAtlasOffset;

uniform vec3 uSunColor;
uniform float uLightIntensity;
uniform vec3 uLightDirection;

uniform bool uIsMaskPass;

void main() {
    vec4 texColor = vec4(uBaseColor, 1.0);

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
            finalUV = atlasOffset + (vTexCoord * uAtlasScale);

        } else {
            finalUV = vTexCoord;
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

    vec3 norm = normalize(vNormal);
    vec3 lightDir = normalize(-uLightDirection);

    float diff = max(dot(norm, lightDir), 0.0);

    vec3 diffuse = diff * uSunColor * uLightIntensity;
    vec3 ambient = vec3(0.3) * uSunColor;
    vec3 totalLight = ambient + diffuse;

    FragColor = vec4(texColor.rgb * totalLight, texColor.a);
}