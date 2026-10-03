package org.tovasha.ych.script;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;
    private final int column;
}
