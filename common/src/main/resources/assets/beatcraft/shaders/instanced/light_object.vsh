#PC
#version 330 core
#ENDPC
#QUEST
#version 310 es
precision mediump float;
#ENDQUEST

layout(location = 0) in vec4 in_position_u;
layout(location = 1) in vec4 in_normal_v;
layout(location = 2) in ivec3 in_colorLayer_materialLayer_flags;
layout(location = 3) in vec4 clipping_plane;
layout(location = 4) in mat4 instance_model;
//     location = 5          col 2
//     location = 6          col 3
//     location = 7          col 4
layout(location = 8) in vec4 c0;
layout(location = 9) in vec4 c1;
layout(location = 10) in vec4 c2;
layout(location = 11) in vec4 c3;
layout(location = 12) in vec4 c4;
layout(location = 13) in vec4 c5;
layout(location = 14) in vec4 c6;
layout(location = 15) in vec4 c7;

uniform int passType; // 0 = normal, 1 = bloom, 2 = bloomfog
uniform mat4 u_projection;
uniform mat4 u_view;
uniform mat4 world_transform;

out vec2 v_uv;
out vec4 v_color;
out vec3 v_pos;
out vec3 v_normal;
flat out int v_material;
out vec3 screenUV;

void main() {
    vec3 in_position = in_position_u.xyz;
    vec3 in_normal = in_normal_v.xyz;
    vec2 in_uv = vec2(in_position_u.w, in_normal_v.w);

    if (in_colorLayer_materialLayer_flags.y == 1) {
        vec4 colors[8] = vec4[8](c0, c1, c2, c3, c4, c5, c6, c7);
        v_color = colors[clamp(in_colorLayer_materialLayer_flags.x, 0, 7)];
    } else {
        v_color = vec4(1.0);
    }

    vec4 pos = u_view * instance_model * vec4(in_position, 1.0);
    vec4 wp = world_transform * pos;
    gl_ClipDistance[0] = dot(wp, clipping_plane);

    vec4 final = u_projection * pos;
    if (passType == 2) {
        final = vec4(final.xyz/2.0, final.w);
    }
    gl_Position = final;

    v_uv = in_uv;
    screenUV = vec3(final.xy, pos.z);
    v_pos = vec4(wp).xyz;
    v_material = in_colorLayer_materialLayer_flags.y;
}
