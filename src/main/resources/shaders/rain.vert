#version 330 core

layout(location = 0) in vec3 aPosition;

uniform mat4 uProjection;
uniform mat4 uView;
uniform vec3 uChunkPos;
uniform float uTime;

out float vYPos;
out float vMinY;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec3 worldPos = aPosition + uChunkPos;
    float fallSpeed = 28.0;
    float range = 25.0;

    float offset = mod(uTime * fallSpeed + hash(aPosition.xz) * 50.0, range);
    worldPos.y -= offset;

    vYPos = worldPos.y;
    gl_Position = uProjection * uView * vec4(worldPos, 1.0);
}