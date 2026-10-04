package org.tovasha.ych.api;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import org.tovasha.ych.script.Interpreter;
import org.tovasha.ych.script.Lexer;
import org.tovasha.ych.script.Parser;
import org.tovasha.ych.script.ReturnException;
import org.tovasha.ych.script.ScriptDiagnostic;
import org.tovasha.ych.script.Token;
import org.tovasha.ych.script.ast.ProgramNode;
import org.tovasha.ych.render.TargetTracker;
import org.tovasha.ych.script.builtins.BuiltinFont;
import org.tovasha.ych.script.builtins.BuiltinKey;
import org.tovasha.ych.script.builtins.BuiltinMath;
import org.tovasha.ych.script.builtins.BuiltinParams;
import org.tovasha.ych.script.builtins.BuiltinRender;
import org.tovasha.ych.script.builtins.BuiltinTarget;
import org.tovasha.ych.script.builtins.BuiltinVariables;

@Getter
@Setter
public class HudElement {
    private String id;
    private String name;
    private String code;
    private boolean enabled = true;
    private float x = 10;
    private float y = 10;
    private float width = 100;
    private float height = 30;
    private String font = "default";
    private ProgramNode programNode;

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setFont(String font) {
        if (font == null || font.isEmpty() || font.contains("BuiltinFont") || font.startsWith("org.tovasha.")) {
            this.font = "default";
        } else {
            this.font = font;
        }
    }

    private Interpreter interpreter;
    private BuiltinRender builtinRender;
    private BuiltinParams builtinParams;
    private List<ScriptDiagnostic> diagnostics = new ArrayList<>();

    public HudElement(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code != null ? code : "";
        compile();
    }

    public void compile() {
        diagnostics.clear();
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        diagnostics.addAll(lexer.getDiagnostics());

        Parser parser = new Parser(tokens);
        programNode = parser.parse();
        diagnostics.addAll(parser.getDiagnostics());

        interpreter = new Interpreter();
        builtinRender = new BuiltinRender(this);
        builtinParams = new BuiltinParams(this);

        interpreter.registerGlobal("Params", builtinParams);
        interpreter.registerGlobal("Variables", new BuiltinVariables());
        BuiltinKey builtinKey = new BuiltinKey();
        interpreter.registerGlobal("Key", builtinKey);
        interpreter.registerGlobal("Keys", builtinKey);
        interpreter.registerGlobal("Math", new BuiltinMath());
        interpreter.registerGlobal("Font", new BuiltinFont(this));
        interpreter.registerGlobal("Render", builtinRender);
        BuiltinTarget builtinTarget = TargetTracker.getTargetNamespace();
        interpreter.registerGlobal("Target", builtinTarget);
        interpreter.registerGlobal("target", builtinTarget);

        try {
            interpreter.interpret(programNode);
        } catch (ReturnException ignored) {
        } catch (Exception e) {
            diagnostics.add(new ScriptDiagnostic("Runtime init error: " + e.getMessage(), 1, 1, true));
        }
    }

    public void render(GuiGraphics graphics, float deltaTick) {
        if (!enabled || interpreter == null) {
            return;
        }
        builtinRender.setGraphics(graphics);
        try {
            if (interpreter.hasFunction("main")) {
                interpreter.callFunction("main");
            } else if (programNode != null) {
                interpreter.interpret(programNode);
            }
        } catch (ReturnException ignored) {
        } catch (Exception e) {
            diagnostics.add(new ScriptDiagnostic("Render error: " + e.getMessage(), 1, 1, true));
        }
    }

    public void tick() {
        if (!enabled || interpreter == null) {
            return;
        }
        try {
            interpreter.callFunction("tick");
        } catch (Exception ignored) {
        }
    }

    public void keyPressed(int key, int action) {
        if (!enabled || interpreter == null) {
            return;
        }
        try {
            interpreter.callFunction("keyPressed", (double) key, (double) action);
        } catch (Exception ignored) {
        }
    }

    public void attack(BuiltinTarget target) {
        if (!enabled || interpreter == null) {
            return;
        }
        try {
            interpreter.callFunction("attack", target);
        } catch (Exception ignored) {
        }
    }
}
