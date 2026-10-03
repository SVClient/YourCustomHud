#version 150

uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

uniform vec4 outlineColor;
uniform vec2 size;
uniform vec2 location;
uniform vec4 radius;
uniform float thickness;
uniform float softness;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, vec4 radius) {
    radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;
    radius.x  = (center.y > 0.0) ? radius.x : radius.y;

    vec2 q = abs(center) - size + radius.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
}

void main() {
    vec2 center = gl_FragCoord.xy - location - (size / 2.0);
    float distance = roundedBoxSDF(center, size / 2.0, radius);

    if (thickness > 0.0) {
        float edge0 = max(0.0, thickness - 1.5);
        float edge1 = thickness;
        float borderAlpha = (edge1 > edge0) ? (1.0 - smoothstep(edge0, edge1, abs(distance))) : 1.0;
        float outerAlpha = 1.0 - smoothstep(-0.5, 0.5, distance);
        fragColor = vec4(outlineColor.rgb, outlineColor.a * borderAlpha * outerAlpha);
    } else {
        float alpha = 1.0 - smoothstep(-0.5, 0.5, distance);
        if (alpha <= 0.0) {
            discard;
        }
        vec2 uv = clamp((gl_FragCoord.xy - location) / size, 0.0, 1.0);
        vec4 col = mix(mix(color1, color2, uv.y), mix(color3, color4, uv.y), uv.x);
        fragColor = vec4(col.rgb, col.a * alpha);
    }
}
