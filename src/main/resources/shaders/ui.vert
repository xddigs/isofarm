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
        vTexCoord = mix(
                uGlyphUV.xy,
                uGlyphUV.zw,
                aTexCoord
        );
    } else if (uTotalFrames > 1) {
        float frameWidth = 1.0 / float(uTotalFrames);

        vTexCoord = vec2(
                (aTexCoord.x + float(uFrameIndex)) * frameWidth,
                aTexCoord.y
        );
    } else {
        vTexCoord = aTexCoord;
    }
}