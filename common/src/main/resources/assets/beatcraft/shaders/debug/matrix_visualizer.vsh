#version 330 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec2 in_uv;
layout(location = 2) in vec3 in_normal;
layout(location = 3) in mat4 instance_model;

uniform mat4 u_projection;
uniform mat4 u_view;

out vec2 v_uv;

void main() {
    gl_Position = u_projection * u_view * instance_model * vec4(in_position, 1.0);
    v_uv = in_uv;
}
