#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec3 screenUV;
in vec3 worldPos;

out vec4 fragColor;

void main() {
    vec2 uv = (screenUV.xy / (-screenUV.z * 2)) + 0.5;

    float sceneDepth = texture(Sampler0, uv).r;

    if (sceneDepth < gl_FragCoord.z-0.000001) {
        discard;
    }

    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }

    float fadeHeight = min(max(0, (worldPos.y + 50) / 35), 1);

    color *= fadeHeight;

//    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722)); // Luminance
//    if (brightness < 0.3) {
//        discard;
//    }

    fragColor = color;
}
