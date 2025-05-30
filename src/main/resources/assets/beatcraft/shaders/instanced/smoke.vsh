#version 330 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec2 in_uv;
layout(location = 2) in vec3 in_normal;

layout(location = 3) in vec4 instance_model_col0;
layout(location = 4) in vec4 instance_model_col1;
layout(location = 5) in vec4 instance_model_col2;
layout(location = 6) in vec4 instance_model_col3;
layout(location = 7) in float delta;

uniform mat4 u_projection;
uniform mat4 u_view;

out vec2 v_uv;
out vec3 v_pos;
out vec3 screenUV;
out float v_delta;


void main() {
    mat4 instance_model = mat4(
        instance_model_col0,
        instance_model_col1,
        instance_model_col2,
        instance_model_col3
    );

    vec4 pos = vec4(u_view * instance_model * vec4(in_position, 1.0));
    gl_Position = u_projection * pos;
    v_uv = in_uv;
    v_pos = in_position;
    screenUV = vec3(gl_Position.xy, pos.z);
    v_delta = delta;
}
