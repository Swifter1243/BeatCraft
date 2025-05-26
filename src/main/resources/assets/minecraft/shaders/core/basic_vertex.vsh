#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    vec4 pos = vec4(ModelViewMat * vec4(Position, 1.0));
    gl_Position = ProjMat * pos;
    texCoord0 = UV0;
    vertexColor = Color;
}
