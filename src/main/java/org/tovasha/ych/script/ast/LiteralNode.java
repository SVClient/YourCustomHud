package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LiteralNode implements ExpressionNode {
    private final Object value;
    private final int line;
    private final int column;
}
