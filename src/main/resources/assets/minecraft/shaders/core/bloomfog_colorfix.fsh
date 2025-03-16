#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec2 blur;

out vec4 fragColor;

vec4 scaleColor(vec4 color) {
    float maxRGB = max(max(color.r, color.g), color.b);

    if (maxRGB > 1) {
        return vec4(color.rgb / maxRGB, color.a);
    }
    return color;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    fragColor = scaleColor(color);
}