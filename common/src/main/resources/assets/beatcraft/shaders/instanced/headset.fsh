#version 330 core

in vec2 v_uv;
in vec3 v_pos;
in vec3 screenUV;

uniform sampler2D u_texture;
uniform sampler2D u_depth;
uniform sampler2D u_bloom_map;
uniform int u_pass;

out vec4 fragColor;

void main() {
    if (u_pass == 0) {
        vec4 c = texture(u_texture, v_uv);
        if (c.a == 0) {
            discard;
        }
        fragColor = c;
    }
    else if (u_pass == 1) {
        vec2 uv = (screenUV.xy / (-screenUV.z * 2.0)) + 0.5;

        float sceneDepth = texture(u_depth, uv).r;

        if (sceneDepth < gl_FragCoord.z-0.000001) {
            discard;
        }
        vec4 c = texture(u_bloom_map, v_uv);
        if (c.a == 0) {
            discard;
        }
        fragColor = c;
    }

}

