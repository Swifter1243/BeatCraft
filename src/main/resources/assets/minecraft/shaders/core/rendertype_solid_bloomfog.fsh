#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Bloomfog;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 screenUV;

out vec4 fragColor;

float lerp(float a, float b, float t) {
    return a + (min(0, max(abs(t), 1)) * (b - a));
}

vec4 lerpColor(vec4 c1, vec4 c2, float t) {
    return vec4(
        lerp(c1.r, c1.r, t),
        lerp(c1.g, c1.g, t),
        lerp(c1.b, c1.b, t),
        c1.a
    );
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    vec4 bloomfog_color = texture(Bloomfog, screenUV.xy);
    color = lerpColor(color, bloomfog_color, screenUV.z);
    fragColor = vec4(0.2, 0.2, 0.2, 1);//linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
