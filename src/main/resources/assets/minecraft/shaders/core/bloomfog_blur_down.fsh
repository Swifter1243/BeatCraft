#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    fragColor = (4.0 *
            texture(Sampler0, texCoord0)
        +   texture(Sampler0, texCoord0 + vec2( vertexColor.x, 0.0           ))
        +   texture(Sampler0, texCoord0 + vec2(-vertexColor.x, 0.0           ))
        +   texture(Sampler0, texCoord0 + vec2(0.0           ,  vertexColor.y))
        +   texture(Sampler0, texCoord0 + vec2(0.0           , -vertexColor.y))
    ) / 6.5;


}
