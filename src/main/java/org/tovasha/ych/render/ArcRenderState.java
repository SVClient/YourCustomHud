package org.tovasha.ych.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public class ArcRenderState implements GuiElementRenderState {
    private final Matrix3x2fc pose;
    private final float cx;
    private final float cy;
    private final float radius;
    private final float thickness;
    private final double startAngle;
    private final double endAngle;
    private final int color;
    private final ScreenRectangle scissorArea;
    private final ScreenRectangle bounds;

    public ArcRenderState(Matrix3x2fc pose, float cx, float cy, float radius, float thickness,
                          double startAngleDeg, double endAngleDeg, int color, ScreenRectangle scissorArea) {
        this.pose = new Matrix3x2f(pose);
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.thickness = Math.min(thickness, radius);
        this.startAngle = Math.toRadians(startAngleDeg);
        this.endAngle = Math.toRadians(endAngleDeg);
        this.color = color;
        this.scissorArea = scissorArea;

        int x0 = (int) Math.floor(cx - radius);
        int y0 = (int) Math.floor(cy - radius);
        int size = (int) Math.ceil(radius * 2.0f);
        ScreenRectangle rect = new ScreenRectangle(x0, y0, Math.max(0, size), Math.max(0, size)).transformMaxBounds(this.pose);
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
        double span = endAngle - startAngle;
        if (Math.abs(span) < 0.001) {
            return;
        }

        int segments = Math.max(8, Math.min(128, (int) Math.ceil(Math.abs(span) / (2.0 * Math.PI) * 64.0)));
        double da = span / segments;
        float ro = radius;
        float ri = Math.max(0.0f, radius - thickness);

        for (int i = 0; i < segments; i++) {
            double a1 = startAngle + i * da;
            double a2 = startAngle + (i + 1) * da;
            float cos1 = (float) Math.cos(a1);
            float sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2);
            float sin2 = (float) Math.sin(a2);

            float o1x = cx + ro * cos1;
            float o1y = cy + ro * sin1;
            float o2x = cx + ro * cos2;
            float o2y = cy + ro * sin2;

            float i1x = cx + ri * cos1;
            float i1y = cy + ri * sin1;
            float i2x = cx + ri * cos2;
            float i2y = cy + ri * sin2;

            consumer.addVertexWith2DPose(pose, i1x, i1y).setColor(color);
            consumer.addVertexWith2DPose(pose, i2x, i2y).setColor(color);
            consumer.addVertexWith2DPose(pose, o2x, o2y).setColor(color);
            consumer.addVertexWith2DPose(pose, o1x, o1y).setColor(color);
        }
    }
}
