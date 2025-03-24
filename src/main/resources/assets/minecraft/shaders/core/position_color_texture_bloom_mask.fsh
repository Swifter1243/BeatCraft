#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 vertexColor;
in vec3 screenUV;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec2 uv = (screenUV.xy / (-screenUV.z * 2)) + 0.5;

    float sceneDepth = texture(Sampler1, uv).r;

    if (sceneDepth < gl_FragCoord.z-0.000001) {
        discard;
    }

    vec4 color = vertexColor;
    vec4 texColor = texture(Sampler0, texCoord0);

    color *= texColor;

    if (color.a == 0.0) {
        discard;
    }

//    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722)); // Luminance
//    if (brightness < 0.3) {
//        discard;
//    }

    fragColor = color;
}
