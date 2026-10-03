package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VarDeclNode implements StatementNode {
    private final String name;
    private final ExpressionNode initializer;
    private final int line;
    private final int column;
}
