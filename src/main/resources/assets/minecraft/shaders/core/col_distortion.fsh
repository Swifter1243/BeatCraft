#version 150

in vec4 vertexColor;
in vec3 screenUV;
in vec2 texCoord0;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    vec2 uv = screenUV.xy*(-screenUV.z)+0.5;

//    vec4 color = 1.0-texture(Sampler0, uv);

    fragColor = vec4(0.0, 0.0, screenUV.z/50.0, 1.0);//vertexColor * ColorModulator + color;
}