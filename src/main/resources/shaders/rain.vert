#version 330 core
layout (location = 0) in vec3 aPos;

uniform mat4 uView;
uniform mat4 uProjection;
uniform vec3 uChunkPos;
uniform float uTime;
uniform float uGroundY;

void main() {
    vec3 worldPos = aPos + uChunkPos;
    float dropSpeed = 28.0;
    float fallVolumeHeight = 20.0;

    float localY = mod(aPos.y - (uTime * dropSpeed), fallVolumeHeight);
    worldPos.y = uChunkPos.y - (fallVolumeHeight - localY);

    if (worldPos.y < uGroundY) {
        worldPos.y = uGroundY;
    }

    gl_Position = uProjection * uView * vec4(worldPos, 1.0);
}