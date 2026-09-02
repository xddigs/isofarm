#version 330 core
out vec4 FragColor;

in vec2 vTexCoord;

uniform sampler2D uMaskTexture;
uniform vec2 uScreenSize;
uniform vec3 uOutlineColor;

void main() {
    vec4 maskColor = texture(uMaskTexture, vTexCoord);
    if (maskColor.a > 0.5) {
        discard;
    }

    vec2 texel = vec2(1.0 / uScreenSize.x, 1.0 / uScreenSize.y);

    float maxAlpha = 0.0;
    maxAlpha = max(maxAlpha, texture(uMaskTexture, vTexCoord + vec2( texel.x,  0.0)).a);
    maxAlpha = max(maxAlpha, texture(uMaskTexture, vTexCoord + vec2(-texel.x,  0.0)).a);
    maxAlpha = max(maxAlpha, texture(uMaskTexture, vTexCoord + vec2( 0.0,      texel.y)).a);
    maxAlpha = max(maxAlpha, texture(uMaskTexture, vTexCoord + vec2( 0.0,     -texel.y)).a);
    maxAlpha = max(maxAlpha, texture(uMaskTexture, vTexCoord + vec2( texel.x,  texel.y)).a);
    maxAlpha = max(maxAlpha, texture(uMaskTexture, vTexCoord + vec2(-texel.x,  texel.y)).a);
    maxAlpha = max(maxAlpha, texture(uMaskTexture, vTexCoord + vec2( texel.x, -texel.y)).a);
    maxAlpha = max(maxAlpha, texture(uMaskTexture, vTexCoord + vec2(-texel.x, -texel.y)).a);

    if (maxAlpha > 0.5) {
        FragColor = vec4(uOutlineColor, 1.0);
    } else {
        discard;
    }
}