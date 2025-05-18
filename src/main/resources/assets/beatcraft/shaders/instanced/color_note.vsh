#version 330 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec2 in_uv;
layout(location = 2) in vec3 in_normal;

layout(location = 3) in vec4 instance_model_row0;
layout(location = 4) in vec4 instance_model_row1;
layout(location = 5) in vec4 instance_model_row2;
layout(location = 6) in vec4 instance_model_row3;
layout(location = 7) in vec4 instance_color;

uniform mat4 u_projection;
uniform mat4 u_view;

out vec2 v_uv;
out vec4 v_color;

void main() {
    mat4 instance_model = mat4(
        instance_model_row0,
        instance_model_row1,
        instance_model_row2,
        instance_model_row3
    );

    gl_Position = u_projection * u_view * instance_model * vec4(in_position, 1.0);
    v_uv = in_uv;
    v_color = instance_color;
}
