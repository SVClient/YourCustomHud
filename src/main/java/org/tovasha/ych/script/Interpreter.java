package org.tovasha.ych.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.tovasha.ych.script.ast.AssignNode;
import org.tovasha.ych.script.ast.AstNode;
import org.tovasha.ych.script.ast.BinaryNode;
import org.tovasha.ych.script.ast.BlockNode;
import org.tovasha.ych.script.ast.BreakNode;
import org.tovasha.ych.script.ast.CallNode;
import org.tovasha.ych.script.ast.ContinueNode;
import org.tovasha.ych.script.ast.ExpressionNode;
import org.tovasha.ych.script.ast.ExpressionStatementNode;
import org.tovasha.ych.script.ast.ForNode;
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
import org.tovasha.ych.script.builtins.ScriptNamespace;

public class Interpreter {
    @Getter
    private final Environment globals = new Environment();

    public void registerGlobal(String name, Object value) {
        globals.define(name, value);
    }

    public void interpret(ProgramNode program) {
        for (StatementNode statement : program.getStatements()) {
            execute(statement, globals);
        }
    }

    public Object callFunction(String name, Object... args) {
        Object func = globals.get(name);
        if (func instanceof ScriptCallable) {
            ScriptCallable callable = (ScriptCallable) func;
            List<Object> argList = new ArrayList<>();
            for (Object arg : args) {
                argList.add(arg);
            }
            return callable.call(this, argList);
        }
        return null;
    }

    public boolean hasFunction(String name) {
        return globals.get(name) instanceof ScriptCallable;
    }

