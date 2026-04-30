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
};

layout(std430, binding = 0) readonly buffer BillboardBuffer {
    BillboardDesc billboards[15];
};

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
flat out int v_flags;
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
        vec3 camera_pos = (u_camera_pos * vec4(vec3(0.0), 1.0)).xyz;
        vec3 authored_front = normalize(mat3(instance_model) * bd.forward_lock.xyz);
        vec3 axis = normalize(mat3(instance_model) * bd.axis.xyz);
        vec3 pivot = (instance_model * vec4(bd.origin.xyz, 1.0)).xyz;
        bool spin = bd.forward_lock.w > 0.5;

        vec3 local = (instance_model * vec4(in_position, 1.0)).xyz - pivot;

        vec3 to_cam = normalize(camera_pos - pivot);

        vec3 world_right, world_up, world_forward;

        if (spin) {
            vec3 cam_up = normalize(vec3(u_view[0][1], u_view[1][1], u_view[2][1]));
            world_forward = to_cam;
            vec3 up_hint = (abs(dot(world_forward, cam_up)) < 0.99) ? cam_up : vec3(0.0, 0.0, 1.0);
            world_right = normalize(cross(world_forward, up_hint));
            world_up = normalize(cross(world_right, world_forward));
        } else {
            vec3 proj = to_cam - dot(to_cam, axis) * axis;
            world_forward = (length(proj) > 0.001) ? normalize(proj) : authored_front;
            world_right = normalize(cross(world_forward, axis));
            world_up = axis;
        }

        vec3 helper = (abs(dot(authored_front, axis)) < 0.99) ? axis : vec3(1.0, 0.0, 0.0);
        vec3 local_right = normalize(cross(authored_front, helper));
        vec3 local_up = normalize(cross(local_right, authored_front));

        float cx = dot(local, local_right);
        float cy = dot(local, local_up);
        float cz = dot(local, authored_front);

        vec3 world_pos = pivot
                       + world_right   * cx
                       + world_up      * cy
                       + world_forward * cz;

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
    v_flags = in_colorLayer_materialLayer_flags.z;
}
