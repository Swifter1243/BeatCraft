#version 310

in vec2 v_uv;
in vec2 tex_coords;
in vec4 v_color;

uniform sampler2D u_texture;

out vec4 fragColor;

void main() {
    vec2 centered = tex_coords * 2.0 - 1.0;
    float dist = length(centered);
    vec4 tex = texture(u_texture, v_uv);

    /* REMOVELINE
    if (dist <= ${r}) {
        fragColor = v_color * tex;
    } else {
        discard;
    }
    */ // REMOVELINE
}