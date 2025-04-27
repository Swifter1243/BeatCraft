#version 150

uniform sampler2D Sampler0;
uniform sampler2D Bloomfog;

uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec4 vertexColor;
in vec2 texCoord0;
in vec3 screenUV;
in vec3 worldPos;

out vec4 fragColor;

float clampF(float t) {
    return min(max(0, (t / 100) - 0.001), 0.8);
}

vec4 lerpColor(vec4 c1, vec4 c2, float t) {
    return c1 + (c2 * min(max(0, t), 1));
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    vec4 bloomfog_color = texture(Bloomfog, (screenUV.xy/(screenUV.z*4))+0.5);

    float fadeHeight = 1 - min(max(0, (worldPos.y + 50) / 20), 1);

    color = lerpColor(color, bloomfog_color, clampF(abs(screenUV.z)) + fadeHeight);
    fragColor = vec4(vec3(fadeHeight), 1);//color;
}
