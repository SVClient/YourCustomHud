package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tovasha.ych.script.TokenType;

@Getter
@AllArgsConstructor
public class UnaryNode implements ExpressionNode {
    private final TokenType operator;
    private final ExpressionNode operand;
    private final int line;
    private final int column;
}
