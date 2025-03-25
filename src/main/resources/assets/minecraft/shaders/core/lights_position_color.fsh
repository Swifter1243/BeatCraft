#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec3 screenUV;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    vec2 uv = (screenUV.xy / (screenUV.z * 2.0)) + 0.5;
    float depth = texture(Sampler0, uv).r;

    if (gl_FragCoord.z < depth-0.00001) {
        discard;
    }

    fragColor = color;
}
