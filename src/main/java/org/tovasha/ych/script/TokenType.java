package org.tovasha.ych.script;

public enum TokenType {
    LET,
    LOCATE,
    IF,
    ELSE,
    FN,
    RETURN,
    TRUE,
    FALSE,
    NULL,

    FOR,
    FOREACH,
    IN,
    WHILE,
    BREAK,
    CONTINUE,
    SWITCH,
    CASE,
    DEFAULT,

    IDENTIFIER,
    NUMBER,
    STRING,

    PLUS,
    PLUS_EQUAL,
    MINUS,
    MINUS_EQUAL,
    STAR,
    STAR_EQUAL,
    SLASH,
    SLASH_EQUAL,
    PERCENT,

    EQUAL,
    EQUAL_EQUAL,
    BANG,
    BANG_EQUAL,
    LESS,
    LESS_EQUAL,
    GREATER,
    GREATER_EQUAL,

    AMP_AMP,
    PIPE_PIPE,

    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACKET,
    RBRACKET,
    COMMA,
    SEMICOLON,
    COLON,
    DOT,

    EOF
}
