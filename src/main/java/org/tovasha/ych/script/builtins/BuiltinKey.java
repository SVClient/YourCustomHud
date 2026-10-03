package org.tovasha.ych.script.builtins;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.tovasha.ych.script.Interpreter;
import org.tovasha.ych.script.ScriptCallable;

public class BuiltinKey implements ScriptNamespace {
    private final Map<String, ScriptCallable> methods = new HashMap<>();

    public BuiltinKey() {
        ScriptCallable isDownCallable = new ScriptCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.isEmpty()) return false;
                Object arg = arguments.get(0);
                if (arg instanceof Number) {
                    return isKeyDownGlfw(((Number) arg).intValue());
                }
                return checkKey(String.valueOf(arg));
            }
        };

        methods.put("isdown", isDownCallable);
        methods.put("down", isDownCallable);
        methods.put("pressed", isDownCallable);
    }

    @Override
    public Object getProperty(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        if (methods.containsKey(lower)) {
            return methods.get(lower);
        }
        return checkKey(lower);
    }

    private static boolean checkKey(String key) {
        if (key == null || key.isEmpty()) return false;
        String k = key.toLowerCase();
        if (k.startsWith("key_") && k.length() > 4) {
            k = k.substring(4);
        } else if (k.startsWith("key") && k.length() > 3 && !k.equals("keyboard")) {
            k = k.substring(3);
        }

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        long handle = window != null ? window.handle() : 0;

        switch (k) {
            case "w":
            case "forward":
                if (mc.options != null && mc.options.keyUp.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_W);
            case "a":
            case "left":
                if (mc.options != null && mc.options.keyLeft.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_A);
            case "s":
            case "back":
            case "down":
                if (mc.options != null && mc.options.keyDown.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_S);
            case "d":
            case "right":
                if (mc.options != null && mc.options.keyRight.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_D);
            case "space":
            case "spacebar":
            case "jump":
                if (mc.options != null && mc.options.keyJump.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_SPACE);
            case "shift":
            case "sneak":
                if (mc.options != null && mc.options.keyShift.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDownGlfw(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
            case "lshift":
            case "leftshift":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_LEFT_SHIFT);
            case "rshift":
            case "rightshift":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
            case "ctrl":
            case "control":
            case "sprint":
                if (mc.options != null && mc.options.keySprint.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyDownGlfw(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
            case "lctrl":
            case "leftctrl":
            case "leftcontrol":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_LEFT_CONTROL);
            case "rctrl":
            case "rightctrl":
            case "rightcontrol":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
            case "alt":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_LEFT_ALT) || isKeyDownGlfw(window, GLFW.GLFW_KEY_RIGHT_ALT);
            case "lalt":
            case "leftalt":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_LEFT_ALT);
            case "ralt":
            case "rightalt":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_RIGHT_ALT);
            case "tab":
                if (mc.options != null && mc.options.keyPlayerList.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_TAB);
            case "enter":
            case "return":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_ENTER) || isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_ENTER);
            case "esc":
            case "escape":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_ESCAPE);
            case "backspace":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_BACKSPACE);
            case "caps":
            case "capslock":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_CAPS_LOCK);
            case "numlock":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_NUM_LOCK);
            case "scrolllock":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_SCROLL_LOCK);
            case "insert":
            case "ins":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_INSERT);
            case "delete":
            case "del":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_DELETE);
            case "home":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_HOME);
            case "end":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_END);
            case "pageup":
            case "pgup":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_PAGE_UP);
            case "pagedown":
            case "pgdn":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_PAGE_DOWN);
            case "arrowup":
            case "uparrow":
            case "up":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_UP);
            case "arrowdown":
            case "downarrow":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_DOWN);
            case "arrowleft":
            case "leftarrow":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_LEFT);
            case "arrowright":
            case "rightarrow":
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_RIGHT);
            case "lmb":
            case "attack":
            case "mouse1":
            case "leftclick":
                return (mc.options != null && mc.options.keyAttack.isDown())
                        || (mc.mouseHandler != null && mc.mouseHandler.isLeftPressed())
                        || isMouseDownGlfw(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT);
            case "rmb":
            case "use":
            case "mouse2":
            case "rightclick":
                return (mc.options != null && mc.options.keyUse.isDown())
                        || (mc.mouseHandler != null && mc.mouseHandler.isRightPressed())
                        || isMouseDownGlfw(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            case "mmb":
            case "middle":
            case "mouse3":
            case "middleclick":
            case "pick":
            case "pickitem":
                return (mc.options != null && mc.options.keyPickItem.isDown())
                        || (mc.mouseHandler != null && mc.mouseHandler.isMiddlePressed())
                        || isMouseDownGlfw(handle, GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
            case "mouse4":
                return isMouseDownGlfw(handle, 3);
            case "mouse5":
                return isMouseDownGlfw(handle, 4);
            case "inventory":
            case "inv":
            case "e":
                if (mc.options != null && mc.options.keyInventory.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_E);
            case "drop":
            case "q":
                if (mc.options != null && mc.options.keyDrop.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_Q);
            case "chat":
            case "t":
                if (mc.options != null && mc.options.keyChat.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_T);
            case "playerlist":
            case "tablist":
                if (mc.options != null && mc.options.keyPlayerList.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_TAB);
            case "perspective":
            case "f5":
                if (mc.options != null && mc.options.keyTogglePerspective.isDown()) return true;
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_F5);
        }

        if (k.length() == 1) {
            char c = Character.toUpperCase(k.charAt(0));
            if (c >= 'A' && c <= 'Z') {
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_A + (c - 'A'));
            }
            if (c >= '0' && c <= '9') {
                return isKeyDownGlfw(window, GLFW.GLFW_KEY_0 + (c - '0'));
            }
        }

        if (k.startsWith("f") && k.length() >= 2 && k.length() <= 3) {
            try {
                int fNum = Integer.parseInt(k.substring(1));
                if (fNum >= 1 && fNum <= 25) {
                    return isKeyDownGlfw(window, GLFW.GLFW_KEY_F1 + (fNum - 1));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (k.startsWith("num") || k.startsWith("digit")) {
            String sub = k.replaceAll("[^0-9]", "");
            if (!sub.isEmpty()) {
                int d = Integer.parseInt(sub);
                if (d >= 0 && d <= 9) {
                    return isKeyDownGlfw(window, GLFW.GLFW_KEY_0 + d);
                }
            }
        }

        if (k.startsWith("numpad") || k.startsWith("np")) {
            if (k.endsWith("0")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_0);
            if (k.endsWith("1")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_1);
            if (k.endsWith("2")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_2);
            if (k.endsWith("3")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_3);
            if (k.endsWith("4")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_4);
            if (k.endsWith("5")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_5);
            if (k.endsWith("6")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_6);
            if (k.endsWith("7")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_7);
            if (k.endsWith("8")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_8);
            if (k.endsWith("9")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_9);
            if (k.contains("add") || k.contains("plus")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_ADD);
            if (k.contains("sub") || k.contains("minus")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_SUBTRACT);
            if (k.contains("mul") || k.contains("star")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_MULTIPLY);
            if (k.contains("div") || k.contains("slash")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_DIVIDE);
            if (k.contains("enter")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_ENTER);
            if (k.contains("dec") || k.contains("dot") || k.contains("point")) return isKeyDownGlfw(window, GLFW.GLFW_KEY_KP_DECIMAL);
        }

        return false;
    }

    private static boolean isKeyDownGlfw(int keyCode) {
        Minecraft mc = Minecraft.getInstance();
        return isKeyDownGlfw(mc.getWindow(), keyCode);
    }

    private static boolean isKeyDownGlfw(Window window, int keyCode) {
        if (window == null) return false;
        return InputConstants.isKeyDown(window, keyCode);
    }

    private static boolean isMouseDownGlfw(long handle, int button) {
        if (handle == 0) return false;
        return GLFW.glfwGetMouseButton(handle, button) == GLFW.GLFW_PRESS;
    }

    @Override
    public void setProperty(String name, Object value) {
    }
}
