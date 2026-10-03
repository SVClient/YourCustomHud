package org.tovasha.ych.script.builtins;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import org.tovasha.ych.api.FontRegistry;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.render.RenderUtils;
import org.tovasha.ych.script.Interpreter;
import org.tovasha.ych.script.ScriptCallable;

public class BuiltinFont implements ScriptNamespace {
    private final HudElement element;
    private final Map<String, Object> methods = new HashMap<>();

    public BuiltinFont(HudElement element) {
        this.element = element;
        Function<List<Object>, Object> heightFunc = args -> {
            String defFont = element != null ? element.getFont() : "default";
            if (args.isEmpty()) {
                return RenderUtils.getFontHeight(defFont, 9.0f);
            }
            if (args.size() == 1) {
                if (args.get(0) instanceof Number) {
                    float size = toFloat(args.get(0));
                    return RenderUtils.getFontHeight(defFont, size > 0 ? size : 9.0f);
                } else {
                    String fontName = String.valueOf(args.get(0));
                    return RenderUtils.getFontHeight(fontName, 9.0f);
                }
            }
            String fontName = String.valueOf(args.get(0));
            float size = toFloat(args.get(1));
            return RenderUtils.getFontHeight(fontName, size);
        };

        registerMethod("height", heightFunc);
        registerMethod("fontHeight", heightFunc);

        registerMethod("textWidth", args -> {
            if (args.isEmpty()) {
                return 0.0f;
            }
            String text = String.valueOf(args.get(0));
            String defFont = element != null ? element.getFont() : "default";
            if (args.size() == 1) {
                return RenderUtils.getTextWidth(text, defFont, 9.0f);
            }
            if (args.size() == 2) {
                if (args.get(1) instanceof Number) {
                    float size = toFloat(args.get(1));
                    return RenderUtils.getTextWidth(text, defFont, size);
                } else {
                    String fontName = String.valueOf(args.get(1));
                    return RenderUtils.getTextWidth(text, fontName, 9.0f);
                }
            }
            String fontName = String.valueOf(args.get(1));
            float size = toFloat(args.get(2));
            return RenderUtils.getTextWidth(text, fontName, size);
        });
    }

    private void registerMethod(String name, Function<List<Object>, Object> func) {
        methods.put(name, new ScriptCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return func.apply(arguments);
            }

            @Override
            public String toString() {
                return String.valueOf(func.apply(Collections.emptyList()));
            }
        });
    }

    private float toFloat(Object o) {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        return 9.0f;
    }

    @Override
    public Object getProperty(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        if ("defaultFont".equals(name) || "default".equals(name)) {
            return "default";
        }
        if ("arialBlack".equals(name) || "arial_black".equals(name) || "arialblack".equals(name) || "black".equals(name)) {
            return "arialblack";
        }
        if ("arial".equals(name)) {
            return "arial";
        }
        if ("modern".equals(name)) {
            return "modern";
        }
        if ("consolas".equals(name) || "code".equals(name)) {
            return "code";
        }
        if ("bahnschrift".equalsIgnoreCase(name) || "bahn".equalsIgnoreCase(name)) {
            return "bahnschrift";
        }
        if ("impact".equalsIgnoreCase(name)) {
            return "impact";
        }
        if ("comic".equalsIgnoreCase(name) || "comicSans".equalsIgnoreCase(name) || "comicsans".equalsIgnoreCase(name)) {
            return "comic";
        }
        if ("tahoma".equalsIgnoreCase(name)) {
            return "tahoma";
        }
        if ("verdana".equalsIgnoreCase(name)) {
            return "verdana";
        }
        if ("trebuchet".equalsIgnoreCase(name) || "trebuc".equalsIgnoreCase(name)) {
            return "trebuchet";
        }
        if ("cascadia".equalsIgnoreCase(name) || "cascadiaCode".equalsIgnoreCase(name) || "cascadiacode".equalsIgnoreCase(name)) {
            return "cascadia";
        }
        if ("calibri".equalsIgnoreCase(name)) {
            return "calibri";
        }
        if ("georgia".equalsIgnoreCase(name)) {
            return "georgia";
        }
        if (FontRegistry.has(name)) {
            Identifier id = FontRegistry.get(name);
            return id != null ? id.toString() : name;
        }
        if (FontRegistry.has(name.toLowerCase())) {
            Identifier id = FontRegistry.get(name.toLowerCase());
            return id != null ? id.toString() : name;
        }
        return name;
    }

    @Override
    public String toString() {
        return "default";
    }

    @Override
    public void setProperty(String name, Object value) {
    }
}
