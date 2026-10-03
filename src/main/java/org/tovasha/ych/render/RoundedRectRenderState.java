package org.tovasha.ych.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public class RoundedRectRenderState implements GuiElementRenderState {
    private final Matrix3x2fc pose;
    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;
    private final float radius;
    private final float thickness;
    private final int color1;
    private final int color2;
    private final boolean horizontal;
    private final ScreenRectangle scissorArea;
    private final ScreenRectangle bounds;

    public RoundedRectRenderState(Matrix3x2fc pose, float x, float y, float w, float h,
                                  float radius, float thickness, int color1, int color2,
                                  boolean horizontal, ScreenRectangle scissorArea) {
        this.pose = new Matrix3x2f(pose);
        this.x0 = x;
        this.y0 = y;
        this.x1 = x + w;
        this.y1 = y + h;
        this.radius = radius;
        this.thickness = thickness;
        this.color1 = color1;
        this.color2 = color2;
        this.horizontal = horizontal;
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
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
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
        if (thickness > 0.0f) {
            buildOutlineVertices(consumer);
        } else {
            buildFillVertices(consumer);
        }
    }

    private void buildFillVertices(VertexConsumer consumer) {
        float maxR = Math.min((x1 - x0) / 2.0f, (y1 - y0) / 2.0f);
        float r = Math.max(0.0f, Math.min(radius, maxR));

        if (r <= 0.5f) {
            addQuad(consumer, x0, y0, x0, y1, x1, y1, x1, y0);
            return;
        }

        addQuad(consumer, x0 + r, y0, x0 + r, y1, x1 - r, y1, x1 - r, y0);
        addQuad(consumer, x0, y0 + r, x0, y1 - r, x0 + r, y1 - r, x0 + r, y0 + r);
        addQuad(consumer, x1 - r, y0 + r, x1 - r, y1 - r, x1, y1 - r, x1, y0 + r);

        int segments = Math.max(8, Math.min(24, (int) (r * 3)));

        buildCornerFill(consumer, x0 + r, y0 + r, r, Math.PI, 1.5 * Math.PI, segments);
        buildCornerFill(consumer, x1 - r, y0 + r, r, 1.5 * Math.PI, 2.0 * Math.PI, segments);
        buildCornerFill(consumer, x1 - r, y1 - r, r, 0.0, 0.5 * Math.PI, segments);
        buildCornerFill(consumer, x0 + r, y1 - r, r, 0.5 * Math.PI, Math.PI, segments);
    }

    private void buildOutlineVertices(VertexConsumer consumer) {
        float maxR = Math.min((x1 - x0) / 2.0f, (y1 - y0) / 2.0f);
        float r = Math.max(0.0f, Math.min(radius, maxR));
        float th = Math.min(thickness, maxR);
        float ri = Math.max(0.0f, r - th);

        addQuad(consumer, x0 + r, y0, x0 + r, y0 + th, x1 - r, y0 + th, x1 - r, y0);
        addQuad(consumer, x0 + r, y1 - th, x0 + r, y1, x1 - r, y1, x1 - r, y1 - th);
        addQuad(consumer, x0, y0 + r, x0, y1 - r, x0 + th, y1 - r, x0 + th, y0 + r);
        addQuad(consumer, x1 - th, y0 + r, x1 - th, y1 - r, x1, y1 - r, x1, y0 + r);

        if (r > 0.5f) {
            int segments = Math.max(8, Math.min(24, (int) (r * 3)));
            buildCornerOutline(consumer, x0 + r, y0 + r, r, ri, Math.PI, 1.5 * Math.PI, segments);
            buildCornerOutline(consumer, x1 - r, y0 + r, r, ri, 1.5 * Math.PI, 2.0 * Math.PI, segments);
            buildCornerOutline(consumer, x1 - r, y1 - r, r, ri, 0.0, 0.5 * Math.PI, segments);
            buildCornerOutline(consumer, x0 + r, y1 - r, r, ri, 0.5 * Math.PI, Math.PI, segments);
        } else {
            addQuad(consumer, x0, y0, x0, y0 + th, x0 + th, y0 + th, x0 + th, y0);
            addQuad(consumer, x1 - th, y0, x1 - th, y0 + th, x1, y0 + th, x1, y0);
            addQuad(consumer, x1 - th, y1 - th, x1 - th, y1, x1, y1, x1, y1 - th);
            addQuad(consumer, x0, y1 - th, x0, y1, x0 + th, y1, x0 + th, y1 - th);
        }
    }

    private void buildCornerFill(VertexConsumer consumer, float cx, float cy, float r,
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

    private void buildCornerOutline(VertexConsumer consumer, float cx, float cy, float ro, float ri,
                                    double startAngle, double endAngle, int segments) {
        double da = (endAngle - startAngle) / segments;
        for (int i = 0; i < segments; i++) {
            double a1 = startAngle + i * da;
            double a2 = startAngle + (i + 1) * da;
            float o1x = (float) (cx + ro * Math.cos(a1));
            float o1y = (float) (cy + ro * Math.sin(a1));
            float o2x = (float) (cx + ro * Math.cos(a2));
            float o2y = (float) (cy + ro * Math.sin(a2));
            float i1x = (float) (cx + ri * Math.cos(a1));
            float i1y = (float) (cy + ri * Math.sin(a1));
            float i2x = (float) (cx + ri * Math.cos(a2));
            float i2y = (float) (cy + ri * Math.sin(a2));
            addQuad(consumer, i1x, i1y, i2x, i2y, o2x, o2y, o1x, o1y);
        }
    }

    private void addQuad(VertexConsumer consumer,
                         float vx0, float vy0,
                         float vx1, float vy1,
                         float vx2, float vy2,
                         float vx3, float vy3) {
        consumer.addVertexWith2DPose(pose, vx0, vy0).setColor(getColorAt(vx0, vy0));
        consumer.addVertexWith2DPose(pose, vx1, vy1).setColor(getColorAt(vx1, vy1));
        consumer.addVertexWith2DPose(pose, vx2, vy2).setColor(getColorAt(vx2, vy2));
        consumer.addVertexWith2DPose(pose, vx3, vy3).setColor(getColorAt(vx3, vy3));
    }

    private int getColorAt(float x, float y) {
        if (color1 == color2) {
            return color1;
        }
        float t = horizontal
                ? (x - x0) / Math.max(1.0f, x1 - x0)
                : (y - y0) / Math.max(1.0f, y1 - y0);
        return lerpColor(color1, color2, Math.max(0.0f, Math.min(1.0f, t)));
    }

    private static int lerpColor(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int a2 = (c2 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
