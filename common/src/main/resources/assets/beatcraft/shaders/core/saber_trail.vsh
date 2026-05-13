#version 450 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec2 in_uv;

uniform mat4 u_view;
uniform mat4 u_projection;
uniform float u_uv_delta;

out vec2 v_uv;

void main() {
    vec4 pos = u_view * vec4(in_position, 1.0);
    gl_Position = u_projection * pos;
    v_uv = vec2(in_uv.x + u_uv_delta, in_uv.y);
}
