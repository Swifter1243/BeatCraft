#version 330
// This was copied and translated from shadertoy

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform vec2 texelSize;
uniform float GameTime;

in vec2 texCoord0;

out vec4 fragColor;


void main() {
    vec4 texColor = texture(Sampler0, texCoord0);

    float mask = texture(Sampler1, fract((texCoord0 / (texelSize/2)))).r;

    mask = (mask * 0.25) + 0.75;

    fragColor = vec4(vec3(texColor * mask), 1.0);

}
