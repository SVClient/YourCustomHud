package org.tovasha.ych.gui;

import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.api.HudRegistry;
import org.tovasha.ych.render.RenderUtils;

@Getter
@Setter
public class HudPreviewWidget {
    private static final Identifier BG_TEXTURE = Identifier.fromNamespaceAndPath("ych", "textures/gui/preview_bg.png");

    private int x;
    private int y;
    private int width;
    private int height;

    private HudElement selectedElement;
    private HudElement draggingElement;
    private double dragOffsetX;
    private double dragOffsetY;
    private Consumer<HudElement> onElementSelected;
    private Runnable onElementMoved;

    public HudPreviewWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private float getScale() {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        if (screenW <= 0) return 1.0f;
        return (float) width / (float) screenW;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTick) {
        graphics.enableScissor(x, y, x + width, y + height);

        graphics.blit(BG_TEXTURE, x, y, x + width, y + height, 0.0f, 1.0f, 0.0f, 1.0f);
        RenderUtils.drawRect(graphics, x, y, width, height, 0x22000000);

        float scale = getScale();

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);

        for (HudElement el : HudRegistry.getElements()) {
            el.render(graphics, deltaTick);

            if (el == selectedElement) {
                RenderUtils.drawRoundedOutline(graphics, el.getX() - 2, el.getY() - 2,
                        el.getWidth() + 4, el.getHeight() + 4, 3, 1.5f / scale, 0xFF3B82F6);
            }
        }

        pose.popMatrix();
        graphics.disableScissor();

        RenderUtils.drawRoundedOutline(graphics, x, y, width, height, 0, 1, Theme.getBorder());
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        if (mx < x || mx > x + width || my < y || my > y + height) {
            return false;
        }

        float scale = getScale();
        double gameMouseX = (mx - x) / scale;
        double gameMouseY = (my - y) / scale;

        List<HudElement> elements = HudRegistry.getElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement el = elements.get(i);
            if (gameMouseX >= el.getX() && gameMouseX <= el.getX() + el.getWidth() &&
                gameMouseY >= el.getY() && gameMouseY <= el.getY() + el.getHeight()) {
                selectedElement = el;
                draggingElement = el;
                dragOffsetX = gameMouseX - el.getX();
                dragOffsetY = gameMouseY - el.getY();
                if (onElementSelected != null) {
                    onElementSelected.accept(el);
                }
                return true;
            }
        }

        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingElement != null) {
            draggingElement = null;
            if (onElementMoved != null) {
                onElementMoved.run();
            }
            return true;
        }
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingElement != null) {
            float scale = getScale();
            double gameMouseX = (event.x() - x) / scale;
            double gameMouseY = (event.y() - y) / scale;

            int newX = (int) (gameMouseX - dragOffsetX);
            int newY = (int) (gameMouseY - dragOffsetY);

            Minecraft mc = Minecraft.getInstance();
            int screenW = mc.getWindow().getGuiScaledWidth();
            int screenH = mc.getWindow().getGuiScaledHeight();

            newX = (int) Math.max(0, Math.min(screenW - draggingElement.getWidth(), newX));
            newY = (int) Math.max(0, Math.min(screenH - draggingElement.getHeight(), newY));

            draggingElement.setX(newX);
            draggingElement.setY(newY);
            return true;
        }
        return false;
    }
}
