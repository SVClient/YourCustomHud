package org.tovasha.ych.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.api.ParamRegistry;
import org.tovasha.ych.api.ParamRegistry.ParamDefinition;
import org.tovasha.ych.api.VariableRegistry;
import org.tovasha.ych.render.RenderUtils;
import org.tovasha.ych.script.builtins.BuiltinVariables;

@Getter
@Setter
public class InspectorWidget {
    private int x;
    private int y;
    private int width;
    private int height;

    private HudElement element;
    private int paramsScroll = 0;
    private int varsScroll = 0;
    private final BuiltinVariables builtinVariables = new BuiltinVariables();
    private Runnable onParamChanged;

    public InspectorWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        RenderUtils.drawRect(graphics, x, y, width, height, Theme.getBgPanel());

        int halfWidth = width / 2;
        RenderUtils.drawRect(graphics, x + halfWidth, y, 1, height, Theme.getBorder());

        int headerH = 22;
        RenderUtils.drawRect(graphics, x, y, halfWidth, headerH, Theme.getBgHeader());
        RenderUtils.drawRect(graphics, x + halfWidth + 1, y, width - halfWidth - 1, headerH, Theme.getBgHeader());
        RenderUtils.drawRect(graphics, x, y + headerH - 1, width, 1, Theme.getBorder());

        Component paramsComp = Component.translatable("yourcustomhud.params.title");
        String paramsTitle = paramsComp.getString().toUpperCase();
        RenderUtils.drawText(graphics, paramsTitle, x + 10, y + 6, Theme.getTextPrimary());

        Component varsComp = Component.translatable("yourcustomhud.variables.title");
        String varsTitle = varsComp.getString().toUpperCase();
        RenderUtils.drawText(graphics, varsTitle, x + halfWidth + 11, y + 6, Theme.getTextPrimary());

