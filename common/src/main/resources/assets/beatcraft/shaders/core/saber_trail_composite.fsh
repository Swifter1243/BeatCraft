#versino 450 core

in vec2 v_uv;

in sampler2D u_texture;

out vec4 fragColor;

void main() {
    if (v_uv.x < 0.5) {
        if (v_uv.y < 0.5) {
            // Top left cell
            float r = texture(u_texture, vec2(v_uv.x / 4.0, v_uv.y / 2.0));
            float g = texture(u_texture, vec2(0.25 + (v_uv.x / 4.0), v_uv.y / 2.0));
            float b = texture(u_texture, vec2(v_uv.x / 4.0, 0.5 + (v_uv.y / 2.0)));
            float a = texture(u_texture, vec2(0.25 + (v_uv.x / 4.0), 0.5 + (v_uv.y / 2.0)));
            fragColor = vec4(r, g, b, a);
        } else {
            // Bottom left cell
            discard;
        }
    } else {
        if (v_uv.y < 0.5) {
            // Top right cell
            fragColor = texture(u_texture, vec2(0.5 + (v_uv.x / 4.0), v_uv.y / 2.0));
        } else {
            // Bottom right cell
            fragColor = texture(u_texture, vec2(0.5 + (v_uv.x / 4.0), 0.5 + (v_uv.y / 2.0)));
        }
    }
}
