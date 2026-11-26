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
    const float PIXEL_SIZE = 20.0;
    //? if >1.21.1 {
    vec2 size = vec2(textureSize(InSampler, 0));
    vec2 pixelatedUV = (floor(texCoord * size / PIXEL_SIZE) + 0.5) * PIXEL_SIZE / size;
    fragColor = texture(InSampler, pixelatedUV);
    //?} else {
    //vec2 size = vec2(textureSize(DiffuseSampler, 0));
    //vec2 pixelatedUV = (floor(texCoord * size / PIXEL_SIZE) + 0.5) * PIXEL_SIZE / size;
    //fragColor = texture(DiffuseSampler, pixelatedUV);
    //?}
}
