package org.tovasha.ych.script.builtins;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.tovasha.ych.script.Interpreter;
import org.tovasha.ych.script.ScriptCallable;

public class Slot implements ScriptNamespace {
    private final ItemStack stack;
    private final int index;
    private final Map<String, ScriptCallable> methods = new HashMap<>();

    public Slot(ItemStack stack) {
        this(stack, 0);
    }

    public Slot(ItemStack stack, int index) {
        this.stack = stack != null ? stack : ItemStack.EMPTY;
        this.index = index;

        registerMethod("getname", args -> getName());
        registerMethod("name", args -> getName());

        registerMethod("getcount", args -> (double) getCount());
        registerMethod("count", args -> (double) getCount());

        registerMethod("geticon", args -> getIcon());
        registerMethod("icon", args -> getIcon());

        registerMethod("isempty", args -> isEmpty());
        registerMethod("empty", args -> isEmpty());

        registerMethod("getindex", args -> (double) getIndex());
        registerMethod("index", args -> (double) getIndex());
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

    public String getName() {
        if (stack.isEmpty()) {
            return "";
        }
        return stack.getHoverName().getString();
    }

    public double getCount() {
        if (stack.isEmpty()) {
            return 0.0;
        }
        return (double) stack.getCount();
    }

    public String getIcon() {
        if (stack.isEmpty()) {
            return "";
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public Object getProperty(String propertyName) {
        if (propertyName == null) {
            return null;
        }
        String lower = propertyName.toLowerCase();
        if (methods.containsKey(lower)) {
            return methods.get(lower);
        }
        switch (lower) {
            case "name":
            case "getname":
                return getName();
            case "count":
            case "getcount":
                return (double) getCount();
            case "icon":
            case "geticon":
                return getIcon();
            case "isempty":
            case "empty":
                return isEmpty();
            case "index":
            case "getindex":
                return (double) getIndex();
            default:
                return null;
        }
    }

    @Override
    public void setProperty(String propertyName, Object value) {
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "Empty";
        }
        return getName() + " x" + (int) getCount();
    }
}
