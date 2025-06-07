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
flat in int v_material; // 0 = solid, 1 = light
flat in int v_texture;
in vec3 screenUV;

uniform int passType; // 0 = normal, 1 = bloom
uniform sampler2D u_bloomfog;
uniform sampler2D u_depth;
uniform sampler2D u_texture;

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
        vec4 fog = texture(u_bloomfog, (screenUV.xy/(-screenUV.z*4.0))+0.5);
        float fadeHeight = 1 - clamp((v_pos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);
        fragColor = lerpColor(tex, fog, clampF(abs(screenUV.z)) + fadeHeight);
    } else {
        if (v_material == 0) {
            discard;
        } else {
            vec2 uv = (screenUV.xy / (-screenUV.z * 2)) + 0.5;
            float sceneDepth = texture(u_depth, uv).r;
            if (sceneDepth < gl_FragCoord.z-0.000001) {
                discard;
            }
            vec4 tex = texture(u_texture, v_uv) * v_color;
            float fadeHeight = 1 - clamp((v_pos.y - u_fog.x) / (u_fog.y - u_fog.x), 0.0, 1.0);
            fragColor = lerpColor(tex, vec4(0.0), clampF(abs(screenUV.z)) + fadeHeight);
        }
    }

}