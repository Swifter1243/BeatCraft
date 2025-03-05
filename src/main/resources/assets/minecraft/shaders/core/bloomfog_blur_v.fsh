#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float weights[5] = float[](0.227, 0.194, 0.121, 0.054, 0.016);

    vec4 blurColor = texture(Sampler0, texCoord0) * weights[0];
    for (int i = 0; i < 5; i++) {
        blurColor += texture(Sampler0, texCoord0 + vertexColor.y*float(i)) * weights[i];
        blurColor += texture(Sampler0, texCoord0 - vertexColor.y*float(i)) * weights[i];
    }

    if (blurColor.a < 0.01) {
        discard;
    }
    fragColor = blurColor * ColorModulator;
}
