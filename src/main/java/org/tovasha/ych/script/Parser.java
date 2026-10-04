package org.tovasha.ych.script;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.tovasha.ych.script.ast.ArrayLiteralNode;
import org.tovasha.ych.script.ast.AssignNode;
import org.tovasha.ych.script.ast.BinaryNode;
import org.tovasha.ych.script.ast.BlockNode;
import org.tovasha.ych.script.ast.BreakNode;
import org.tovasha.ych.script.ast.CallNode;
import org.tovasha.ych.script.ast.ContinueNode;
import org.tovasha.ych.script.ast.ExpressionNode;
import org.tovasha.ych.script.ast.ExpressionStatementNode;
import org.tovasha.ych.script.ast.ForNode;
import org.tovasha.ych.script.ast.ForeachNode;
import org.tovasha.ych.script.ast.IndexAccessNode;
import org.tovasha.ych.script.ast.FunctionDeclNode;
import org.tovasha.ych.script.ast.IdentifierNode;
import org.tovasha.ych.script.ast.IfNode;
import org.tovasha.ych.script.ast.LiteralNode;
import org.tovasha.ych.script.ast.MemberAccessNode;
import org.tovasha.ych.script.ast.ProgramNode;
import org.tovasha.ych.script.ast.ReturnNode;
import org.tovasha.ych.script.ast.StatementNode;
import org.tovasha.ych.script.ast.SwitchCase;
import org.tovasha.ych.script.ast.SwitchNode;
import org.tovasha.ych.script.ast.UnaryNode;
import org.tovasha.ych.script.ast.VarDeclNode;
import org.tovasha.ych.script.ast.WhileNode;

@Getter
public class Parser {
    private final List<Token> tokens;
    private final List<ScriptDiagnostic> diagnostics = new ArrayList<>();
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ProgramNode parse() {
        List<StatementNode> statements = new ArrayList<>();
        int startLine = peek().getLine();
        int startCol = peek().getColumn();

        while (!isAtEnd()) {
            try {
                StatementNode stmt = declaration();
                if (stmt != null) {
                    statements.add(stmt);
                }
            } catch (ParseException e) {
                synchronize();
            }
        }

        return new ProgramNode(statements, startLine, startCol);
    }

    private StatementNode declaration() {
        if (match(TokenType.LET, TokenType.LOCATE)) {
            return varDeclaration();
        }
        if (match(TokenType.FN)) {
            return functionDeclaration();
        }
        return statement();
    }

    private StatementNode varDeclaration() {
        Token prev = previous();
        Token nameToken = consume(TokenType.IDENTIFIER, "Expect variable name");
        ExpressionNode initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        match(TokenType.SEMICOLON);
        return new VarDeclNode(nameToken.getLexeme(), initializer, prev.getLine(), prev.getColumn());
    }

    private StatementNode functionDeclaration() {
        Token prev = previous();
        Token nameToken = consume(TokenType.IDENTIFIER, "Expect function name after 'fn'");
        consume(TokenType.LPAREN, "Expect '(' after function name");
        List<String> parameters = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                Token token = consume(TokenType.IDENTIFIER, "Expect parameter name");
                if (check(TokenType.IDENTIFIER)) {
                    Token nameTokenParam = advance();
                    parameters.add(nameTokenParam.getLexeme());
                } else {
                    parameters.add(token.getLexeme());
                }
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expect ')' after parameters");
        consume(TokenType.LBRACE, "Expect '{' before function body");
        BlockNode body = block();
        return new FunctionDeclNode(nameToken.getLexeme(), parameters, body, prev.getLine(), prev.getColumn());
    }

    private StatementNode statement() {
        if (match(TokenType.IF)) {
            return ifStatement();
        }
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        if (match(TokenType.FOR)) {
            return forStatement();
        }
        if (match(TokenType.FOREACH)) {
            return foreachStatement();
        }
        if (match(TokenType.SWITCH)) {
            return switchStatement();
        }
        if (match(TokenType.BREAK)) {
            Token prev = previous();
            match(TokenType.SEMICOLON);
            return new BreakNode(prev.getLine(), prev.getColumn());
        }
        if (match(TokenType.CONTINUE)) {
            Token prev = previous();
            match(TokenType.SEMICOLON);
            return new ContinueNode(prev.getLine(), prev.getColumn());
        }
        if (match(TokenType.RETURN)) {
            return returnStatement();
        }
        if (match(TokenType.LBRACE)) {
            return block();
        }
        return expressionStatement();
    }

