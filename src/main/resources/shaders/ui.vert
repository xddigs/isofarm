#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

out vec2 vTexCoord;

uniform mat4 uModel;
uniform mat4 uProjection;

uniform int uFrameIndex;
uniform int uTotalFrames;

uniform bool uUseFont;
uniform vec4 uGlyphUV;

void main() {
    gl_Position = uProjection * uModel * vec4(aPos, 1.0);

    if (uUseFont) {
        vTexCoord = mix(uGlyphUV.xy, uGlyphUV.zw, aTexCoord);
    } else {
        float frameWidth = 1.0 / float(max(uTotalFrames, 1));
        vTexCoord = vec2((float(uFrameIndex) + aTexCoord.x) * frameWidth, 1.0 - aTexCoord.y);
    }
}