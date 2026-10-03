package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IdentifierNode implements ExpressionNode {
    private final String name;
    private final int line;
    private final int column;
}
