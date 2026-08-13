#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

out vec2 vTexCoord;
out vec3 vNormal;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

uniform int uFrameIndex;
uniform int uTotalFrames;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(aPos, 1.0);
    vNormal = mat3(transpose(inverse(uModel))) * aNormal;

    if (uTotalFrames > 1) {
        float frameWidth = 1.0 / float(uTotalFrames);
        vTexCoord = vec2((aTexCoord.x + float(uFrameIndex)) * frameWidth, aTexCoord.y);
    } else {
        vTexCoord = aTexCoord;
    }
}