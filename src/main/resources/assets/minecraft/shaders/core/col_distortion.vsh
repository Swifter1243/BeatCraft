#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec3 screenUV;
out vec2 texCoord0;

void main() {
    vec4 pos = vec4(ModelViewMat * vec4(Position, 1.0));
    gl_Position = ProjMat * pos;
    screenUV = vec3(gl_Position.xy, pos.z);
    vertexColor = Color;
    texCoord0 = UV0;
}
