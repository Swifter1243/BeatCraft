#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec3 screenUV;
out vec2 texCoord0;
out vec3 worldPos;

void main() {
    vec4 pos = vec4(ModelViewMat * vec4(Position, 1.0));
    gl_Position = ProjMat * pos;
    screenUV = vec3(gl_Position.xy, pos.z);
    worldPos = pos.xyz;
    vertexColor = Color;
    texCoord0 = UV0;
}
