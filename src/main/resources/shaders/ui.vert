#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aTexCoord;

out vec2 vTexCoord;

uniform mat4 uModel;
uniform mat4 uProjection;

uniform bool uUsePageTransform;
uniform float uPagePivotX;
uniform float uPageScaleX;
uniform float uPageWidth;
uniform float uPageCurve;

uniform bool uUseFont;
uniform bool uUseTexture;

uniform vec4 uGlyphUV;
uniform vec4 uUVBounds;

void main() {
    vec4 worldPosition = uModel * vec4(aPos, 1.0);
    if (uUsePageTransform) {
        float distanceFromSpine = worldPosition.x - uPagePivotX;
        float pageProgress = clamp(abs(distanceFromSpine) / uPageWidth, 0.0, 1.0);
        worldPosition.x = uPagePivotX + distanceFromSpine * uPageScaleX;
        worldPosition.y -= sin(pageProgress * 3.14159265) * uPageCurve;
    }
    gl_Position = uProjection * worldPosition;

    if (uUseFont) {
        vTexCoord = mix(uGlyphUV.xy,uGlyphUV.zw, aTexCoord);
    } else if (uUseTexture) {
        vTexCoord = mix(uUVBounds.xy, uUVBounds.zw, aTexCoord);
    } else {
        vTexCoord = aTexCoord;
    }
}
