#version 330

layout (location=0) in vec3 inPosition;
out vec2 fragCoord_toy; // specify an output to the fragment shader

uniform vec3 iResolution; // viewport resolution (in pixels)

void main()
{
    fragCoord_toy = (inPosition * iResolution).xy;

    gl_Position = vec4(inPosition, 1.0f);
}