#version 150

uniform sampler2D Sampler0;
uniform sampler2D Bloomfog;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 screenUV;

out vec4 fragColor;

vec4 lerpColor(vec4 c1, vec4 c2, float t) {
    return c1 + (c2 * min(max(0, (t / 100) - 0.001), 0.8));
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    vec4 bloomfog_color = texture(Bloomfog, (screenUV.xy/(screenUV.z*4))+0.5);
    color = lerpColor(color, bloomfog_color, abs(screenUV.z));
    fragColor = color * ColorModulator;
}
