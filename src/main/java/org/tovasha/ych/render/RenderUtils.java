package org.tovasha.ych.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.tovasha.ych.api.FontRegistry;

public class RenderUtils {
    public static final Identifier FONT_DEFAULT = Identifier.fromNamespaceAndPath("minecraft", "default");
    public static final Identifier FONT_MODERN = Identifier.fromNamespaceAndPath("ych", "modern");
    public static final Identifier FONT_ARIAL = Identifier.fromNamespaceAndPath("ych", "arial");
    public static final Identifier FONT_ARIAL_BLACK = Identifier.fromNamespaceAndPath("ych", "arialblack");
    public static final Identifier FONT_CODE = Identifier.fromNamespaceAndPath("ych", "code");
    public static final Identifier FONT_BAHNSCHRIFT = Identifier.fromNamespaceAndPath("ych", "bahnschrift");
    public static final Identifier FONT_IMPACT = Identifier.fromNamespaceAndPath("ych", "impact");
    public static final Identifier FONT_COMIC = Identifier.fromNamespaceAndPath("ych", "comic");
    public static final Identifier FONT_TAHOMA = Identifier.fromNamespaceAndPath("ych", "tahoma");
    public static final Identifier FONT_VERDANA = Identifier.fromNamespaceAndPath("ych", "verdana");
    public static final Identifier FONT_TREBUCHET = Identifier.fromNamespaceAndPath("ych", "trebuchet");
    public static final Identifier FONT_CASCADIA = Identifier.fromNamespaceAndPath("ych", "cascadia");
    public static final Identifier FONT_CALIBRI = Identifier.fromNamespaceAndPath("ych", "calibri");
    public static final Identifier FONT_GEORGIA = Identifier.fromNamespaceAndPath("ych", "georgia");

    public static void drawImage(GuiGraphics graphics, String path, float x, float y, float w, float h) {
        drawImage(graphics, path, x, y, w, h, 0.0f);
    }

    public static void drawImage(GuiGraphics graphics, String path, float x, float y, float w, float h, float radius) {
        Identifier textureId = CustomTextureManager.getOrLoadTexture(path);
        if (textureId == null) return;
        drawTexture(graphics, textureId, x, y, w, h, radius, 0.0f, 1.0f, 0.0f, 1.0f, 0xFFFFFFFF);
    }

    public static void drawTexture(GuiGraphics graphics, Identifier textureId, float x, float y, float w, float h, float radius,
                                   float u0, float u1, float v0, float v1, int color) {
        if (graphics == null || textureId == null) return;
        if (radius <= 0.5f) {
            graphics.blit(textureId, (int) x, (int) y, (int) (x + w), (int) (y + h), u0, u1, v0, v1);
            return;
        }

        if (graphics instanceof GuiGraphicsBridge bridge) {
            AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(textureId);
            if (tex != null && tex.getTextureView() != null) {
                TextureSetup setup = TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler());
                GuiRenderState state = bridge.ych$getGuiRenderState();
                ScreenRectangle scissor = bridge.ych$getCurrentScissor();
                state.submitGuiElement(new RoundedTextureRenderState(setup, graphics.pose(), x, y, w, h, radius, u0, u1, v0, v1, color, scissor));
                return;
            }
        }

