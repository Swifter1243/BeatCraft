#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec3 screenUV;
in vec3 worldPos;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    vec2 uv = (screenUV.xy / (screenUV.z * 2.0)) + 0.5;
    float depth = texture(Sampler0, uv).r;

    float fadeHeight = min(max(0, (worldPos.y + 50) / 35), 1);

    color *= fadeHeight;

    if (color.a == 0) {
        discard;
    }

    // Define a depth range for blending
    float depthDiff = depth - gl_FragCoord.z;
    float blendThreshold = 1.0; // Adjust this value to control blend sensitivity

    if (depthDiff < -0.01) {
        // Fragment is behind another object
        // Instead of discard, create a blend factor
        float blendFactor = 1.0 - min(abs(depthDiff) / blendThreshold, 1.0);

        // Apply the blend factor to alpha
        color.a *= blendFactor;

        // Still discard if alpha becomes too low
        if (color.a < 0.01) {
            discard;
        }
    }

    fragColor = color;
}