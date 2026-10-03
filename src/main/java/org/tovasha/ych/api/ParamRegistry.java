package org.tovasha.ych.api;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ParamRegistry {
    @Getter
    @AllArgsConstructor
    public static class ParamDefinition {
        private final String name;
        private final Class<?> type;
        private final Object defaultValue;
    }

    private static final Map<String, ParamDefinition> PARAMS = new ConcurrentHashMap<>();

    public static void register(String name, Class<?> type, Object defaultValue) {
        PARAMS.put(name, new ParamDefinition(name, type, defaultValue));
    }

    public static ParamDefinition getDefinition(String name) {
        return PARAMS.get(name);
    }

    public static boolean has(String name) {
        return PARAMS.containsKey(name);
    }

    public static Map<String, ParamDefinition> getAll() {
        return Collections.unmodifiableMap(PARAMS);
    }
}