    public void execute(StatementNode stmt, Environment env) {
        if (stmt instanceof VarDeclNode) {
            VarDeclNode varDecl = (VarDeclNode) stmt;
            Object val = varDecl.getInitializer() != null ? evaluate(varDecl.getInitializer(), env) : null;
            env.define(varDecl.getName(), val);
        } else if (stmt instanceof FunctionDeclNode) {
            FunctionDeclNode funcDecl = (FunctionDeclNode) stmt;
            ScriptFunction func = new ScriptFunction(funcDecl, env);
            env.define(funcDecl.getName(), func);
        } else if (stmt instanceof IfNode) {
            IfNode ifNode = (IfNode) stmt;
            if (isTruthy(evaluate(ifNode.getCondition(), env))) {
                execute(ifNode.getThenBranch(), env);
            } else if (ifNode.getElseBranch() != null) {
                execute(ifNode.getElseBranch(), env);
            }
        } else if (stmt instanceof BlockNode) {
            BlockNode block = (BlockNode) stmt;
            Environment blockEnv = new Environment(env);
            for (StatementNode s : block.getStatements()) {
                execute(s, blockEnv);
            }
        } else if (stmt instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode) stmt;
            Object val = returnNode.getValue() != null ? evaluate(returnNode.getValue(), env) : null;
            throw new ReturnException(val);
        } else if (stmt instanceof WhileNode) {
            WhileNode whileNode = (WhileNode) stmt;
            int iterations = 0;
            while (isTruthy(evaluate(whileNode.getCondition(), env))) {
                if (++iterations > 10000) {
                    throw new RuntimeException("While loop exceeded maximum iterations (10000)");
                }
                try {
                    execute(whileNode.getBody(), env);
                } catch (BreakException e) {
                    break;
                } catch (ContinueException e) {
                }
            }
        } else if (stmt instanceof ForNode) {
            ForNode forNode = (ForNode) stmt;
            Environment forEnv = new Environment(env);
            if (forNode.getInitializer() != null) {
                execute(forNode.getInitializer(), forEnv);
            }
            int iterations = 0;
            while (forNode.getCondition() == null || isTruthy(evaluate(forNode.getCondition(), forEnv))) {
                if (++iterations > 10000) {
                    throw new RuntimeException("For loop exceeded maximum iterations (10000)");
                }
                try {
                    execute(forNode.getBody(), forEnv);
                } catch (BreakException e) {
                    break;
                } catch (ContinueException e) {
                }
                if (forNode.getIncrement() != null) {
                    evaluate(forNode.getIncrement(), forEnv);
                }
            }
        } else if (stmt instanceof BreakNode) {
            throw new BreakException();
        } else if (stmt instanceof ContinueNode) {
            throw new ContinueException();
        } else if (stmt instanceof SwitchNode) {
            SwitchNode switchNode = (SwitchNode) stmt;
            Object switchVal = evaluate(switchNode.getExpression(), env);
            boolean matched = false;
            try {
                for (SwitchCase sc : switchNode.getCases()) {
                    if (!matched) {
                        for (ExpressionNode valExpr : sc.getValues()) {
                            Object caseVal = evaluate(valExpr, env);
                            if (areEqual(switchVal, caseVal)) {
                                matched = true;
                                break;
                            }
                        }
                    }
                    if (matched) {
                        for (StatementNode s : sc.getStatements()) {
                            execute(s, env);
                        }
                    }
                }
                if (!matched && switchNode.getDefaultStatements() != null) {
                    for (StatementNode s : switchNode.getDefaultStatements()) {
                        execute(s, env);
                    }
                }
            } catch (BreakException e) {
            }
        } else if (stmt instanceof ExpressionStatementNode) {
            ExpressionStatementNode exprStmt = (ExpressionStatementNode) stmt;
            evaluate(exprStmt.getExpression(), env);
        } else if (stmt instanceof AssignNode) {
            evaluate((AssignNode) stmt, env);
        }
    }

    public Object evaluate(ExpressionNode expr, Environment env) {
        if (expr instanceof LiteralNode) {
            return ((LiteralNode) expr).getValue();
        }
        if (expr instanceof IdentifierNode) {
            String name = ((IdentifierNode) expr).getName();
            return env.get(name);
        }
        if (expr instanceof AssignNode) {
            AssignNode assign = (AssignNode) expr;
            Object val = evaluate(assign.getValue(), env);
            if (assign.getTarget() instanceof IdentifierNode) {
                String name = ((IdentifierNode) assign.getTarget()).getName();
                if (!env.assign(name, val)) {
                    env.define(name, val);
                }
            } else if (assign.getTarget() instanceof MemberAccessNode) {
                MemberAccessNode access = (MemberAccessNode) assign.getTarget();
                Object targetObj = evaluate(access.getObject(), env);
                if (targetObj instanceof ScriptNamespace) {
                    ((ScriptNamespace) targetObj).setProperty(access.getMember(), val);
                }
            }
            return val;
        }
        if (expr instanceof MemberAccessNode) {
            MemberAccessNode access = (MemberAccessNode) expr;
            Object target = evaluate(access.getObject(), env);
            if (target instanceof ScriptNamespace) {
                return ((ScriptNamespace) target).getProperty(access.getMember());
            }
            return null;
        }
        if (expr instanceof UnaryNode) {
            UnaryNode unary = (UnaryNode) expr;
            Object operand = evaluate(unary.getOperand(), env);
            switch (unary.getOperator()) {
                case MINUS:
                    return -toDouble(operand);
                case BANG:
                    return !isTruthy(operand);
                default:
                    return null;
            }
        }
        if (expr instanceof BinaryNode) {
            BinaryNode binary = (BinaryNode) expr;
            if (binary.getOperator() == TokenType.PIPE_PIPE) {
                Object left = evaluate(binary.getLeft(), env);
                if (isTruthy(left)) return left;
                return evaluate(binary.getRight(), env);
            }
            if (binary.getOperator() == TokenType.AMP_AMP) {
                Object left = evaluate(binary.getLeft(), env);
                if (!isTruthy(left)) return left;
                return evaluate(binary.getRight(), env);
            }

            Object left = evaluate(binary.getLeft(), env);
            Object right = evaluate(binary.getRight(), env);

            switch (binary.getOperator()) {
                case PLUS:
                    if (left instanceof String || right instanceof String) {
                        return formatString(left) + formatString(right);
                    }
                    return toDouble(left) + toDouble(right);
                case MINUS:
                    return toDouble(left) - toDouble(right);
                case STAR:
                    return toDouble(left) * toDouble(right);
                case SLASH: {
                    double r = toDouble(right);
                    return r == 0.0 ? 0.0 : toDouble(left) / r;
                }
                case PERCENT: {
                    double r = toDouble(right);
                    return r == 0.0 ? 0.0 : toDouble(left) % r;
                }
                case GREATER:
                    return toDouble(left) > toDouble(right);
                case GREATER_EQUAL:
                    return toDouble(left) >= toDouble(right);
                case LESS:
                    return toDouble(left) < toDouble(right);
                case LESS_EQUAL:
                    return toDouble(left) <= toDouble(right);
                case EQUAL_EQUAL:
                    return areEqual(left, right);
                case BANG_EQUAL:
                    return !areEqual(left, right);
                default:
                    return null;
            }
        }
        if (expr instanceof CallNode) {
            CallNode call = (CallNode) expr;
            Object callee = evaluate(call.getCallee(), env);
            if (callee instanceof ScriptCallable) {
                ScriptCallable callable = (ScriptCallable) callee;
                List<Object> args = new ArrayList<>();
                for (ExpressionNode arg : call.getArguments()) {
                    args.add(evaluate(arg, env));
                }
                return callable.call(this, args);
            }
            return null;
        }
        return null;
    }

    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0.0;
        if (o instanceof String) return !((String) o).isEmpty();
        return true;
    }

    private double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        if (o instanceof ScriptCallable) {
            Object res = ((ScriptCallable) o).call(this, Collections.emptyList());
            if (res instanceof Number) {
                return ((Number) res).doubleValue();
            }
        }
        if (o instanceof Boolean) {
            return (Boolean) o ? 1.0 : 0.0;
        }
        if (o instanceof String) {
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0.0;
    }

    private boolean areEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue()) == 0;
        }
        if (a instanceof String && b instanceof String) {
            return ((String) a).equalsIgnoreCase((String) b);
        }
        if (a instanceof Number && b instanceof String) {
            try {
                return Double.compare(((Number) a).doubleValue(), Double.parseDouble((String) b)) == 0;
            } catch (NumberFormatException ignored) {
            }
        }
        if (a instanceof String && b instanceof Number) {
            try {
                return Double.compare(Double.parseDouble((String) a), ((Number) b).doubleValue()) == 0;
            } catch (NumberFormatException ignored) {
            }
        }
        return Objects.equals(a, b);
    }

    private String formatString(Object o) {
        if (o == null) return "null";
        if (o instanceof Double) {
            double d = (Double) o;
            if (d == (long) d) {
                return String.format("%d", (long) d);
            }
        }
        return String.valueOf(o);
    }
}
