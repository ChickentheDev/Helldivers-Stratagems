#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform OrbitalShockwaveConfig {
    vec2 Center;
    vec3 EnergyColor;
    float Strength;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 delta = texCoord - Center;
    float distanceFromCenter = length(delta);
    vec2 radial = normalize(delta + vec2(0.0001));

    // A fast expanding ring, paired with refraction and color separation.
    float phase = fract(GameTime * 300.0);
    float radius = mix(0.035, 1.05, phase);
    float ring = exp(-abs(distanceFromCenter - radius) * 105.0);
    float innerRing = exp(-abs(distanceFromCenter - radius * 0.72) * 72.0) * 0.45;
    float pulse = max(ring, innerRing) * Strength;

    float spiral = sin(atan(delta.y, delta.x) * 10.0 - GameTime * 1800.0
            + distanceFromCenter * 58.0);
    float distortion = pulse * (0.0045 + max(spiral, 0.0) * 0.002);
    vec2 warpedUv = texCoord + radial * distortion;
    float split = pulse * 0.004;

    vec3 scene;
    scene.r = texture(InSampler, warpedUv + radial * split).r;
    scene.g = texture(InSampler, warpedUv).g;
    scene.b = texture(InSampler, warpedUv - radial * split).b;

    float core = exp(-distanceFromCenter * 8.0) * 0.12 * Strength;
    vec3 energy = EnergyColor * (pulse * 0.35 + core);
    float vignette = (1.0 - smoothstep(0.18, 1.15, distanceFromCenter)) * 0.08 * pulse;

    fragColor = vec4(scene * (1.0 - vignette) + energy, 1.0);
}