        graphics.blit(textureId, (int) x, (int) y, (int) (x + w), (int) (y + h), u0, u1, v0, v1);
    }

    public static void drawPlayerHead(GuiGraphics graphics, PlayerSkin skin, float x, float y, float size) {
        drawPlayerHead(graphics, skin, x, y, size, 0.0f);
    }

    public static void drawPlayerHead(GuiGraphics graphics, PlayerSkin skin, float x, float y, float size, float radius) {
        if (graphics == null || skin == null) return;
        if (radius <= 0.5f) {
            PlayerFaceRenderer.draw(graphics, skin, (int) x, (int) y, (int) size);
            return;
        }

        Identifier skinId = skin.body().id();
        drawTexture(graphics, skinId, x, y, size, size, radius, 8.0f / 64.0f, 16.0f / 64.0f, 8.0f / 64.0f, 16.0f / 64.0f, 0xFFFFFFFF);
        drawTexture(graphics, skinId, x, y, size, size, radius, 40.0f / 64.0f, 48.0f / 64.0f, 8.0f / 64.0f, 16.0f / 64.0f, 0xFFFFFFFF);
    }

    public static void drawItem(GuiGraphics graphics, ItemStack stack, float x, float y, float size) {
        if (graphics == null || stack == null || stack.isEmpty()) return;
        if (Math.abs(size - 16.0f) < 0.1f) {
            graphics.renderItem(stack, (int) x, (int) y);
        } else {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            float scale = size / 16.0f;
            graphics.pose().scale(scale, scale);
            graphics.renderItem(stack, 0, 0);
            graphics.pose().popMatrix();
        }
    }

    public static void drawRect(GuiGraphics graphics, float x, float y, float w, float h, int color) {
        drawRoundedRect(graphics, x, y, w, h, 0.0f, color);
    }

    public static void drawGradientRect(GuiGraphics graphics, float x, float y, float w, float h, int colorStart, int colorEnd, boolean horizontal) {
        if (graphics instanceof GuiGraphicsBridge bridge) {
            GuiRenderState state = bridge.ych$getGuiRenderState();
            ScreenRectangle scissor = bridge.ych$getCurrentScissor();
            state.submitGuiElement(new RoundedRectRenderState(graphics.pose(), x, y, w, h, 0.0f, 0.0f, colorStart, colorEnd, horizontal, scissor));
            return;
        }

        if (!horizontal) {
            graphics.fillGradient((int) x, (int) y, (int) (x + w), (int) (y + h), colorStart, colorEnd);
        } else {
            drawRect(graphics, x, y, w, h, colorStart);
        }
    }

    public static void drawRoundedRect(GuiGraphics graphics, float x, float y, float w, float h, float radius, int color) {
        if (graphics instanceof GuiGraphicsBridge bridge) {
            GuiRenderState state = bridge.ych$getGuiRenderState();
            ScreenRectangle scissor = bridge.ych$getCurrentScissor();
            state.submitGuiElement(new RoundedRectRenderState(graphics.pose(), x, y, w, h, radius, 0.0f, color, color, false, scissor));
            return;
        }

        drawRect(graphics, x, y, w, h, color);
    }

    public static void drawOutline(GuiGraphics graphics, float x, float y, float w, float h, float thickness, int color) {
        drawRoundedOutline(graphics, x, y, w, h, 0.0f, thickness, color);
    }

    public static void drawRoundedOutline(GuiGraphics graphics, float x, float y, float w, float h, float radius, float thickness, int color) {
        if (thickness <= 0.0f) {
            return;
        }

        if (graphics instanceof GuiGraphicsBridge bridge) {
            GuiRenderState state = bridge.ych$getGuiRenderState();
            ScreenRectangle scissor = bridge.ych$getCurrentScissor();
            state.submitGuiElement(new RoundedRectRenderState(graphics.pose(), x, y, w, h, radius, thickness, color, color, false, scissor));
            return;
        }

        int xi = (int) x;
        int yi = (int) y;
        int wi = (int) w;
        int hi = (int) h;
        int ti = Math.max(1, (int) thickness);
        graphics.fill(xi, yi, xi + wi, yi + ti, color);
        graphics.fill(xi, yi + hi - ti, xi + wi, yi + hi, color);
        graphics.fill(xi, yi + ti, xi + ti, yi + hi - ti, color);
        graphics.fill(xi + wi - ti, yi + ti, xi + wi, yi + hi - ti, color);
    }

    public static void drawCircle(GuiGraphics graphics, float cx, float cy, float radius, int color) {
        drawRoundedRect(graphics, cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, color);
    }

    public static void drawRing(GuiGraphics graphics, float cx, float cy, float radius, float thickness, int color) {
        drawRoundedOutline(graphics, cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, thickness, color);
    }

    public static void drawArc(GuiGraphics graphics, float cx, float cy, float radius, float thickness, double startAngle, double endAngle, int color) {
        if (graphics instanceof GuiGraphicsBridge bridge) {
            GuiRenderState state = bridge.ych$getGuiRenderState();
            ScreenRectangle scissor = bridge.ych$getCurrentScissor();
            state.submitGuiElement(new ArcRenderState(graphics.pose(), cx, cy, radius, thickness, startAngle, endAngle, color, scissor));
            return;
        }

        drawRing(graphics, cx, cy, radius, thickness, color);
    }

    public static void drawText(GuiGraphics graphics, String text, float x, float y, int color) {
        drawCustomText(graphics, text, x, y, "modern", 9.0f, color);
    }

    public static void drawText(GuiGraphics graphics, String text, float x, float y, float size, int color) {
        drawCustomText(graphics, text, x, y, "modern", size, color);
    }

    public static void drawCustomText(GuiGraphics graphics, String text, float x, float y, String fontId, float size, int color) {
        if (text == null || text.isEmpty()) return;
        Font font = Minecraft.getInstance().font;
        Identifier fontLoc = resolveFont(fontId);
        Component comp = fontLoc != null
                ? Component.literal(text).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(fontLoc)))
                : Component.literal(text);

        float scale = size > 0 ? size / 9.0f : 1.0f;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        if (Math.abs(scale - 1.0f) >= 0.001f) {
            pose.scale(scale, scale);
        }
        graphics.drawString(font, comp, 0, 0, color, false);
        pose.popMatrix();
    }

    public static int getTextWidth(String text) {
        return (int) getTextWidth(text, "modern", 9.0f);
    }

    public static float getTextWidth(String text, String fontId, float size) {
        if (text == null || text.isEmpty()) return 0;
        Font font = Minecraft.getInstance().font;
        Identifier fontLoc = resolveFont(fontId);
        float baseWidth = fontLoc != null
                ? font.width(Component.literal(text).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(fontLoc))))
                : font.width(text);
        float scale = size > 0 ? size / 9.0f : 1.0f;
        return baseWidth * scale;
    }

    public static float getFontHeight(String fontId, float size) {
        float scale = size > 0 ? size / 9.0f : 1.0f;
        return 9.0f * scale;
    }

    public static Identifier resolveFont(String fontId) {
        if (fontId == null) return null;
        String lower = fontId.toLowerCase();
        if (lower.equals("default") || lower.equals("vanilla")) return null;
        if (lower.contains("black")) return FONT_ARIAL_BLACK;
        if (lower.contains("arial")) return FONT_ARIAL;
        if (lower.contains("code") || lower.contains("consola")) return FONT_CODE;
        if (lower.contains("modern") || lower.contains("segoe")) return FONT_MODERN;
        if (lower.contains("bahn")) return FONT_BAHNSCHRIFT;
        if (lower.contains("impact")) return FONT_IMPACT;
        if (lower.contains("comic")) return FONT_COMIC;
        if (lower.contains("tahoma")) return FONT_TAHOMA;
        if (lower.contains("verdana")) return FONT_VERDANA;
        if (lower.contains("trebuchet") || lower.contains("trebuc")) return FONT_TREBUCHET;
        if (lower.contains("cascadia")) return FONT_CASCADIA;
        if (lower.contains("calibri")) return FONT_CALIBRI;
        if (lower.contains("georgia")) return FONT_GEORGIA;
        if (FontRegistry.has(fontId)) return FontRegistry.get(fontId);
        if (FontRegistry.has(lower)) return FontRegistry.get(lower);
        return null;
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
