package org.tovasha.ych.script.ast;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArrayLiteralNode implements ExpressionNode {
    private final List<ExpressionNode> elements;
    private final int line;
    private final int column;
}
