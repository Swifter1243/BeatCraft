#PC
#version 330 core
#ENDPC
#QUEST
#version 310 es
precision mediump float;
#ENDQUEST

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec2 in_uv;
layout(location = 2) in vec3 in_normal;
layout(location = 3) in mat4 instance_model;
layout(location = 7) in float delta;

uniform mat4 u_projection;
uniform mat4 u_view;

out vec2 v_uv;
out vec3 v_pos;
out vec3 screenUV;
out float v_delta;


void main() {
    vec4 pos = u_view * instance_model * vec4(in_position, 1.0);
    vec4 final = u_projection * pos;
    gl_Position = final;
    v_uv = in_uv;
    v_pos = in_position;
    screenUV = vec3(final.xy, pos.z);
    v_delta = delta;
}
