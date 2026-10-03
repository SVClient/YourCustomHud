package org.tovasha.ych.script.ast;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlockNode implements StatementNode {
    private final List<StatementNode> statements;
    private final int line;
    private final int column;
}
