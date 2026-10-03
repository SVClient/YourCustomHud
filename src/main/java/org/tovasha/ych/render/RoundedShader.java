package org.tovasha.ych.render;

import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.tovasha.ych.YourCustomHud;

public class RoundedShader {
    private static int programId = -1;
    private static int vao = -1;
    private static int vbo = -1;
    private static FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(12);

    private static int locSize;
    private static int locLocation;
    private static int locRadius;
    private static int locThickness;
    private static int locSoftness;
    private static int locColor1;
    private static int locColor2;
    private static int locColor3;
    private static int locColor4;
    private static int locOutlineColor;

    private static final String VERTEX_SHADER =
            "#version 150\n" +
            "in vec2 inPos;\n" +
            "void main() {\n" +
            "    gl_Position = vec4(inPos, 0.0, 1.0);\n" +
            "}\n";

    private static final String FRAGMENT_SHADER =
            "#version 150\n" +
            "uniform vec4 color1;\n" +
            "uniform vec4 color2;\n" +
            "uniform vec4 color3;\n" +
            "uniform vec4 color4;\n" +
            "uniform vec4 outlineColor;\n" +
            "uniform vec2 size;\n" +
            "uniform vec2 location;\n" +
            "uniform vec4 radius;\n" +
            "uniform float thickness;\n" +
            "uniform float softness;\n" +
            "out vec4 fragColor;\n" +
            "float roundedBoxSDF(vec2 center, vec2 size, vec4 radius) {\n" +
            "    radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;\n" +
            "    radius.x  = (center.y > 0.0) ? radius.x : radius.y;\n" +
            "    vec2 q = abs(center) - size + radius.x;\n" +
            "    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;\n" +
            "}\n" +
            "void main() {\n" +
            "    vec2 center = gl_FragCoord.xy - location - (size / 2.0);\n" +
            "    float distance = roundedBoxSDF(center, size / 2.0, radius);\n" +
            "    if (thickness > 0.0) {\n" +
            "        float edge0 = max(0.0, thickness - 1.5);\n" +
            "        float edge1 = thickness;\n" +
            "        float borderAlpha = (edge1 > edge0) ? (1.0 - smoothstep(edge0, edge1, abs(distance))) : 1.0;\n" +
            "        float outerAlpha = 1.0 - smoothstep(-0.5, 0.5, distance);\n" +
            "        fragColor = vec4(outlineColor.rgb, outlineColor.a * borderAlpha * outerAlpha);\n" +
            "    } else {\n" +
            "        float alpha = 1.0 - smoothstep(-0.5, 0.5, distance);\n" +
            "        if (alpha <= 0.0) discard;\n" +
            "        vec2 uv = clamp((gl_FragCoord.xy - location) / size, 0.0, 1.0);\n" +
            "        vec4 col = mix(mix(color1, color2, uv.y), mix(color3, color4, uv.y), uv.x);\n" +
            "        fragColor = vec4(col.rgb, col.a * alpha);\n" +
            "    }\n" +
            "}\n";

    public static void init() {
        if (programId != -1) return;

        try {
            int vShader = compileShaderSource(VERTEX_SHADER, GL20.GL_VERTEX_SHADER);
            int fShader = compileShaderSource(FRAGMENT_SHADER, GL20.GL_FRAGMENT_SHADER);

            programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, vShader);
            GL20.glAttachShader(programId, fShader);
            GL20.glBindAttribLocation(programId, 0, "inPos");
            GL20.glLinkProgram(programId);

            if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
                YourCustomHud.LOGGER.error("Failed to link round shader: " + GL20.glGetProgramInfoLog(programId));
                programId = -1;
                return;
            }

            GL20.glDeleteShader(vShader);
            GL20.glDeleteShader(fShader);

            locSize = GL20.glGetUniformLocation(programId, "size");
            locLocation = GL20.glGetUniformLocation(programId, "location");
            locRadius = GL20.glGetUniformLocation(programId, "radius");
            locThickness = GL20.glGetUniformLocation(programId, "thickness");
            locSoftness = GL20.glGetUniformLocation(programId, "softness");
            locColor1 = GL20.glGetUniformLocation(programId, "color1");
            locColor2 = GL20.glGetUniformLocation(programId, "color2");
            locColor3 = GL20.glGetUniformLocation(programId, "color3");
            locColor4 = GL20.glGetUniformLocation(programId, "color4");
            locOutlineColor = GL20.glGetUniformLocation(programId, "outlineColor");

            vao = GL30.glGenVertexArrays();
            vbo = GL15.glGenBuffers();

