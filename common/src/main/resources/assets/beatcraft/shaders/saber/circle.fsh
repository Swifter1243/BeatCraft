#version 330 core

in vec2 v_uv;
in vec4 v_color;
in vec3 screenUV;

uniform sampler2D u_texture;

out vec4 fragColor;

void main() {
    vec2 centered = v_uv * 2.0 - 1.0;
    float dist = length(centered);

    /* REMOVELINE
    if (dist <= ${r}) {
        fragColor = v_color;
    } else {
        discard;
    }
    */ // REMOVELINE
}