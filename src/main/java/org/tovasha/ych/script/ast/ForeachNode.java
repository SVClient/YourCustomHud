package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ForeachNode implements StatementNode {
    private final String variableName;
    private final ExpressionNode iterable;
    private final StatementNode body;
    private final int line;
    private final int column;
}
