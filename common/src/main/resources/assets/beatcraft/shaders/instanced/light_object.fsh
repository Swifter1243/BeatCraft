#version 450 core

in vec2 v_uv;
in vec4 v_color;
in vec3 v_pos;
in vec3 v_normal;
flat in int v_material; // 0 = solid, 1 = light/solid, 2 = light/nothing, 3 = tinted
flat in int v_style;
in vec3 screenUV;

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

    vec4 tex_sample = ((v_style == 0)
        ? texture(u_texture, v_uv)
        : vec4(1.0)) * v_color;

    if (passType == 0 /* Normal */ && v_material != 2 /* Not Light/Nothing */) {
        if (v_material == 1 /* Light/Solid */) {
            tex_sample = vec4(tex_sample.rgb, 1.0);
        }
        vec4 fog = texture(u_bloomfog, (screenUV.xy/(-screenUV.z*4.0))+0.5);
        float fadeHeight = clamp((v_pos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);
        fragColor = lerpColor(tex_sample * fadeHeight, fog, clampF(abs(screenUV.z)));
    } else if (passType == 1 /* Bloom */) {
        if (v_material == 0 /* Solid */) {
            discard;
        } else {
            vec2 uv = (screenUV.xy / (-screenUV.z * 2)) + 0.5;
            float sceneDepth = texture(u_depth, uv).r;
            if (sceneDepth < gl_FragCoord.z-0.000001) {
                discard;
            }
            float fadeHeight = clamp((v_pos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);
            fragColor = lerpColor(tex_sample * fadeHeight, vec4(0.0), clampF(abs(screenUV.z)));
        }
    } else if (passType == 2 /* Bloomfog */) {
        if (v_material == 0 /* Solid */) {
            discard;
        } else {
            fragColor = v_color;
        }
    } else if (passType == 3 /* Late Lights */ && v_material == 2 /* Light/Nothing */) {
        float fadeHeight = clamp((v_pos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);
        fragColor = lerpColor(tex_sample * fadeHeight, vec4(0.0), clampF(abs(screenUV.z)));
    } else {
        discard;
    }

}