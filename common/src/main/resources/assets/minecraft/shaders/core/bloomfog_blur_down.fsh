#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec2 blur;

out vec4 fragColor;



void main() {
    fragColor = (3.0 *
            texture(Sampler0, texCoord0)
        +   texture(Sampler0, texCoord0 + vec2( blur.x, 0.0    ))
        +   texture(Sampler0, texCoord0 + vec2(-blur.x, 0.0    ))
        +   texture(Sampler0, texCoord0 + vec2(0.0    ,  blur.y))
        +   texture(Sampler0, texCoord0 + vec2(0.0    , -blur.y))
    ) / 6.5;

    /*
    gl_FragColor = (4.0*texture2D(gm_BaseTexture, v_vTexcoord                                 )
                 +      texture2D(gm_BaseTexture, v_vTexcoord + vec2( u_vTexel.x,         0.0))
                 +      texture2D(gm_BaseTexture, v_vTexcoord + vec2(-u_vTexel.x,         0.0))
                 +      texture2D(gm_BaseTexture, v_vTexcoord + vec2(        0.0,  u_vTexel.y))
                 +      texture2D(gm_BaseTexture, v_vTexcoord + vec2(        0.0, -u_vTexel.y))) / 8.0;
    */

}
