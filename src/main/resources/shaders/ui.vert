#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

out vec2 vTexCoord;

uniform mat4 uModel;
uniform mat4 uProjection;

uniform int uFrameIndex;
uniform int uFrameRow;
uniform int uFramesPerRow;
uniform int uFrameRows;

uniform bool uUseFont;
uniform bool uUseTexture;
uniform vec4 uGlyphUV;

void main() {
    gl_Position = uProjection * uModel * vec4(aPos, 1.0);

    if (uUseFont) {
        vTexCoord = mix(uGlyphUV.xy, uGlyphUV.zw, aTexCoord);

    } else if (uUseTexture) {
        float columns = float(max(uFramesPerRow, 1));
        float rows = float(max(uFrameRows, 1));

        float frameWidth = 1.0 / columns;
        float frameHeight = 1.0 / rows;

        float column = float(uFrameIndex);
        float row = float(uFrameRow);

        vTexCoord = vec2(
                (column + aTexCoord.x) * frameWidth,
                1.0 - ((row + aTexCoord.y) * frameHeight)
        );

    } else {
        vTexCoord = aTexCoord;
    }
}