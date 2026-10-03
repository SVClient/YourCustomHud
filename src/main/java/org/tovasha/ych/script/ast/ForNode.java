package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ForNode implements StatementNode {
    private final StatementNode initializer;
    private final ExpressionNode condition;
    private final ExpressionNode increment;
    private final StatementNode body;
    private final int line;
    private final int column;
}
