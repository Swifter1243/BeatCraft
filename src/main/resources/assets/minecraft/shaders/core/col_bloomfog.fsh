#version 150

in vec4 vertexColor;
in vec3 screenUV;
in vec3 worldPos;

uniform sampler2D Sampler0;

out vec4 fragColor;

float clampF(float t) {
    return min(max(0, (t / 100) - 0.001), 0.8);
}

vec4 lerpColor(vec4 c1, vec4 c2, float t) {
    return c1 + (c2 * min(max(0, t), 1));
}

void main() {
    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }

    vec4 bloomfog_color = texture(Sampler0, (screenUV.xy/(screenUV.z*4))+0.5);
    float fadeHeight = 1 - min(max(0, (worldPos.y + 50) / 35), 1);


    color = lerpColor(color, bloomfog_color, clampF(abs(screenUV.z)) + fadeHeight);
    fragColor = color;
}
