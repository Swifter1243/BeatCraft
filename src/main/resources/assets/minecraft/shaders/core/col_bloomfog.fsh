#version 150

in vec4 vertexColor;
in vec3 screenUV;
in vec3 worldPos;

uniform sampler2D Sampler0;
uniform vec2 u_fog;

out vec4 fragColor;

float clampF(float t) {
    return clamp((t / 100) - 0.001, 0.0, 0.8);
}

vec4 lerpColor(vec4 c1, vec4 c2, float t) {
    return c1 + (c2 * clamp(t, 0.0, 1.0));
}

void main() {
    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }

    vec4 bloomfog_color = texture(Sampler0, (screenUV.xy/(screenUV.z*4))+0.5);
    float fadeHeight = 1 - clamp((worldPos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);


    color = lerpColor(color, bloomfog_color, clampF(abs(screenUV.z)) + fadeHeight);
    fragColor = color;
}
