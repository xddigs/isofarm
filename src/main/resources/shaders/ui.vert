#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aTexCoord;

out vec2 vTexCoord;

uniform mat4 uModel;
uniform mat4 uProjection;

uniform bool uUseFont;
uniform bool uUseTexture;

uniform vec4 uGlyphUV;
uniform vec4 uUVBounds;

void main() {
    gl_Position = uProjection * uModel * vec4(aPos, 1.0);

    if (uUseFont) {
        vTexCoord = mix(uGlyphUV.xy,uGlyphUV.zw, aTexCoord);
    } else if (uUseTexture) {
        vTexCoord = mix(uUVBounds.xy, uUVBounds.zw, aTexCoord);
    } else {
        vTexCoord = aTexCoord;
    }
}