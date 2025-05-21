#version 330 core

in vec2 v_uv;
in vec4 v_color;
in float v_dissolve;
in float v_index;
in vec4 v_slice;
in vec3 v_pos;

uniform sampler2D u_texture;

out vec4 fragColor;

void main() {
    if (v_slice.x + v_slice.y + v_slice.z != 0) {
        vec3 n = normalize(v_slice.xyz);
        if (dot(n, v_pos) + v_slice.w < 0.0) {
            discard;
        }
    }

    vec4 tex = texture(u_texture, v_uv);
    fragColor = tex * v_color * (1.0 - v_dissolve);
}
