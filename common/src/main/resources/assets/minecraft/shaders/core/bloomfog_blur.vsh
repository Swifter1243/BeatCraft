#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 blur;
out vec2 texCoord0;

void main() {
    vec4 pos = vec4(ModelViewMat * vec4(Position, 1.0));
    gl_Position = pos;//vec4(Position, 1.0);

    texCoord0 = UV0;
    blur = Normal.xy/(Normal.z*64);
}
