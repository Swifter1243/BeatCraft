#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec3 screenUV;

uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    vec2 uv = (screenUV.xy / (-screenUV.z * 2)) + 0.5;

    float depth = texture(Sampler0, uv).r;

    if (depth > gl_FragCoord.z) {
        discard;
    }

    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
