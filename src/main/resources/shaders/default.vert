#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aTexCoord;

out vec2 vTexCoord;
out vec3 vNormal;
out vec2 vAtlasOffset;
out vec3 vFragPos;
out vec4 vLightSpacePosition;
out vec2 TexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uLightSpaceMatrix;
uniform vec4 uUVBounds;

uniform int uFrameIndex;
uniform int uTotalFrames;

uniform vec2 uTopAtlasOffset = vec2(0.0, 0.0);
uniform vec2 uBottomAtlasOffset = vec2(0.0, 0.0);
uniform vec2 uSideAtlasOffset = vec2(0.0, 0.0);
uniform bool uUseFaceAtlas = false;

void main() {
    vec4 worldPosition = uModel * vec4(aPos, 1.0);

    gl_Position = uProjection * uView * worldPosition;
    vFragPos = worldPosition.xyz;
    vNormal = normalize(mat3(transpose(inverse(uModel))) * aNormal);
    vLightSpacePosition = uLightSpaceMatrix * worldPosition;

    if (uTotalFrames > 1) {
        float frameWidth = 1.0 / float(uTotalFrames);
        vTexCoord = vec2((aTexCoord.x + float(uFrameIndex)) * frameWidth, aTexCoord.y);
    } else {
        vTexCoord = aTexCoord;
    }

    TexCoord.x = mix(uUVBounds.x, uUVBounds.z, aTexCoord.x);
    TexCoord.y = mix(uUVBounds.y, uUVBounds.w, aTexCoord.y);
}