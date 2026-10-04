package org.tovasha.ych.script.builtins;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import org.tovasha.ych.script.Interpreter;
import org.tovasha.ych.script.ScriptCallable;

public class Potion implements ScriptNamespace {
    private final MobEffectInstance instance;
    private final int amplifier;
    private final PotionDuration duration;
    private final String name;
    private final String icon;
    private final Map<String, ScriptCallable> methods = new HashMap<>();

    public Potion(MobEffectInstance instance) {
        this.instance = instance;
        if (instance != null) {
            this.amplifier = instance.getAmplifier();
            String formatted = MobEffectUtil.formatDuration(instance, 1.0f, 20.0f).getString();
            this.duration = new PotionDuration(instance.getDuration(), formatted);
            Holder<MobEffect> holder = instance.getEffect();
            this.name = holder.value().getDisplayName().getString();
            Identifier sprite = Gui.getMobEffectSprite(holder);
            this.icon = sprite != null ? sprite.toString() : "";
        } else {
            this.amplifier = 0;
            this.duration = new PotionDuration(0, "0:00");
            this.name = "";
            this.icon = "";
        }
        initMethods();
    }

    public Potion(int amplifier, int durationTicks, String formattedDuration, String name, String icon) {
        this.instance = null;
        this.amplifier = amplifier;
        this.duration = new PotionDuration(durationTicks, formattedDuration);
        this.name = name != null ? name : "";
        this.icon = icon != null ? icon : "";
        initMethods();
    }

    private void initMethods() {
        registerMethod("getamplifier", args -> (double) getAmplifier());
        registerMethod("amplifier", args -> (double) getAmplifier());

        registerMethod("getduration", args -> getDuration());
        registerMethod("duration", args -> getDuration());

        registerMethod("getname", args -> getName());
        registerMethod("name", args -> getName());

        registerMethod("geticon", args -> getIcon());
        registerMethod("icon", args -> getIcon());
    }

    private void registerMethod(String methodName, Function<List<Object>, Object> func) {
        methods.put(methodName.toLowerCase(), new ScriptCallable() {
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

    public int getAmplifier() {
        return amplifier;
    }

    public PotionDuration getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public Object getProperty(String propertyName) {
        if (propertyName == null) return null;
        String lower = propertyName.toLowerCase();
        if (methods.containsKey(lower)) {
            return methods.get(lower);
        }
        switch (lower) {
            case "amplifier":
            case "level":
                return (double) getAmplifier();
            case "duration":
                return getDuration();
            case "name":
                return getName();
            case "icon":
                return getIcon();
            default:
                return null;
        }
    }

    @Override
    public void setProperty(String propertyName, Object value) {
    }

    @Override
    public String toString() {
        return name + " " + (amplifier + 1) + " (" + duration + ")";
    }
}
