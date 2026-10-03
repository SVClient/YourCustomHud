package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpressionStatementNode implements StatementNode {
    private final ExpressionNode expression;
    private final int line;
    private final int column;
}
