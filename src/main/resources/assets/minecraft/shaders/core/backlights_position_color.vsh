#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 WorldTransform;

out vec4 vertexColor;
out vec3 screenUV;
out vec3 worldPos;

void main() {
    vec4 pos = vec4(ModelViewMat * vec4(Position, 1.0));
    gl_Position = ProjMat * pos;
    screenUV = vec3(gl_Position.xyz);
    worldPos = vec4(WorldTransform * pos).xyz;
    vertexColor = Color;
}
