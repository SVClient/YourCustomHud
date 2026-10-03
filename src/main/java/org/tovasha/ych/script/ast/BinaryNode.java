package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tovasha.ych.script.TokenType;

@Getter
@AllArgsConstructor
public class BinaryNode implements ExpressionNode {
    private final ExpressionNode left;
    private final TokenType operator;
    private final ExpressionNode right;
    private final int line;
    private final int column;
}
