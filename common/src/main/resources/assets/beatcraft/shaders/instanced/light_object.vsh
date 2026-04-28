#version 450 core

layout(location =  0) in vec4  in_position_u;
layout(location =  1) in vec4  in_normal_v;
layout(location =  2) in ivec3 in_colorLayer_materialLayer_flags;
layout(location =  3) in vec4  clipping_plane;
layout(location =  4) in mat4  instance_model;
//     location =  5           column 2
//     location =  6           column 3
//     location =  7           column 4
layout(location =  8) in vec4  c0;
layout(location =  9) in vec4  c1;
layout(location = 10) in vec4  c2;
layout(location = 11) in vec4  c3;
layout(location = 12) in vec4  c4;
layout(location = 13) in vec4  c5;
layout(location = 14) in vec4  c6;
layout(location = 15) in vec4  c7;

// Flags:
// 31 : bool : Editor render mode
// 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10  9  8
//  7  6  5  4 : u4 : shader style
//  3  2  1  0 : u4 : billboard index
//

struct BillboardDesc {
    vec4 origin;
    vec4 axis;
    vec4 forward_lock;
}

layout(std430, binding = 0) readonly buffer BillboardBuffer {
    BillboardDesc billboards[15];
}

uniform int passType; // 0 = normal, 1 = bloom, 2 = bloomfog, 3 = late lights
uniform mat4 u_projection;
uniform mat4 u_view;
uniform mat4 u_camera_pos;

out vec2 v_uv;
out vec4 v_color;
out vec3 v_pos;
out vec3 v_normal;
flat out int v_material;
flat out int v_style;
out vec3 screenUV;

void main() {
    vec3 in_position = in_position_u.xyz;
    vec3 in_normal = in_normal_v.xyz;
    vec2 in_uv = vec2(in_position_u.w, in_normal_v.w);

    if (in_colorLayer_materialLayer_flags.y >= 1) {
        vec4 colors[8] = vec4[8](c0, c1, c2, c3, c4, c5, c6, c7);
        v_color = colors[clamp(in_colorLayer_materialLayer_flags.x, 0, 7)];
    } else {
        v_color = vec4(1.0);
    }

    vec4 pos = instance_model * vec4(in_position, 1.0);

    int billboard_idx = in_colorLayer_materialLayer_flags.z & 0xF;
    if (billboard_idx > 0) {
        BillboardDesc bd = billboards[billboard_idx - 1];
        vec3 pivot = bd.origin.xyz;
        vec3 axis = normalize(bd.axis.xyz);
        bool spin = bd.forward_lock.w > 0.5;

        vec3 to_cam = normalize(u_camera_pos - pivot);

        vec3 right, up, forward;

        if (spin) {
            forward = normalize(to_cam - dot(to_cam, axis) * axis);
            right = normalize(cross(axis, forward));
            up = axis;
        } else {
            right = normalize(cross(axis, to_cam));
            forward = normalize(cross(right, axis));
            up = axis;
        }

        float scaleX = length(instance_model[0].xyz);
        float scaleY = length(instance_model[1].xyz);
        float scaleZ = length(instance_model[2].xyz);

        vec3 local = in_position_u.xyz;
        vec3 world_pos = pivot
                       + right   * local.x * scaleX
                       + up      * local.y * scaleY
                       + forward * local.z * scaleZ;
        pos = vec4(world_pos, 1.0);
    }

    pos = u_view * pos;
    vec4 wp = u_camera_pos * pos;
    gl_ClipDistance[0] = dot(wp, clipping_plane);

    vec4 final = u_projection * pos;
    if (passType == 2 /* Bloomfog */) {
        final = vec4(final.xyz/2.0, final.w);
    }
    gl_Position = final;

    v_uv = in_uv;
    screenUV = vec3(final.xy, pos.z);
    v_pos = vec4(wp).xyz;
    v_material = in_colorLayer_materialLayer_flags.y;
    v_style = (in_colorLayer_materialLayer_flags.z & 0xF0) >> 4;
}
