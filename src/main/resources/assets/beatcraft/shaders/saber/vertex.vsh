
layout(location = 0) in vec3 in_position;
layout(location = 1) in vec2 in_uv;
layout(location = 2) in vec4 in_color;


uniform mat4 u_projection;
uniform mat4 u_view;

out vec2 v_uv;
out vec4 v_color;
out vec2 tex_coords;

void main() {
    gl_Position = u_projection * u_view * instance_model * vec4(in_position, 1.0);
}