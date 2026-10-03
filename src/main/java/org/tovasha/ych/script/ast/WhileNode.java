package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WhileNode implements StatementNode {
    private final ExpressionNode condition;
    private final StatementNode body;
    private final int line;
    private final int column;
}
