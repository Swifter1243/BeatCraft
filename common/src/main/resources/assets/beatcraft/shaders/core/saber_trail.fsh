#version 450 core

in vec2 v_uv;

uniform sampler2D u_texture;
// Texture layout:
// [A][B]
// [C][D]
// A.r: primary glow alpha map
// A.g: primary tinted alpha map
// A.b: secondary glow alpha map
// A.a: secondary tinted alpha map
// B: colored overlay
// C: reserved
// D: colored glow overlay

uniform vec4 u_c0;
uniform vec4 u_c1;

void main() {
    vec2 uv = vec2((v_uv.x / 2.0) % 0.5, v_uv.y);



}