    private StatementNode whileStatement() {
        Token prev = previous();
        boolean hasParen = match(TokenType.LPAREN);
        ExpressionNode condition = expression();
        if (hasParen) {
            consume(TokenType.RPAREN, "Expect ')' after while condition");
        }
        StatementNode body = statement();
        return new WhileNode(condition, body, prev.getLine(), prev.getColumn());
    }

    private StatementNode foreachStatement() {
        Token prev = previous();
        boolean hasParen = match(TokenType.LPAREN);
        match(TokenType.LET, TokenType.LOCATE);
        Token varToken = consume(TokenType.IDENTIFIER, "Expect variable name in foreach loop");
        if (!match(TokenType.COLON) && !match(TokenType.IN)) {
            if (check(TokenType.IDENTIFIER) && "in".equalsIgnoreCase(peek().getLexeme())) {
                advance();
            } else {
                error(peek(), "Expect ':' or 'in' in foreach loop");
                throw new ParseException();
            }
        }
        ExpressionNode iterable = expression();
        if (hasParen) {
            consume(TokenType.RPAREN, "Expect ')' after foreach clauses");
        }
        StatementNode body = statement();
        return new ForeachNode(varToken.getLexeme(), iterable, body, prev.getLine(), prev.getColumn());
    }

    private StatementNode forStatement() {
        Token prev = previous();
        boolean hasParen = match(TokenType.LPAREN);
        boolean isForeach = false;
        if (check(TokenType.LET) || check(TokenType.LOCATE)) {
            if (current + 2 < tokens.size() && tokens.get(current + 1).getType() == TokenType.IDENTIFIER) {
                TokenType t = tokens.get(current + 2).getType();
                if (t == TokenType.COLON || t == TokenType.IN || (t == TokenType.IDENTIFIER && "in".equalsIgnoreCase(tokens.get(current + 2).getLexeme()))) {
                    isForeach = true;
                }
            }
        } else if (check(TokenType.IDENTIFIER)) {
            if (current + 1 < tokens.size()) {
                TokenType t = tokens.get(current + 1).getType();
                if (t == TokenType.COLON || t == TokenType.IN || (t == TokenType.IDENTIFIER && "in".equalsIgnoreCase(tokens.get(current + 1).getLexeme()))) {
                    isForeach = true;
                }
            }
        }
        if (isForeach) {
            match(TokenType.LET, TokenType.LOCATE);
            Token varToken = consume(TokenType.IDENTIFIER, "Expect variable name in loop");
            if (!match(TokenType.COLON) && !match(TokenType.IN)) {
                advance();
            }
            ExpressionNode iterable = expression();
            if (hasParen) {
                consume(TokenType.RPAREN, "Expect ')' after loop clauses");
            }
            StatementNode body = statement();
            return new ForeachNode(varToken.getLexeme(), iterable, body, prev.getLine(), prev.getColumn());
        }
        StatementNode initializer = null;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.LET, TokenType.LOCATE)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        ExpressionNode condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after for loop condition");

        ExpressionNode increment = null;
        if (!check(TokenType.RPAREN) && !check(TokenType.LBRACE)) {
            increment = expression();
        }
        if (hasParen) {
            consume(TokenType.RPAREN, "Expect ')' after for clauses");
        }

