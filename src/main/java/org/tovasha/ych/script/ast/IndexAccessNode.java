package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IndexAccessNode implements ExpressionNode {
    private final ExpressionNode target;
    private final ExpressionNode index;
    private final int line;
    private final int column;
}
