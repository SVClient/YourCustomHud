package org.tovasha.ych.script.ast;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FunctionDeclNode implements StatementNode {
    private final String name;
    private final List<String> parameters;
    private final BlockNode body;
    private final int line;
    private final int column;
}