        StatementNode body = statement();
        return new ForNode(initializer, condition, increment, body, prev.getLine(), prev.getColumn());
    }

    private StatementNode switchStatement() {
        Token prev = previous();
        boolean hasParen = match(TokenType.LPAREN);
        ExpressionNode expr = expression();
        if (hasParen) {
            consume(TokenType.RPAREN, "Expect ')' after switch expression");
        }
        consume(TokenType.LBRACE, "Expect '{' before switch body");

        List<SwitchCase> cases = new ArrayList<>();
        List<StatementNode> defaultStatements = null;

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            if (match(TokenType.CASE)) {
                List<ExpressionNode> values = new ArrayList<>();
                do {
                    do {
                        values.add(expression());
                    } while (match(TokenType.COMMA));
                    consume(TokenType.COLON, "Expect ':' after case value");
                } while (match(TokenType.CASE));

                List<StatementNode> caseStatements = new ArrayList<>();
                while (!check(TokenType.CASE) && !check(TokenType.DEFAULT) && !check(TokenType.RBRACE) && !isAtEnd()) {
                    caseStatements.add(declaration());
                }
                cases.add(new SwitchCase(values, caseStatements));
            } else if (match(TokenType.DEFAULT)) {
                consume(TokenType.COLON, "Expect ':' after 'default'");
                List<StatementNode> defStatements = new ArrayList<>();
                while (!check(TokenType.CASE) && !check(TokenType.DEFAULT) && !check(TokenType.RBRACE) && !isAtEnd()) {
                    defStatements.add(declaration());
                }
                defaultStatements = defStatements;
            } else {
                error(peek(), "Expect 'case' or 'default'");
                throw new ParseException();
            }
        }

        consume(TokenType.RBRACE, "Expect '}' after switch body");
        return new SwitchNode(expr, cases, defaultStatements, prev.getLine(), prev.getColumn());
    }

    private StatementNode ifStatement() {
        Token prev = previous();
        boolean hasParen = match(TokenType.LPAREN);
        ExpressionNode condition = expression();
        if (hasParen) {
            consume(TokenType.RPAREN, "Expect ')' after if condition");
        }
        StatementNode thenBranch = statement();
        StatementNode elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new IfNode(condition, thenBranch, elseBranch, prev.getLine(), prev.getColumn());
    }

    private StatementNode returnStatement() {
        Token prev = previous();
        ExpressionNode value = null;
        if (!check(TokenType.SEMICOLON) && !check(TokenType.RBRACE)) {
            value = expression();
        }
        match(TokenType.SEMICOLON);
        return new ReturnNode(value, prev.getLine(), prev.getColumn());
    }

    private BlockNode block() {
        Token prev = previous();
        List<StatementNode> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            StatementNode stmt = declaration();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        consume(TokenType.RBRACE, "Expect '}' after block");
        return new BlockNode(statements, prev.getLine(), prev.getColumn());
    }

    private StatementNode expressionStatement() {
        ExpressionNode expr = expression();
        match(TokenType.SEMICOLON);
        return new ExpressionStatementNode(expr, expr.getLine(), expr.getColumn());
    }

    private ExpressionNode expression() {
        return assignment();
    }

    private ExpressionNode assignment() {
        ExpressionNode expr = logicalOr();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            ExpressionNode value = assignment();
            if (expr instanceof IdentifierNode || expr instanceof MemberAccessNode || expr instanceof IndexAccessNode) {
                return new AssignNode(expr, value, equals.getLine(), equals.getColumn());
            }
            error(equals, "Invalid assignment target");
        } else if (match(TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL, TokenType.STAR_EQUAL, TokenType.SLASH_EQUAL)) {
            Token op = previous();
            TokenType binOp;
            switch (op.getType()) {
                case PLUS_EQUAL: binOp = TokenType.PLUS; break;
                case MINUS_EQUAL: binOp = TokenType.MINUS; break;
                case STAR_EQUAL: binOp = TokenType.STAR; break;
                default: binOp = TokenType.SLASH; break;
            }
            ExpressionNode value = assignment();
            if (expr instanceof IdentifierNode || expr instanceof MemberAccessNode || expr instanceof IndexAccessNode) {
                ExpressionNode bin = new BinaryNode(expr, binOp, value, op.getLine(), op.getColumn());
                return new AssignNode(expr, bin, op.getLine(), op.getColumn());
            }
            error(op, "Invalid assignment target");
        }
        return expr;
    }

    private ExpressionNode logicalOr() {
        ExpressionNode expr = logicalAnd();
        while (match(TokenType.PIPE_PIPE)) {
            Token operator = previous();
            ExpressionNode right = logicalAnd();
            expr = new BinaryNode(expr, operator.getType(), right, operator.getLine(), operator.getColumn());
        }
        return expr;
    }

    private ExpressionNode logicalAnd() {
        ExpressionNode expr = equality();
        while (match(TokenType.AMP_AMP)) {
            Token operator = previous();
            ExpressionNode right = equality();
            expr = new BinaryNode(expr, operator.getType(), right, operator.getLine(), operator.getColumn());
        }
        return expr;
    }

    private ExpressionNode equality() {
        ExpressionNode expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            ExpressionNode right = comparison();
            expr = new BinaryNode(expr, operator.getType(), right, operator.getLine(), operator.getColumn());
        }
        return expr;
    }

    private ExpressionNode comparison() {
        ExpressionNode expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            ExpressionNode right = term();
            expr = new BinaryNode(expr, operator.getType(), right, operator.getLine(), operator.getColumn());
        }
        return expr;
    }

    private ExpressionNode term() {
        ExpressionNode expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            ExpressionNode right = factor();
            expr = new BinaryNode(expr, operator.getType(), right, operator.getLine(), operator.getColumn());
        }
        return expr;
    }

    private ExpressionNode factor() {
        ExpressionNode expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR, TokenType.PERCENT)) {
            Token operator = previous();
            ExpressionNode right = unary();
            expr = new BinaryNode(expr, operator.getType(), right, operator.getLine(), operator.getColumn());
        }
        return expr;
    }

    private ExpressionNode unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            ExpressionNode operand = unary();
            return new UnaryNode(operator.getType(), operand, operator.getLine(), operator.getColumn());
        }
        return call();
    }

    private ExpressionNode call() {
        ExpressionNode expr = primary();
        while (true) {
            if (match(TokenType.LPAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'");
                expr = new MemberAccessNode(expr, name.getLexeme(), name.getLine(), name.getColumn());
            } else if (match(TokenType.LBRACKET)) {
                Token bracket = previous();
                ExpressionNode index = expression();
                consume(TokenType.RBRACKET, "Expect ']' after index");
                expr = new IndexAccessNode(expr, index, bracket.getLine(), bracket.getColumn());
            } else {
                break;
            }
        }
        return expr;
    }

    private ExpressionNode finishCall(ExpressionNode callee) {
        List<ExpressionNode> arguments = new ArrayList<>();
        Token paren = previous();
        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expect ')' after arguments");
        return new CallNode(callee, arguments, paren.getLine(), paren.getColumn());
    }

    private ExpressionNode primary() {
        if (match(TokenType.FALSE)) {
            return new LiteralNode(false, previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.TRUE)) {
            return new LiteralNode(true, previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.NULL)) {
            return new LiteralNode(null, previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new LiteralNode(previous().getLiteral(), previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.IDENTIFIER)) {
            return new IdentifierNode(previous().getLexeme(), previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.LBRACKET)) {
            Token bracket = previous();
            List<ExpressionNode> elements = new ArrayList<>();
            if (!check(TokenType.RBRACKET)) {
                do {
                    if (check(TokenType.RBRACKET)) break;
                    elements.add(expression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RBRACKET, "Expect ']' after array elements");
            return new ArrayLiteralNode(elements, bracket.getLine(), bracket.getColumn());
        }
        if (match(TokenType.LPAREN)) {
            ExpressionNode expr = expression();
            consume(TokenType.RPAREN, "Expect ')' after expression");
            return expr;
        }

        Token token = peek();
        error(token, "Expect expression");
        throw new ParseException();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        error(peek(), message);
        throw new ParseException();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void error(Token token, String message) {
        diagnostics.add(new ScriptDiagnostic(message, token.getLine(), token.getColumn(), true));
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().getType() == TokenType.SEMICOLON) return;
            switch (peek().getType()) {
                case LET:
                case FN:
                case IF:
                case RETURN:
                    return;
                default:
                    break;
            }
            advance();
        }
    }

    private static class ParseException extends RuntimeException {
    }
}
