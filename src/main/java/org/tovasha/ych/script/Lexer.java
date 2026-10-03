package org.tovasha.ych.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class Lexer {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("let", TokenType.LET);
        KEYWORDS.put("locate", TokenType.LOCATE);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("fn", TokenType.FN);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("null", TokenType.NULL);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("while", TokenType.WHILE);
        KEYWORDS.put("break", TokenType.BREAK);
        KEYWORDS.put("continue", TokenType.CONTINUE);
        KEYWORDS.put("switch", TokenType.SWITCH);
        KEYWORDS.put("case", TokenType.CASE);
        KEYWORDS.put("default", TokenType.DEFAULT);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final List<ScriptDiagnostic> diagnostics = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int startColumn = 1;

    public Lexer(String source) {
        this.source = source != null ? source : "";
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            startColumn = column;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case '[': addToken(TokenType.LBRACKET); break;
            case ']': addToken(TokenType.RBRACKET); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ':': addToken(TokenType.COLON); break;
            case '.': addToken(TokenType.DOT); break;
            case '+':
                addToken(match('=') ? TokenType.PLUS_EQUAL : TokenType.PLUS);
                break;
            case '-':
                addToken(match('=') ? TokenType.MINUS_EQUAL : TokenType.MINUS);
                break;
            case '*':
                addToken(match('=') ? TokenType.STAR_EQUAL : TokenType.STAR);
                break;
            case '%':
                addToken(TokenType.PERCENT);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AMP_AMP);
                } else {
                    diagnostics.add(new ScriptDiagnostic("Unexpected character '&'", line, startColumn, true));
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.PIPE_PIPE);
                } else {
                    diagnostics.add(new ScriptDiagnostic("Unexpected character '|'", line, startColumn, true));
                }
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else if (match('*')) {
                    while (!isAtEnd()) {
                        if (peek() == '*' && peekNext() == '/') {
                            advance();
                            advance();
                            break;
                        }
                        if (peek() == '\n') {
                            line++;
                            column = 0;
                        }
                        advance();
                    }
                } else if (match('=')) {
                    addToken(TokenType.SLASH_EQUAL);
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                column = 1;
                break;
            case '"':
            case '\'':
                string(c);
                break;
            default:
                if (isDigit(c)) {
                    number(c);
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    diagnostics.add(new ScriptDiagnostic("Unexpected character '" + c + "'", line, startColumn, true));
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.get(text);
        if (type == null) {
            type = TokenType.IDENTIFIER;
        }
        addToken(type);
    }

    private void number(char firstChar) {
        if (firstChar == '0' && (peek() == 'x' || peek() == 'X')) {
            advance();
            while (isHexDigit(peek())) {
                advance();
            }
            String hexText = source.substring(start + 2, current);
            try {
                long val = Long.parseLong(hexText, 16);
                addToken(TokenType.NUMBER, (double) val);
            } catch (NumberFormatException e) {
                diagnostics.add(new ScriptDiagnostic("Invalid hex number", line, startColumn, true));
                addToken(TokenType.NUMBER, 0.0);
            }
            return;
        }

        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }

        String numText = source.substring(start, current);
        try {
            double val = Double.parseDouble(numText);
            addToken(TokenType.NUMBER, val);
        } catch (NumberFormatException e) {
            diagnostics.add(new ScriptDiagnostic("Invalid number format", line, startColumn, true));
            addToken(TokenType.NUMBER, 0.0);
        }
    }

    private void string(char quote) {
        StringBuilder sb = new StringBuilder();
        while (peek() != quote && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 0;
            }
            char c = advance();
            if (c == '\\' && !isAtEnd()) {
                char esc = advance();
                switch (esc) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '\\': sb.append('\\'); break;
                    case '"': sb.append('"'); break;
                    case '\'': sb.append('\''); break;
                    default: sb.append('\\').append(esc); break;
                }
            } else {
                sb.append(c);
            }
        }

        if (isAtEnd()) {
            diagnostics.add(new ScriptDiagnostic("Unterminated string", line, startColumn, true));
            return;
        }

        advance();
        addToken(TokenType.STRING, sb.toString());
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) {
            return false;
        }
        current++;
        column++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        char c = source.charAt(current++);
        column++;
        return c;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, startColumn));
    }
}
