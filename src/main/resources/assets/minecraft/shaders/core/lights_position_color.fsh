#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec3 screenUV;
in vec3 worldPos;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    vec2 uv = (screenUV.xy / (screenUV.z * 2.0)) + 0.5;
    float depth = texture(Sampler0, uv).r;

    float fadeHeight = min(max(0, (worldPos.y + 50) / 35), 1);

    color *= fadeHeight;

    if (color.a == 0) {
        discard;
    }

    if (gl_FragCoord.z < depth-0.00001) {
        discard;
    }

    fragColor = color;
}
