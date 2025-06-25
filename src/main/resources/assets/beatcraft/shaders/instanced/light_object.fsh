#PC
#version 330 core
#ENDPC
#QUEST
#version 300 es
precision mediump float;
#ENDQUEST

in vec2 v_uv;
in vec4 v_color;
in vec3 v_pos;
in vec3 v_normal;
flat in int v_material; // 0 = solid, 1 = light
in vec3 screenUV;

uniform int passType; // 0 = normal, 1 = bloom, 2 = bloomfog
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

    if (passType == 0) {
        vec4 tex = texture(u_texture, v_uv) * v_color;
        if (v_material == 1) {
            tex = vec4(tex.rgb, 1.0);
        }
        vec4 fog = texture(u_bloomfog, (screenUV.xy/(-screenUV.z*4.0))+0.5);
        float fadeHeight = clamp((v_pos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);
        fragColor = lerpColor(tex * fadeHeight, fog, clampF(abs(screenUV.z)));
    } else if (passType == 1) {
        if (v_material == 0) {
            discard;
        } else {
            vec2 uv = (screenUV.xy / (-screenUV.z * 2)) + 0.5;
            float sceneDepth = texture(u_depth, uv).r;
            if (sceneDepth < gl_FragCoord.z-0.000001) {
                discard;
            }
            vec4 tex = texture(u_texture, v_uv) * v_color;
            float fadeHeight = clamp((v_pos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);
            fragColor = lerpColor(tex * fadeHeight, vec4(0.0), clampF(abs(screenUV.z)));
        }
    } else {
        if (v_material == 0) {
            discard;
        } else {
            fragColor = v_color;
        }
    }

}