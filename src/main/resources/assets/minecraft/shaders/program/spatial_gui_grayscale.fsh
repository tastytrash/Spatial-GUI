//? if >=26.3 {
//#version 450
//?} else if >1.21.1 {
#version 330
//?} else {
//#version 150
//?}

//? if >1.21.1 {
uniform sampler2D InSampler;
//?} else {
//uniform sampler2D DiffuseSampler;
//?}

//? if >=26.3 {
//layout(location = 0) in vec2 texCoord;
//layout(location = 0) out vec4 fragColor;
//?} else {
in vec2 texCoord;
out vec4 fragColor;
//?}

void main() {
    //? if >1.21.1 {
    vec4 color = texture(InSampler, texCoord);
    //?} else {
    //vec4 color = texture(DiffuseSampler, texCoord);
    //?}
    float gray = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    fragColor = vec4(vec3(gray), color.a);
}
