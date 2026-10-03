package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReturnNode implements StatementNode {
    private final ExpressionNode value;
    private final int line;
    private final int column;
}
