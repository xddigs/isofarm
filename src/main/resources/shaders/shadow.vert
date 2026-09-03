#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 2) in vec2 aTexCoord;

out vec2 vTexCoord;

uniform mat4 uModel;
uniform mat4 uLightSpaceMatrix;
uniform vec4 uUVBounds;

void main() {
    gl_Position = uLightSpaceMatrix * uModel * vec4(aPos, 1.0);
    vTexCoord = mix(uUVBounds.xy, uUVBounds.zw, aTexCoord);
}