package org.tovasha.ych.script.ast;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CallNode implements ExpressionNode {
    private final ExpressionNode callee;
    private final List<ExpressionNode> arguments;
    private final int line;
    private final int column;
}
