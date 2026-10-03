package org.tovasha.ych.script;

import java.util.List;

public interface ScriptCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
