package org.tovasha.ych.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public class RoundedTextureRenderState implements GuiElementRenderState {
    private final RenderPipeline pipeline;
    private final TextureSetup textureSetup;
    private final Matrix3x2fc pose;
    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;
    private final float radius;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final int color;
    private final ScreenRectangle scissorArea;
    private final ScreenRectangle bounds;

    public RoundedTextureRenderState(TextureSetup textureSetup, Matrix3x2fc pose,
                                     float x, float y, float w, float h, float radius,
                                     float u0, float u1, float v0, float v1, int color,
                                     ScreenRectangle scissorArea) {
        this.pipeline = RenderPipelines.GUI_TEXTURED;
        this.textureSetup = textureSetup;
        this.pose = new Matrix3x2f(pose);
        this.x0 = x;
        this.y0 = y;
        this.x1 = x + w;
        this.y1 = y + h;
        this.radius = Math.max(0.0f, Math.min(radius, Math.min(w / 2.0f, h / 2.0f)));
        this.u0 = u0;
        this.u1 = u1;
        this.v0 = v0;
        this.v1 = v1;
        this.color = color;
        this.scissorArea = scissorArea;

        int ix0 = (int) Math.floor(x0);
        int iy0 = (int) Math.floor(y0);
        int iw = (int) Math.ceil(x1 - x0);
        int ih = (int) Math.ceil(y1 - y0);
        ScreenRectangle rect = new ScreenRectangle(ix0, iy0, Math.max(0, iw), Math.max(0, ih)).transformMaxBounds(this.pose);
        if (scissorArea != null) {
            rect = scissorArea.intersection(rect);
        }
        this.bounds = rect != null ? rect : new ScreenRectangle(0, 0, 0, 0);
    }

    @Override
    public RenderPipeline pipeline() {
        return pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return textureSetup;
    }

    @Override
    public ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public ScreenRectangle bounds() {
        return bounds;
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        float r = radius;
        if (r <= 0.5f) {
            addQuad(consumer, x0, y0, x0, y1, x1, y1, x1, y0);
            return;
        }

        addQuad(consumer, x0 + r, y0, x0 + r, y1, x1 - r, y1, x1 - r, y0);
        addQuad(consumer, x0, y0 + r, x0, y1 - r, x0 + r, y1 - r, x0 + r, y0 + r);
        addQuad(consumer, x1 - r, y0 + r, x1 - r, y1 - r, x1, y1 - r, x1, y0 + r);

        int segments = Math.max(8, Math.min(24, (int) (r * 3)));
        buildCornerFan(consumer, x0 + r, y0 + r, r, Math.PI, 1.5 * Math.PI, segments);
        buildCornerFan(consumer, x1 - r, y0 + r, r, 1.5 * Math.PI, 2.0 * Math.PI, segments);
        buildCornerFan(consumer, x1 - r, y1 - r, r, 0.0, 0.5 * Math.PI, segments);
        buildCornerFan(consumer, x0 + r, y1 - r, r, 0.5 * Math.PI, Math.PI, segments);
    }

    private void buildCornerFan(VertexConsumer consumer, float cx, float cy, float r,
                                double startAngle, double endAngle, int segments) {
        double da = (endAngle - startAngle) / segments;
        for (int i = 0; i < segments; i++) {
            double a1 = startAngle + i * da;
            double a2 = startAngle + (i + 1) * da;
            float px1 = (float) (cx + r * Math.cos(a1));
            float py1 = (float) (cy + r * Math.sin(a1));
            float px2 = (float) (cx + r * Math.cos(a2));
            float py2 = (float) (cy + r * Math.sin(a2));
            addQuad(consumer, cx, cy, px2, py2, px1, py1, cx, cy);
        }
    }

    private void addQuad(VertexConsumer consumer,
                         float vx0, float vy0,
                         float vx1, float vy1,
                         float vx2, float vy2,
                         float vx3, float vy3) {
        addVertex(consumer, vx0, vy0);
        addVertex(consumer, vx1, vy1);
        addVertex(consumer, vx2, vy2);
        addVertex(consumer, vx3, vy3);
    }

    private void addVertex(VertexConsumer consumer, float x, float y) {
        float u = u0 + ((x - x0) / Math.max(1.0f, x1 - x0)) * (u1 - u0);
        float v = v0 + ((y - y0) / Math.max(1.0f, y1 - y0)) * (v1 - v0);
        consumer.addVertexWith2DPose(pose, x, y).setUv(u, v).setColor(color);
    }
}
