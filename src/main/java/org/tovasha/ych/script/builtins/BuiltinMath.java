package org.tovasha.ych.script.builtins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.tovasha.ych.script.Interpreter;
import org.tovasha.ych.script.ScriptCallable;

public class BuiltinMath implements ScriptNamespace {
    private final Map<String, Object> members = new HashMap<>();

    public BuiltinMath() {
        members.put("PI", Math.PI);
        members.put("E", Math.E);

        registerFunction("sin", 1, args -> Math.sin(toDouble(args.get(0))));
        registerFunction("cos", 1, args -> Math.cos(toDouble(args.get(0))));
        registerFunction("min", 2, args -> Math.min(toDouble(args.get(0)), toDouble(args.get(1))));
        registerFunction("max", 2, args -> Math.max(toDouble(args.get(0)), toDouble(args.get(1))));
        registerFunction("clamp", 3, args -> {
            double val = toDouble(args.get(0));
            double min = toDouble(args.get(1));
            double max = toDouble(args.get(2));
            return Math.max(min, Math.min(max, val));
        });
        registerFunction("abs", 1, args -> Math.abs(toDouble(args.get(0))));
        registerFunction("round", 1, args -> (double) Math.round(toDouble(args.get(0))));
        registerFunction("floor", 1, args -> Math.floor(toDouble(args.get(0))));
        registerFunction("ceil", 1, args -> Math.ceil(toDouble(args.get(0))));
        registerFunction("random", 0, args -> Math.random());
    }

    private void registerFunction(String name, int arity, Function<List<Object>, Object> func) {
        members.put(name, new ScriptCallable() {
            @Override
            public int arity() {
                return arity;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return func.apply(arguments);
            }
        });
    }

    private double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return 0.0;
    }

    @Override
    public Object getProperty(String name) {
        return members.get(name);
    }

    @Override
    public void setProperty(String name, Object value) {
    }
}
