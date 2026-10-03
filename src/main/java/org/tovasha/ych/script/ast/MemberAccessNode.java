package org.tovasha.ych.script.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberAccessNode implements ExpressionNode {
    private final ExpressionNode object;
    private final String member;
    private final int line;
    private final int column;
}
