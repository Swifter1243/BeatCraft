#version 330 core

in vec2 v_uv;
in vec4 v_color;

uniform sampler2D u_texture;

out vec4 fragColor;

void main() {
    vec4 tex = texture(u_texture, v_uv);
    fragColor = tex * v_color;
}
