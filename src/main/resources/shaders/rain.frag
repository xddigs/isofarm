#version 330 core

out vec4 FragColor;

uniform vec3 uRainColor;

void main() {
    FragColor = vec4(uRainColor, 0.45);
}