            GL30.glBindVertexArray(vao);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL30.glBindVertexArray(0);
        } catch (Exception e) {
            YourCustomHud.LOGGER.error("Failed to initialize RoundedShader", e);
            programId = -1;
        }
    }

    private static int compileShaderSource(String source, int type) throws Exception {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, source);
        GL20.glCompileShader(id);

        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(id);
            YourCustomHud.LOGGER.error("Shader compile error: " + log);
            throw new RuntimeException("Failed to compile shader: " + log);
        }

        return id;
    }

    public static boolean isAvailable() {
        if (programId == -1) {
            init();
        }
        return programId != -1;
    }

    public static void render(GuiGraphics graphics, float x, float y, float width, float height,
                              float radius, float thickness, int colorStart, int colorEnd, boolean horizontal, int outlineColor) {
        if (!isAvailable()) return;

        Matrix3x2fStack pose = graphics.pose();
        Vector2f p1 = pose.transformPosition(x, y, new Vector2f());
        Vector2f p2 = pose.transformPosition(x + width, y + height, new Vector2f());

        float actualX = Math.min(p1.x, p2.x);
        float actualY = Math.min(p1.y, p2.y);
        float actualW = Math.abs(p2.x - p1.x);
        float actualH = Math.abs(p2.y - p1.y);

        Minecraft mc = Minecraft.getInstance();
        float guiScale = (float) mc.getWindow().getGuiScale();
        float winHeight = (float) mc.getWindow().getHeight();
        float screenW = (float) mc.getWindow().getGuiScaledWidth();
        float screenH = (float) mc.getWindow().getGuiScaledHeight();

        float locX = actualX * guiScale;
        float locY = winHeight - (actualY + actualH) * guiScale;
        float szX = actualW * guiScale;
        float szY = actualH * guiScale;
        float scaleFactor = height > 0 ? (actualH / height) : 1.0f;
        float rad = radius * scaleFactor * guiScale;
        float thick = thickness * scaleFactor * guiScale;

        float ndcX1 = (actualX / screenW) * 2.0f - 1.0f;
        float ndcY1 = 1.0f - (actualY / screenH) * 2.0f;
        float ndcX2 = ((actualX + actualW) / screenW) * 2.0f - 1.0f;
        float ndcY2 = 1.0f - ((actualY + actualH) / screenH) * 2.0f;

        vertexBuffer.clear();
        vertexBuffer.put(ndcX1).put(ndcY1);
        vertexBuffer.put(ndcX1).put(ndcY2);
        vertexBuffer.put(ndcX2).put(ndcY2);
        vertexBuffer.put(ndcX1).put(ndcY1);
        vertexBuffer.put(ndcX2).put(ndcY2);
        vertexBuffer.put(ndcX2).put(ndcY1);
        vertexBuffer.flip();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL20.glUseProgram(programId);

        GL20.glUniform2f(locSize, szX, szY);
        GL20.glUniform2f(locLocation, locX, locY);
        GL20.glUniform4f(locRadius, rad, rad, rad, rad);
        GL20.glUniform1f(locThickness, thick);
        GL20.glUniform1f(locSoftness, 1.0f);

        float a1 = ((colorStart >> 24) & 0xFF) / 255.0f;
        float r1 = ((colorStart >> 16) & 0xFF) / 255.0f;
        float g1 = ((colorStart >> 8) & 0xFF) / 255.0f;
        float b1 = (colorStart & 0xFF) / 255.0f;

        float a2 = ((colorEnd >> 24) & 0xFF) / 255.0f;
        float r2 = ((colorEnd >> 16) & 0xFF) / 255.0f;
        float g2 = ((colorEnd >> 8) & 0xFF) / 255.0f;
        float b2 = (colorEnd & 0xFF) / 255.0f;

        if (horizontal) {
            GL20.glUniform4f(locColor1, r1, g1, b1, a1);
            GL20.glUniform4f(locColor2, r1, g1, b1, a1);
            GL20.glUniform4f(locColor3, r2, g2, b2, a2);
            GL20.glUniform4f(locColor4, r2, g2, b2, a2);
        } else {
            GL20.glUniform4f(locColor1, r2, g2, b2, a2);
            GL20.glUniform4f(locColor2, r1, g1, b1, a1);
            GL20.glUniform4f(locColor3, r2, g2, b2, a2);
            GL20.glUniform4f(locColor4, r1, g1, b1, a1);
        }

        float ao = ((outlineColor >> 24) & 0xFF) / 255.0f;
        float ro = ((outlineColor >> 16) & 0xFF) / 255.0f;
        float go = ((outlineColor >> 8) & 0xFF) / 255.0f;
        float bo = (outlineColor & 0xFF) / 255.0f;
        GL20.glUniform4f(locOutlineColor, ro, go, bo, ao);

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
        GL11.glDepthMask(true);
    }
}
