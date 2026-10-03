package org.tovasha.ych.script.ast;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SwitchNode implements StatementNode {
    private final ExpressionNode expression;
    private final List<SwitchCase> cases;
    private final List<StatementNode> defaultStatements;
    private final int line;
    private final int column;
}
