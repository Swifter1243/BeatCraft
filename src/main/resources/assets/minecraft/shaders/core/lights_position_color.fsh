#version 150

uniform sampler2D Sampler0;
uniform vec2 u_fog;

in vec4 vertexColor;
in vec3 screenUV;
in vec3 worldPos;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    vec2 uv = (screenUV.xy / (screenUV.z * 2.0)) + 0.5;
    float depth = texture(Sampler0, uv).r;

    float fadeHeight = clamp((worldPos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);

    color *= fadeHeight;

    if (color.a == 0.0) {
        discard;
    }

    if (gl_FragCoord.z < depth + 0.0001) {
        discard;
    }

    fragColor = color;
}
