#PC
#version 330 core
#ENDPC
#QUEST
#version 300 es
precision mediump float;
#ENDQUEST

in vec2 v_uv;
in vec3 v_pos;
in vec3 screenUV;
in float v_delta;

uniform sampler2D u_texture;
uniform sampler2D u_depth;
uniform sampler2D u_bloomfog;

out vec4 fragColor;

float fadeRamp(float t) {
    float rampIn = clamp(t * 16.0, 0.0, 1.0);
    float rampOut = clamp((1.0 - t) * 16.0, 0.0, 1.0);
    return min(rampIn, rampOut);
}

float easeInOutQuad(float x) {
    if (x < 0.5) {
        return 2.0 * x * x;
    } else {
        return 1.0 - pow(-2.0 * x + 2.0, 2.0) / 2.0;
    }
}

vec3 ease(vec3 col) {
    return vec3(
        easeInOutQuad(col.r),
        easeInOutQuad(col.g),
        easeInOutQuad(col.b)
    );
}

void main() {
    vec2 uv = (screenUV.xy / (-screenUV.z * 2.0)) + 0.5;

    float sceneDepth = texture(u_depth, uv).r;

    vec4 tex = texture(u_texture, v_uv);
    vec4 fog = texture(u_bloomfog, (screenUV.xy/(-screenUV.z*4.0))+0.5);

    vec3 t = tex.rgb;

    vec4 col = vec4(tex.rgb * fog.rgb, fog.a * 2.0) * 0.5;
    col = col * fadeRamp(v_delta);
    if (sceneDepth < gl_FragCoord.z-0.000001) {
        discard;
    }
    col = mix(vec4(0.0), col, max(0.0, min(1.0, (sceneDepth - gl_FragCoord.z) * 100.0)));

    fragColor = col;
}
