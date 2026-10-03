package org.tovasha.ych.script;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tovasha.ych.script.ast.FunctionDeclNode;
import org.tovasha.ych.script.ast.StatementNode;

@Getter
@AllArgsConstructor
public class ScriptFunction implements ScriptCallable {
    private final FunctionDeclNode declaration;
    private final Environment closure;

    @Override
    public int arity() {
        return declaration.getParameters().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.getParameters().size(); i++) {
            Object arg = i < arguments.size() ? arguments.get(i) : null;
            environment.define(declaration.getParameters().get(i), arg);
        }

        try {
            for (StatementNode stmt : declaration.getBody().getStatements()) {
                interpreter.execute(stmt, environment);
            }
        } catch (ReturnException returnException) {
            return returnException.getValue();
        }

        return null;
    }
}
