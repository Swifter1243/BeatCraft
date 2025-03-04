#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 sum = vec4(0.0);
    float weights[5] = float[](0.227, 0.194, 0.121, 0.054, 0.016);

    for (int i = -2; i <= 2; i++) {
        sum += texture(Sampler0, texCoord0 + vec2(vertexColor.x * i, 0)) * weights[i + 2];
    }

    if (sum.a == 0.0) {
        discard;
    }
    fragColor = sum * ColorModulator;
}

//
//
//#version 150
//
//uniform sampler2D Sampler0;
//
//uniform vec4 ColorModulator;
//
//in vec2 texCoord0;
//
//out vec4 fragColor;
//
//void main() {
//    vec4 color = texture(Sampler0, texCoord0);
//    if (color.a == 0.0) {
//        discard;
//    }
//    fragColor = color * ColorModulator;
//}

