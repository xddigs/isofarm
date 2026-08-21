#version 330 core

in float vYPos;
in float vMinY;

uniform vec3 uRainColor;

out vec4 FragColor;

void main() {
    if (vYPos < vMinY) {
        discard;
    }

    FragColor = vec4(uRainColor, 0.4);
}