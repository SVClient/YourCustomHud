package org.tovasha.ych.script.ast;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SwitchCase {
    private final List<ExpressionNode> values;
    private final List<StatementNode> statements;
}
