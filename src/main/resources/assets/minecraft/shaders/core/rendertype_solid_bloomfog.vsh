#version 150

#moj_import <light.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;

out vec4 vertexColor;
out vec2 texCoord0;
out vec3 screenUV;

void main() {
    vec3 pos = Position + ChunkOffset;
    vec4 pos2 = vec4(ModelViewMat * vec4(pos, 1.0));
    gl_Position = ProjMat * pos2;
    screenUV = vec3(gl_Position.xyz);

    vertexColor = Color * minecraft_sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
}
