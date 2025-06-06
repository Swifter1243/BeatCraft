#PC
#version 330 core
#ENDPC
#QUEST
#version 300 es
#ENDQUEST

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec2 in_uv;
layout(location = 2) in vec3 in_normal;
layout(location = 3) in ivec3 in_colorLayer_materialLayer_texture;
#PC
layout(location = 4) in mat4 instance_model;
#ENDPC
#QUEST
layout(location = 4) in vec4 instance_model_0;
layout(location = 5) in vec4 instance_model_1;
layout(location = 6) in vec4 instance_model_2;
layout(location = 7) in vec4 instance_model_3;
#ENDQUEST
layout(location = 8) in vec4 c0;
layout(location = 9) in vec4 c1;
layout(location = 10) in vec4 c2;
layout(location = 11) in vec4 c3;
layout(location = 12) in vec4 c4;
layout(location = 13) in vec4 c5;
layout(location = 14) in vec4 c6;
layout(location = 15) in vec4 c7;

uniform mat4 u_projection;
uniform mat4 u_view;

out vec2 v_uv;
out vec4 v_color;
out vec3 v_pos;
flat out int v_material;
out vec3 screenUV;
flat out int v_texture;

void main() {
#QUEST
    mat4 instance_model = mat4(
        instance_model_0,
        instance_model_1,
        instance_model_2,
        instance_model_3
    );
#ENDQUEST

    if (in_colorLayer_materialLayer_texture.y == 0) {
        vec4 colors[8] = vec4[8](c0, c1, c2, c3, c4, c5, c6, c7);
        v_color = colors[clamp(in_colorLayer_materialLayer_texture.x, 0, 7)];
    } else {
        v_color = vec4(1.0);
    }

    vec4 pos = u_view * instance_model * vec4(in_position, 1.0);
    gl_Position = u_projection * pos;
    v_uv = in_uv;
    screenUV = vec3(gl_Position.xy, pos.z);
    v_pos = pos.xyz;
    v_material = in_colorLayer_materialLayer_texture.y;
    v_texture = in_colorLayer_materialLayer_texture.z;
}
