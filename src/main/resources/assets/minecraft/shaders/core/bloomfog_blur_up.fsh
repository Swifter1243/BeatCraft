#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec2 blur;

out vec4 fragColor;

vec4 scaleColor(vec4 color) {
    float maxRGB = max(max(color.r, color.g), color.b);

    return vec4(color.rgb / maxRGB, color.a / 1.475);
}

void main() {
    fragColor = scaleColor((
              texture(Sampler0, texCoord0 + vec2( 2.0*blur.x,  0.0       ))
        +     texture(Sampler0, texCoord0 + vec2(-2.0*blur.x,  0.0       ))
        +     texture(Sampler0, texCoord0 + vec2(0.0        ,  2.0*blur.y))
        +     texture(Sampler0, texCoord0 + vec2(0.0        , -2.0*blur.y))
        + 2.0*texture(Sampler0, texCoord0 + vec2( blur.x    ,  blur.y    ))
        + 2.0*texture(Sampler0, texCoord0 + vec2(-blur.x    ,  blur.y    ))
        + 2.0*texture(Sampler0, texCoord0 + vec2( blur.x    , -blur.y    ))
        + 2.0*texture(Sampler0, texCoord0 + vec2(-blur.x    , -blur.y    ))
    ) / 8); //*/

    /*
    gl_FragColor = (    texture2D(gm_BaseTexture, v_vTexcoord + vec2( 2.0*u_vTexel.x,            0.0))
                 +      texture2D(gm_BaseTexture, v_vTexcoord + vec2(-2.0*u_vTexel.x,            0.0))
                 +      texture2D(gm_BaseTexture, v_vTexcoord + vec2(            0.0, 2.0*u_vTexel.y))
                 +      texture2D(gm_BaseTexture, v_vTexcoord + vec2(            0.0,-2.0*u_vTexel.y))
                 +  2.0*texture2D(gm_BaseTexture, v_vTexcoord + vec2(     u_vTexel.x,     u_vTexel.y))
                 +  2.0*texture2D(gm_BaseTexture, v_vTexcoord + vec2(    -u_vTexel.x,     u_vTexel.y))
                 +  2.0*texture2D(gm_BaseTexture, v_vTexcoord + vec2(     u_vTexel.x,    -u_vTexel.y))
                 +  2.0*texture2D(gm_BaseTexture, v_vTexcoord + vec2(    -u_vTexel.x,    -u_vTexel.y))) / 12.0;
*/
}
