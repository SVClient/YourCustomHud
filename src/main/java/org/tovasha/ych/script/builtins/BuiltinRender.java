package org.tovasha.ych.script.builtins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.render.RenderUtils;
import org.tovasha.ych.render.TargetTracker;
import org.tovasha.ych.script.Interpreter;
import org.tovasha.ych.script.ScriptCallable;

public class BuiltinRender implements ScriptNamespace {
    @Getter
    @Setter
    private GuiGraphics graphics;
    private final HudElement element;

    private final Map<String, Object> methods = new HashMap<>();

    public BuiltinRender(HudElement element) {
        this.element = element;
        registerMethod("drawRect", args -> {
            if (graphics != null && args.size() >= 5) {
                RenderUtils.drawRect(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                        toFloat(args.get(2)), toFloat(args.get(3)), toColor(args.get(4)));
            }
            return null;
        });

        registerMethod("drawRoundedRect", args -> {
            if (graphics != null && args.size() >= 6) {
                RenderUtils.drawRoundedRect(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                        toFloat(args.get(2)), toFloat(args.get(3)), toFloat(args.get(4)), toColor(args.get(5)));
            }
            return null;
        });

        registerMethod("drawOutline", args -> {
            if (graphics != null && args.size() >= 5) {
                if (args.size() == 5) {
                    RenderUtils.drawOutline(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                            toFloat(args.get(2)), toFloat(args.get(3)), 1.0f, toColor(args.get(4)));
                } else if (args.size() >= 6) {
                    RenderUtils.drawOutline(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                            toFloat(args.get(2)), toFloat(args.get(3)), toFloat(args.get(4)), toColor(args.get(5)));
                }
            }
            return null;
        });

        registerMethod("outline", args -> {
            if (graphics != null && args.size() >= 5) {
                if (args.size() == 5) {
                    RenderUtils.drawOutline(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                            toFloat(args.get(2)), toFloat(args.get(3)), 1.0f, toColor(args.get(4)));
                } else if (args.size() >= 6) {
                    RenderUtils.drawOutline(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                            toFloat(args.get(2)), toFloat(args.get(3)), toFloat(args.get(4)), toColor(args.get(5)));
                }
            }
            return null;
        });

        registerMethod("drawRoundedOutline", args -> {
            if (graphics != null && args.size() >= 6) {
                if (args.size() == 6) {
                    RenderUtils.drawRoundedOutline(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                            toFloat(args.get(2)), toFloat(args.get(3)), toFloat(args.get(4)), 1.0f, toColor(args.get(5)));
                } else if (args.size() >= 7) {
                    RenderUtils.drawRoundedOutline(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                            toFloat(args.get(2)), toFloat(args.get(3)), toFloat(args.get(4)), toFloat(args.get(5)), toColor(args.get(6)));
                }
            }
            return null;
        });

        registerMethod("roundedOutline", args -> {
            if (graphics != null && args.size() >= 6) {
                if (args.size() == 6) {
                    RenderUtils.drawRoundedOutline(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                            toFloat(args.get(2)), toFloat(args.get(3)), toFloat(args.get(4)), 1.0f, toColor(args.get(5)));
                } else if (args.size() >= 7) {
                    RenderUtils.drawRoundedOutline(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                            toFloat(args.get(2)), toFloat(args.get(3)), toFloat(args.get(4)), toFloat(args.get(5)), toColor(args.get(6)));
                }
            }
            return null;
        });

        registerMethod("drawText", args -> {
            if (graphics != null) {
                String defaultFont = element != null ? element.getFont() : "default";
                if (args.size() == 4) {
                    RenderUtils.drawCustomText(graphics, String.valueOf(args.get(0)),
                            toFloat(args.get(1)), toFloat(args.get(2)), defaultFont, 9.0f, toColor(args.get(3)));
                } else if (args.size() == 5) {
                    if (args.get(3) instanceof Number) {
                        RenderUtils.drawCustomText(graphics, String.valueOf(args.get(0)),
                                toFloat(args.get(1)), toFloat(args.get(2)), defaultFont, toFloat(args.get(3)), toColor(args.get(4)));
                    } else {
                        RenderUtils.drawCustomText(graphics, String.valueOf(args.get(0)),
                                toFloat(args.get(1)), toFloat(args.get(2)), String.valueOf(args.get(3)), 9.0f, toColor(args.get(4)));
                    }
                } else if (args.size() >= 6) {
                    RenderUtils.drawCustomText(graphics, String.valueOf(args.get(0)),
                            toFloat(args.get(1)), toFloat(args.get(2)), String.valueOf(args.get(3)), toFloat(args.get(4)), toColor(args.get(5)));
                }
            }
            return null;
        });

        registerMethod("drawCustomText", args -> {
            if (graphics != null) {
                if (args.size() == 5) {
                    RenderUtils.drawCustomText(graphics, String.valueOf(args.get(0)),
                            toFloat(args.get(1)), toFloat(args.get(2)), String.valueOf(args.get(3)),
                            9.0f, toColor(args.get(4)));
                } else if (args.size() >= 6) {
                    RenderUtils.drawCustomText(graphics, String.valueOf(args.get(0)),
                            toFloat(args.get(1)), toFloat(args.get(2)), String.valueOf(args.get(3)),
                            toFloat(args.get(4)), toColor(args.get(5)));
                }
            }
            return null;
        });

        registerMethod("drawGradientRect", args -> {
            if (graphics != null && args.size() >= 6) {
                boolean horiz = args.get(5) instanceof Boolean && (Boolean) args.get(5);
                RenderUtils.drawGradientRect(graphics, toFloat(args.get(0)), toFloat(args.get(1)),
                        toFloat(args.get(2)), toFloat(args.get(3)), toColor(args.get(4)), toColor(args.get(5)), horiz);
            }
            return null;
        });

        registerMethod("drawImage", args -> {
            if (graphics != null && args.size() >= 3) {
                String path = String.valueOf(args.get(0));
                if (path == null || path.isEmpty()) return null;
                float x = toFloat(args.get(1));
                float y = toFloat(args.get(2));
                float w = args.size() >= 4 ? toFloat(args.get(3)) : 16.0f;
                float h = args.size() >= 5 ? toFloat(args.get(4)) : w;
                PlayerSkin skin = TargetTracker.getTargetSkin();
                if (skin != null && (path.equals(skin.body().id().toString()) || path.contains("skin") || path.contains("player"))) {
                    RenderUtils.drawPlayerHead(graphics, skin, x, y, Math.min(w, h));
                    return null;
                }
                ItemStack stack = getItemStackByName(path);
                if (!stack.isEmpty()) {
                    RenderUtils.drawItem(graphics, stack, x, y, Math.min(w, h));
                    return null;
                }
                RenderUtils.drawImage(graphics, path, x, y, w, h);
            }
            return null;
        });

        registerMethod("drawTexture", args -> {
            if (graphics != null && args.size() >= 3) {
                String path = String.valueOf(args.get(0));
                if (path == null || path.isEmpty()) return null;
                float x = toFloat(args.get(1));
                float y = toFloat(args.get(2));
                float w = args.size() >= 4 ? toFloat(args.get(3)) : 16.0f;
                float h = args.size() >= 5 ? toFloat(args.get(4)) : w;
                PlayerSkin skin = TargetTracker.getTargetSkin();
                if (skin != null && (path.equals(skin.body().id().toString()) || path.contains("skin") || path.contains("player"))) {
                    RenderUtils.drawPlayerHead(graphics, skin, x, y, Math.min(w, h));
                    return null;
                }
                ItemStack stack = getItemStackByName(path);
                if (!stack.isEmpty()) {
                    RenderUtils.drawItem(graphics, stack, x, y, Math.min(w, h));
                    return null;
                }
                RenderUtils.drawImage(graphics, path, x, y, w, h);
            }
            return null;
        });

        registerMethod("drawPlayerHead", args -> {
            if (graphics != null && args.size() >= 3) {
                float x = toFloat(args.get(0));
                float y = toFloat(args.get(1));
                float size = toFloat(args.get(2));
                PlayerSkin skin = TargetTracker.getTargetSkin();
                if (skin != null) {
                    RenderUtils.drawPlayerHead(graphics, skin, x, y, size);
                }
            }
            return null;
        });

        registerMethod("drawItem", args -> {
            if (graphics != null && args.size() >= 3) {
                String itemName = String.valueOf(args.get(0));
                float x = toFloat(args.get(1));
                float y = toFloat(args.get(2));
                float size = args.size() >= 4 ? toFloat(args.get(3)) : 16.0f;
                ItemStack stack = getItemStackByName(itemName);
                if (!stack.isEmpty()) {
                    RenderUtils.drawItem(graphics, stack, x, y, size);
                }
            }
            return null;
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
        });
    }

    private float toFloat(Object o) {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        return 0.0f;
    }

    private int toColor(Object o) {
        if (o instanceof Number) {
            long val = ((Number) o).longValue();
            if ((val & 0xFF000000L) == 0 && val <= 0x00FFFFFFL) {
                val |= 0xFF000000L;
            }
            return (int) val;
        }
        return 0xFFFFFFFF;
    }

    private ItemStack getItemStackByName(String name) {
        if (name == null || name.isEmpty()) return ItemStack.EMPTY;
        Identifier id = Identifier.tryParse(name);
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item);
    }

    @Override
    public Object getProperty(String name) {
        return methods.get(name);
    }

    @Override
    public void setProperty(String name, Object value) {
    }
}
