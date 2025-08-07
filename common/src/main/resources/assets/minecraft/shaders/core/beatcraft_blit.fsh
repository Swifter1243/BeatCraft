#version 330

uniform sampler2D Sampler0;

uniform vec2 texelSize;
uniform float GameTime;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    fragColor = texColor;
}
