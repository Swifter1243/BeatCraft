#version 150

uniform sampler2D Sampler0;
uniform vec2 u_fog;

in vec4 vertexColor;
in vec3 screenUV;
in vec3 worldPos;

out vec4 fragColor;

void main() {
    vec2 uv = (screenUV.xy / (-screenUV.z * 2)) + 0.5;

    float sceneDepth = texture(Sampler0, uv).r;

    if (sceneDepth < gl_FragCoord.z-0.00001) {
        discard;
    }

    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }

    float fadeHeight = clamp((worldPos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);

    color *= fadeHeight;

//    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722)); // Luminance
//    if (brightness < 0.3) {
//        discard;
//    }

    fragColor = color;
}
