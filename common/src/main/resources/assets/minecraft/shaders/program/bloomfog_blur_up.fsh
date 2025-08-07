#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

vec4 scaleColor(vec4 color) {
    float maxRGB = max(max(color.r, color.g), color.b);

    return vec4(color.rgb / maxRGB, color.a / 1.475);
}

void main() {
    fragColor = scaleColor((
              texture(DiffuseSampler, texCoord0 + vec2( 2.0*vertexColor.x,  0.0              ))
        +     texture(DiffuseSampler, texCoord0 + vec2(-2.0*vertexColor.x,  0.0              ))
        +     texture(DiffuseSampler, texCoord0 + vec2(0.0               ,  2.0*vertexColor.y))
        +     texture(DiffuseSampler, texCoord0 + vec2(0.0               , -2.0*vertexColor.y))
        + 2.0*texture(DiffuseSampler, texCoord0 + vec2( vertexColor.x    ,  vertexColor.y    ))
        + 2.0*texture(DiffuseSampler, texCoord0 + vec2(-vertexColor.x    ,  vertexColor.y    ))
        + 2.0*texture(DiffuseSampler, texCoord0 + vec2( vertexColor.x    , -vertexColor.y    ))
        + 2.0*texture(DiffuseSampler, texCoord0 + vec2(-vertexColor.x    , -vertexColor.y    ))
    ) / 8);

}
