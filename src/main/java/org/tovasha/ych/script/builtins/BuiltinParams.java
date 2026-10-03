package org.tovasha.ych.script.builtins;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.api.ParamRegistry;

public class BuiltinParams implements ScriptNamespace {
    private final HudElement element;
    private final Map<String, Object> customParams = new HashMap<>();

    public BuiltinParams(HudElement element) {
        this.element = element;
    }

    @Override
    public Object getProperty(String name) {
        Minecraft mc = Minecraft.getInstance();
        switch (name) {
            case "screenWidth":
                return mc.getWindow() != null ? (double) mc.getWindow().getGuiScaledWidth() : 854.0;
            case "screenHeight":
                return mc.getWindow() != null ? (double) mc.getWindow().getGuiScaledHeight() : 480.0;
            case "x":
                return (double) element.getX();
            case "y":
                return (double) element.getY();
            case "width":
                return (double) element.getWidth();
            case "height":
                return (double) element.getHeight();
            case "font":
                return element.getFont();
            case "rainbowColor1":
                return (double) computeRainbow(0L);
            case "rainbowColor2":
                return (double) computeRainbow(1333L);
            case "rainbowColor3":
                return (double) computeRainbow(2666L);
            default:
                if (customParams.containsKey(name)) {
                    return customParams.get(name);
                }
                if (ParamRegistry.has(name)) {
                    return ParamRegistry.getDefinition(name).getDefaultValue();
                }
                return null;
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        switch (name) {
            case "x":
                if (value instanceof Number) element.setX(((Number) value).floatValue());
                break;
            case "y":
                if (value instanceof Number) element.setY(((Number) value).floatValue());
                break;
            case "width":
                if (value instanceof Number) element.setWidth(((Number) value).floatValue());
                break;
            case "height":
                if (value instanceof Number) element.setHeight(((Number) value).floatValue());
                break;
            case "font":
                if (value instanceof BuiltinFont) {
                    element.setFont("default");
                } else if (value != null) {
                    element.setFont(value.toString());
                }
                break;
            default:
                customParams.put(name, value);
                break;
        }
    }

    private int computeRainbow(long offset) {
        long time = System.currentTimeMillis() + offset;
        float hue = (time % 4000L) / 4000.0f;
        int rgb = Color.HSBtoRGB(hue, 0.8f, 1.0f);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }
}
