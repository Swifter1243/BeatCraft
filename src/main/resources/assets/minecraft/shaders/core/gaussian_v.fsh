#version 150


uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform vec2 texelSize;

in vec2 texCoord0;
in vec2 blur;

out vec4 fragColor;


void main() {
    vec4 color = vec4(0.0);
    float offsets[] = float[](
    -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0
    );
    float weights[] = float[](
    0.01621622, 0.05405405, 0.12162162, 0.19459459, 0.22702703,
    0.19459459, 0.12162162, 0.05405405, 0.01621622
    );
    for (int i = 0; i < 9; i++) {
        float offset = offsets[i] * 2.0 * texelSize.y;
        color += texture(Sampler0, texCoord0 + vec2(0.0, offset)) * weights[i];
    }
    fragColor = color;
}