        renderParams(graphics, x + 8, y + headerH + 4, halfWidth - 16, height - headerH - 8, mouseX, mouseY);
        renderVariables(graphics, x + halfWidth + 9, y + headerH + 4, width - halfWidth - 18, height - headerH - 8, mouseX, mouseY);
    }

    private void renderParams(GuiGraphicsExtractor graphics, int px, int py, int pw, int ph, int mouseX, int mouseY) {
        List<ParamItem> items = new ArrayList<>();
        if (element != null) {
            items.add(new ParamItem("Params.x", String.valueOf(element.getX()), false, 0));
            items.add(new ParamItem("Params.y", String.valueOf(element.getY()), false, 0));
            items.add(new ParamItem("Params.width", String.valueOf(element.getWidth()), false, 0));
            items.add(new ParamItem("Params.height", String.valueOf(element.getHeight()), false, 0));
            items.add(new ParamItem("Params.font", element.getFont(), false, 0));

            if (element.getBuiltinParams() != null) {
                int c1 = ((Number) element.getBuiltinParams().getProperty("rainbowColor1")).intValue();
                int c2 = ((Number) element.getBuiltinParams().getProperty("rainbowColor2")).intValue();
                int c3 = ((Number) element.getBuiltinParams().getProperty("rainbowColor3")).intValue();
                items.add(new ParamItem("Params.rainbowColor1", String.format("#%06X", (c1 & 0xFFFFFF)), false, 0, c1));
                items.add(new ParamItem("Params.rainbowColor2", String.format("#%06X", (c2 & 0xFFFFFF)), false, 0, c2));
                items.add(new ParamItem("Params.rainbowColor3", String.format("#%06X", (c3 & 0xFFFFFF)), false, 0, c3));
            }

            Minecraft mc = Minecraft.getInstance();
            int sw = mc.getWindow() != null ? mc.getWindow().getGuiScaledWidth() : 0;
            int sh = mc.getWindow() != null ? mc.getWindow().getGuiScaledHeight() : 0;
            items.add(new ParamItem("Params.screenWidth", String.valueOf(sw), false, 0));
            items.add(new ParamItem("Params.screenHeight", String.valueOf(sh), false, 0));

            for (Entry<String, ParamDefinition> e : ParamRegistry.getAll().entrySet()) {
                items.add(new ParamItem("Params." + e.getKey(), String.valueOf(e.getValue().getDefaultValue()), false, 0));
            }
        }

        int itemH = 16;
        int totalH = items.size() * itemH;
        int maxScroll = Math.max(0, totalH - ph);
        paramsScroll = Math.max(0, Math.min(paramsScroll, maxScroll));

        graphics.enableScissor(px, py, px + pw, py + ph);

        int curY = py - paramsScroll;

        if (items.isEmpty()) {
            Component selectComp = Component.translatable("yourcustomhud.params.select_element");
            RenderUtils.drawText(graphics, selectComp.getString(), px + 4, py + 8, Theme.getTextSecondary());
        } else {
            for (ParamItem item : items) {
                if (curY + itemH >= py && curY <= py + ph) {
                    int nameW = RenderUtils.getTextWidth(item.name);
                    RenderUtils.drawText(graphics, item.name, px, curY + 3, 0xFF38BDF8);
                    int valW = RenderUtils.getTextWidth(item.value);
                    int valX = Math.max(px + nameW + 8, px + pw - valW);

                    if (item.hasColorPreview) {
                        RenderUtils.drawRoundedRect(graphics, valX - 14, curY + 4, 9, 9, 2, item.colorPreview);
                        RenderUtils.drawRoundedOutline(graphics, valX - 14, curY + 4, 9, 9, 2, 1, Theme.getBorder());
                    }

                    graphics.enableScissor(px + nameW + 6, py, px + pw, py + ph);
                    RenderUtils.drawText(graphics, item.value, valX, curY + 3, Theme.getTextPrimary());
                    graphics.disableScissor();
                }
                curY += itemH;
            }
        }

        graphics.disableScissor();
    }

    private void renderVariables(GuiGraphicsExtractor graphics, int vx, int vy, int vw, int vh, int mouseX, int mouseY) {
        List<VarItem> items = new ArrayList<>();
        items.add(new VarItem("Variables.nick", String.valueOf(builtinVariables.getProperty("nick"))));
        items.add(new VarItem("Variables.minecraft_version", String.valueOf(builtinVariables.getProperty("minecraft_version"))));
        items.add(new VarItem("Variables.fps", String.valueOf(builtinVariables.getProperty("fps"))));
        items.add(new VarItem("Variables.tps", String.valueOf(builtinVariables.getProperty("tps"))));
        items.add(new VarItem("Variables.ping", String.valueOf(builtinVariables.getProperty("ping")) + " ms"));
        items.add(new VarItem("Variables.speed", String.valueOf(builtinVariables.getProperty("speed")) + " m/s"));
        items.add(new VarItem("Variables.horizontalSpeed", String.valueOf(builtinVariables.getProperty("horizontalSpeed")) + " m/s"));
        items.add(new VarItem("Variables.verticalSpeed", String.valueOf(builtinVariables.getProperty("verticalSpeed")) + " m/s"));
        items.add(new VarItem("Variables.posX", String.valueOf(builtinVariables.getProperty("posX"))));
        items.add(new VarItem("Variables.posY", String.valueOf(builtinVariables.getProperty("posY"))));
        items.add(new VarItem("Variables.posZ", String.valueOf(builtinVariables.getProperty("posZ"))));
        items.add(new VarItem("Variables.x", String.valueOf(builtinVariables.getProperty("x"))));
        items.add(new VarItem("Variables.y", String.valueOf(builtinVariables.getProperty("y"))));
        items.add(new VarItem("Variables.z", String.valueOf(builtinVariables.getProperty("z"))));
        items.add(new VarItem("Variables.pitch", String.valueOf(builtinVariables.getProperty("pitch"))));
        items.add(new VarItem("Variables.yaw", String.valueOf(builtinVariables.getProperty("yaw"))));
        items.add(new VarItem("Variables.direction", String.valueOf(builtinVariables.getProperty("direction"))));
        items.add(new VarItem("Variables.directionShort", String.valueOf(builtinVariables.getProperty("directionShort"))));
        items.add(new VarItem("Variables.facing", String.valueOf(builtinVariables.getProperty("facing"))));
        items.add(new VarItem("Variables.facingShort", String.valueOf(builtinVariables.getProperty("facingShort"))));
        items.add(new VarItem("Variables.dimension", String.valueOf(builtinVariables.getProperty("dimension"))));
        //items.add(new VarItem("Variables.dim", String.valueOf(builtinVariables.getProperty("dim"))));
        items.add(new VarItem("Variables.oppositeDimension", String.valueOf(builtinVariables.getProperty("oppositeDimension"))));
        //items.add(new VarItem("Variables.oppositeDim", String.valueOf(builtinVariables.getProperty("oppositeDim"))));
        items.add(new VarItem("Variables.oppositeX", String.valueOf(builtinVariables.getProperty("oppositeX"))));
        items.add(new VarItem("Variables.oppositeY", String.valueOf(builtinVariables.getProperty("oppositeY"))));
        items.add(new VarItem("Variables.oppositeZ", String.valueOf(builtinVariables.getProperty("oppositeZ"))));
        items.add(new VarItem("Variables.netherX", String.valueOf(builtinVariables.getProperty("netherX"))));
        items.add(new VarItem("Variables.netherY", String.valueOf(builtinVariables.getProperty("netherY"))));
        items.add(new VarItem("Variables.netherZ", String.valueOf(builtinVariables.getProperty("netherZ"))));
        items.add(new VarItem("Variables.overworldX", String.valueOf(builtinVariables.getProperty("overworldX"))));
        items.add(new VarItem("Variables.overworldY", String.valueOf(builtinVariables.getProperty("overworldY"))));
        items.add(new VarItem("Variables.overworldZ", String.valueOf(builtinVariables.getProperty("overworldZ"))));
        items.add(new VarItem("Variables.biome", String.valueOf(builtinVariables.getProperty("biome"))));
        items.add(new VarItem("Variables.time", String.valueOf(builtinVariables.getProperty("time"))));
        items.add(new VarItem("Variables.rawTime", String.valueOf(builtinVariables.getProperty("rawTime"))));
        items.add(new VarItem("Variables.day", String.valueOf(builtinVariables.getProperty("day"))));
        items.add(new VarItem("Variables.isDay", String.valueOf(builtinVariables.getProperty("isDay"))));
        items.add(new VarItem("Variables.isNight", String.valueOf(builtinVariables.getProperty("isNight"))));
        items.add(new VarItem("Variables.weather", String.valueOf(builtinVariables.getProperty("weather"))));
        items.add(new VarItem("Variables.isClear", String.valueOf(builtinVariables.getProperty("isClear"))));
        items.add(new VarItem("Variables.isRaining", String.valueOf(builtinVariables.getProperty("isRaining"))));
        items.add(new VarItem("Variables.isThundering", String.valueOf(builtinVariables.getProperty("isThundering"))));
        items.add(new VarItem("Variables.cps", String.valueOf(builtinVariables.getProperty("cps"))));
        items.add(new VarItem("Variables.lmbCps", String.valueOf(builtinVariables.getProperty("lmbCps"))));
        items.add(new VarItem("Variables.rmbCps", String.valueOf(builtinVariables.getProperty("rmbCps"))));
        items.add(new VarItem("Variables.cpsRmb", String.valueOf(builtinVariables.getProperty("cpsRmb"))));
        items.add(new VarItem("Variables.hasTarget", String.valueOf(builtinVariables.getProperty("hasTarget"))));
        items.add(new VarItem("Variables.target", String.valueOf(builtinVariables.getProperty("target"))));
        items.add(new VarItem("Variables.potions", String.valueOf(builtinVariables.getProperty("potions"))));
        items.add(new VarItem("Variables.inventory", String.valueOf(builtinVariables.getProperty("inventory"))));

        for (Entry<String, Supplier<Object>> e : VariableRegistry.getAll().entrySet()) {
            Object val = e.getValue() != null ? e.getValue().get() : "null";
            items.add(new VarItem("Variables." + e.getKey(), String.valueOf(val)));
        }

        int itemH = 16;
        int totalH = items.size() * itemH;
        int maxScroll = Math.max(0, totalH - vh);
        varsScroll = Math.max(0, Math.min(varsScroll, maxScroll));

        graphics.enableScissor(vx, vy, vx + vw, vy + vh);

        int curY = vy - varsScroll;

        for (VarItem item : items) {
            if (curY + itemH >= vy && curY <= vy + vh) {
                int nameW = RenderUtils.getTextWidth(item.name);
                RenderUtils.drawText(graphics, item.name, vx, curY + 3, 0xFF4ADE80);
                int valW = RenderUtils.getTextWidth(item.value);
                int valX = Math.max(vx + nameW + 8, vx + vw - valW);
                graphics.enableScissor(vx + nameW + 6, vy, vx + vw, vy + vh);
                RenderUtils.drawText(graphics, item.value, valX, curY + 3, Theme.getTextPrimary());
                graphics.disableScissor();
            }
            curY += itemH;
        }

        graphics.disableScissor();
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        if (mx < x || mx > x + width || my < y || my > y + height) {
            return false;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            int halfWidth = width / 2;
            if (mouseX < x + halfWidth) {
                paramsScroll = Math.max(0, paramsScroll - (int) (verticalAmount * 16));
            } else {
                varsScroll = Math.max(0, varsScroll - (int) (verticalAmount * 16));
            }
            return true;
        }
        return false;
    }

    private static class ParamItem {
        final String name;
        final String value;
        final boolean editable;
        final int id;
        final boolean hasColorPreview;
        final int colorPreview;

        ParamItem(String name, String value, boolean editable, int id) {
            this(name, value, editable, id, false, 0);
        }

        ParamItem(String name, String value, boolean editable, int id, int color) {
            this(name, value, editable, id, true, color);
        }

        ParamItem(String name, String value, boolean editable, int id, boolean hasColorPreview, int colorPreview) {
            this.name = name;
            this.value = value;
            this.editable = editable;
            this.id = id;
            this.hasColorPreview = hasColorPreview;
            this.colorPreview = colorPreview;
        }
    }

    private static class VarItem {
        final String name;
        final String value;

        VarItem(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
