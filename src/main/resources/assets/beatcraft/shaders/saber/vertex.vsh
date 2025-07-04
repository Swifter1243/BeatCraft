#version 330 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec2 in_uv;
layout(location = 2) in vec4 in_color;

uniform mat4 u_projection;
uniform mat4 u_view;

out vec2 v_uv;
out vec4 v_color;
out vec3 screenUV;

void main() {
    vec4 pos = vec4(u_view * vec4(in_position, 1.0));
    vec4 final = u_projection * pos;
    gl_Position = final;
    screenUV = vec3(final.xy, pos.z);
    v_uv = in_uv;
    v_color = in_color;
}