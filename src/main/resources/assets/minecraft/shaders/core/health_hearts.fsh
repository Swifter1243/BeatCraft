#version 330


uniform sampler2D Sampler0;
uniform float GameTime;

in vec2 texCoord0;

out vec4 fragColor;

void main() {

    vec2 containerUV = vec2(fract(texCoord0 / vec2(9.0/36.0, 9.0/45.0))) * vec2(9.0/36.0, 9.0/45.0);

    vec4 containerColor = texture(Sampler0, containerUV);
    vec4 heartColor = texture(Sampler0, texCoord0);

    vec4 color = containerColor + heartColor;

    if (color.a == 0.0) {
        discard;
    }

    fragColor = color;
}