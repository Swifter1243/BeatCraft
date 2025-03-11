#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    vec4 outPos = vec4(ProjMat * vec4(Position.xy, 0.0, 1.0));
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
}
