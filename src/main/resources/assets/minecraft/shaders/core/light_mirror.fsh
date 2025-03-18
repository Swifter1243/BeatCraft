#version 150

in vec4 vertexColor;
in vec4 screenUV;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

out vec4 fragColor;

void main() {
    vec2 uv = (screenUV.xy / (screenUV.w * 2)) + 0.5;  // Convert to normalized UV coordinates
    vec4 color = texture(Sampler0, uv);  // Sample color texture

    // Use gl_FragCoord.z for correct depth comparison
    float fragDepth = gl_FragCoord.z;
    float depthBuffer = texture(Sampler1, uv).r; // Read depth buffer (stored in NDC space 0-1)

    // Cull fragments that are in front of the stored depth
    if (fragDepth > depthBuffer) {
        fragColor = vec4(vec3(0), 1);
    } else {
        fragColor = color;
    }
}


/*#version 150

in vec4 vertexColor;
in vec4 screenUV;

uniform sampler2D Sampler0;
uniform sampler2DShadow Sampler1;
uniform vec4 ColorModulator;

out vec4 fragColor;
//
//vec4 lerpColor(vec4 c1, vec4 c2, float t) {
//    return c1 + (c2 * min(max(0, (t / 100) - 0.001), 0.8));
//}

void main() {
    vec2 uv = (screenUV.xy/(screenUV.w*2))+0.5;
    vec4 color = texture(Sampler0, uv);
    float depth = texture(Sampler1, vec3(uv, screenUV.z));
    if (screenUV.z <= depth) {
        fragColor = vec4(1, 0, 0, 1);
    } else {
        fragColor = color * 0.8;//vec4(depth.r/5.0, screenUV.z/5.0, depth.a/5.0, 1.0);//color * 0.8;
    }
} //*/
