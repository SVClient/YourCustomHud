package org.tovasha.ych.script.builtins;

public interface ScriptNamespace {
    Object getProperty(String name);
    void setProperty(String name, Object value);
}
