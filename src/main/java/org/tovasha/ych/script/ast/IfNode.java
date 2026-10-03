package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IfNode implements StatementNode {
    private final ExpressionNode condition;
    private final StatementNode thenBranch;
    private final StatementNode elseBranch;
    private final int line;
    private final int column;
}
