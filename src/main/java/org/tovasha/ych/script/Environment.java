package org.tovasha.ych.script;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class Environment {
    @Getter
    private final Environment parent;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public boolean assign(String name, Object value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return true;
        }
        if (parent != null) {
            return parent.assign(name, value);
        }
        return false;
    }

    public Object get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        return null;
    }

    public boolean has(String name) {
        if (values.containsKey(name)) {
            return true;
        }
        if (parent != null) {
            return parent.has(name);
        }
        return false;
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
