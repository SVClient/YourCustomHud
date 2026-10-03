package org.tovasha.ych.api;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class VariableRegistry {
    private static final Map<String, Supplier<Object>> VARIABLES = new ConcurrentHashMap<>();

    public static void register(String name, Supplier<Object> getter) {
        VARIABLES.put(name, getter);
    }

    public static Object get(String name) {
        Supplier<Object> supplier = VARIABLES.get(name);
        return supplier != null ? supplier.get() : null;
    }

    public static boolean has(String name) {
        return VARIABLES.containsKey(name);
    }

    public static Map<String, Supplier<Object>> getAll() {
        return Collections.unmodifiableMap(VARIABLES);
    }
}
