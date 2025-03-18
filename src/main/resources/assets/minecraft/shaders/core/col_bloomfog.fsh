#version 150

in vec4 vertexColor;
in vec3 screenUV;

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

out vec4 fragColor;

vec4 lerpColor(vec4 c1, vec4 c2, float t) {
    return c1 + (c2 * min(max(0, (t / 100) - 0.001), 0.8));
}

void main() {
    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    vec4 bloomfog_color = texture(Sampler0, (screenUV.xy/(screenUV.z*2))+0.5);
    color = lerpColor(color, bloomfog_color, abs(screenUV.z));
    fragColor = color;
}
