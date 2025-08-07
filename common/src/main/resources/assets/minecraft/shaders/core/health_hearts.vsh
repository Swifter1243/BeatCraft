#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;
uniform float DoShaking;

out vec2 texCoord0;

float apply_shaking(float v) {
    if (DoShaking < 0.5) {
        return 0.0;
    }

    float r = fract(sin(v * 43758.5453) * 43758.5453);
    return (r < 0.33) ? -1.0 : (r < 0.66) ? 0.0 : 1.0;

}

void main() {
    float shake = (1.0/32.0) * apply_shaking(GameTime + Color.r);
    vec3 pos = Position + vec3(0.0, shake, 0.0);


    vec4 pos2 = vec4(ModelViewMat * vec4(pos, 1.0));
    gl_Position = ProjMat * pos2;

    texCoord0 = UV0;
}
