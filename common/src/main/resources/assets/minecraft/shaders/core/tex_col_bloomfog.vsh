#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 WorldTransform;

out vec2 texCoord0;
out vec4 vertexColor;
out vec3 screenUV;
out vec3 worldPos;

void main() {
    vec3 pos = Position;
    vec4 pos2 = vec4(ModelViewMat * vec4(pos, 1.0));
    gl_Position = ProjMat * pos2;
    screenUV = vec3(gl_Position.xyz);

    worldPos = vec4(WorldTransform * pos2).xyz;
    vertexColor = Color;
    texCoord0 = UV0;
}
