#version 150


uniform sampler2D Sampler0;
uniform vec2 texelSize;

in vec2 texCoord0;
in vec2 blur;

out vec4 fragColor;


void main() {
    vec4 d = texelSize.xyxy * vec4(1.0, 1.0, -1.0, 0.0);

    vec4 s;
    s  = texture(Sampler0, texCoord0 - d.xy);
    s += texture(Sampler0, texCoord0 - d.wy) * 2.0;
    s += texture(Sampler0, texCoord0 - d.zy);

    s += texture(Sampler0, texCoord0 + d.zw) * 2.0;
    s += texture(Sampler0, texCoord0       ) * 4.0;
    s += texture(Sampler0, texCoord0 + d.xw) * 2.0;

    s += texture(Sampler0, texCoord0 + d.zy);
    s += texture(Sampler0, texCoord0 + d.wy) * 2.0;
    s += texture(Sampler0, texCoord0 + d.xy);

    fragColor = s / 16.0;
}