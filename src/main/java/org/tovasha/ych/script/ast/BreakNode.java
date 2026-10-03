package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BreakNode implements StatementNode {
    private final int line;
    private final int column;
}
