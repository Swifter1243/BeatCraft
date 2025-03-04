#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;


vec4 scaleColor(vec4 color) {
    float maxRGB = max(max(color.r, color.g), color.b);

    return vec4(color.rgb / maxRGB, color.a/1.475);
//    if (maxRGB > 1) {
//        return color / (maxRGB);
//    } else {
//        return color;
//    }
}

void main() {
    float[] weights = float[](
        0.227f,
        0.194f,
        0.162f,
        0.131f,
        0.103f,
        0.077f,
        0.055f,
        0.037f,
        0.023f,
        0.013f
    );

    vec4 blurColor = texture(Sampler0, texCoord0) * weights[0];
//    float tw = 0;//weights[0];
    for (int i = 1; i < 10; i++) {
        vec2 offset = vec2(vertexColor.x*float(i), 0);
        blurColor += texture(Sampler0, texCoord0 + offset) * weights[i];
        blurColor += texture(Sampler0, texCoord0 - offset) * weights[i];
//        tw += weights[i]*2;
    }

    blurColor = blurColor / 1.1;

    if (blurColor.a <= 0.1) {
        discard;
    }
    fragColor = scaleColor(blurColor);
}
