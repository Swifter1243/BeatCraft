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
    vec4 texColor2 = texture(Sampler1, texCoord0);

    fragColor = texColor + texColor2;
}
