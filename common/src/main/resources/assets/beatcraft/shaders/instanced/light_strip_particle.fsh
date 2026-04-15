#version 330 core

in vec2 v_uv;
in vec4 v_color;
in vec3 v_pos;
in vec3 v_normal;
in vec3 screenUV;
in vec4 v_rect;

uniform int passType; // 0 = normal, 1 = bloom, 2 = bloomfog, 3 = late lights
uniform sampler2D u_texture;
uniform sampler2D u_bloomfog;
uniform sampler2D u_depth;

uniform vec2 u_fog;

out vec4 fragColor;

float clampF(float t) {
    return clamp((t / 100) - 0.001, 0.0, 0.8);
}

vec4 lerpColor(vec4 c1, vec4 c2, float t) {
    return c1 + (c2 * clamp(t, 0.0, 1.0));
}

void main() {
    float x = (v_rect.x + v_rect.z * 2.0) * v_normal.x;
    float y = (v_rect.y + v_rect.w * 2.0) * v_normal.y;

    vec4 texColor = texture(u_texture, v_uv) * v_color;

    float bufW = v_rect.z;
    float bufH = v_rect.w;

    float dLeft = v_normal.x / bufW;
    float dRight = (1.0 - v_normal.x) / bufW;
    float dBottom = v_normal.y / bufH;
    float dTop = (1.0 - v_normal.y) / bufH;

    float glow = clamp(min(min(dLeft, dRight), min(dBottom, dTop)), 0.0, 1.0);

    vec4 glowColor = vec4(v_color.rgb, glow);

    fragColor = texColor * glowColor;
}
