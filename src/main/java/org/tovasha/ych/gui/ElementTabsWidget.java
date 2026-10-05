package org.tovasha.ych.gui;

import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;
import org.tovasha.ych.YourCustomHud;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.api.HudRegistry;
import org.tovasha.ych.render.RenderUtils;
import org.tovasha.ych.storage.StorageManager;

@Getter
@Setter
public class ElementTabsWidget {
    private int x;
    private int y;
    private int width;
    private int height;

    private HudElement selectedElement;
    private HudElement draggingElement;
    private double dragStartX;
    private boolean isDraggingTab = false;
    private int scrollOffset = 0;
    private String nameInput = "";
    private boolean inputFocused = false;
    private Consumer<HudElement> onElementChanged;
    private Runnable onPresetModified;

    public ElementTabsWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setSelectedElement(HudElement element) {
        this.selectedElement = element;
        this.nameInput = element != null ? element.getName() : "";
    }

    private int computeTotalTabsWidth() {
        int total = 0;
        for (HudElement el : HudRegistry.getElements()) {
            total += RenderUtils.getTextWidth(el.getName()) + 26;
        }
        return total;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        RenderUtils.drawRect(graphics, x, y, width, height, Theme.getBgHeader());
        RenderUtils.drawRect(graphics, x, y + height - 1, width, 1, Theme.getBorder());

        int curX = x + 8;
        int centerY = y + height / 2;

        boolean hoverPrev = mouseX >= curX - 4 && mouseX <= curX + 12 && mouseY >= y && mouseY <= y + height;
        drawChevron(graphics, curX + 4, centerY, true, hoverPrev ? Theme.getTextPrimary() : Theme.getTextSecondary());
        curX += 16;

        int tabsEndX = x + width - 110;
        int totalW = computeTotalTabsWidth();
        int maxScroll = Math.max(0, totalW - (tabsEndX - curX));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        graphics.enableScissor(curX, y, tabsEndX, y + height);

        List<HudElement> elements = HudRegistry.getElements();
        int itemX = curX - scrollOffset;

        for (HudElement el : elements) {
            boolean isActive = el == selectedElement;
            boolean isDraggingThis = isDraggingTab && el == draggingElement;
            int textW = RenderUtils.getTextWidth(el.getName());
            int tabW = textW + 24;

            if (itemX + tabW >= curX && itemX <= tabsEndX) {
                boolean hover = mouseX >= itemX && mouseX <= itemX + tabW && mouseY >= y + 3 && mouseY <= y + height - 3;
                if (isDraggingThis) {
                    RenderUtils.drawRoundedRect(graphics, itemX, y + 4, tabW, height - 8, 4, 0xFF4338CA);
                    RenderUtils.drawRoundedOutline(graphics, itemX, y + 4, tabW, height - 8, 4, 1.5f, 0xFF6366F1);
                } else if (isActive) {
                    RenderUtils.drawRoundedRect(graphics, itemX, y + 4, tabW, height - 8, 4, Theme.getAccent());
                } else if (hover) {
                    RenderUtils.drawRoundedRect(graphics, itemX, y + 4, tabW, height - 8, 4, 0xFF2A2A2E);
                } else {
                    RenderUtils.drawRoundedRect(graphics, itemX, y + 4, tabW, height - 8, 4, 0xFF1E1E22);
                }

                int dotColor = el.isEnabled() ? 0xFF22C55E : 0xFFEF4444;
                RenderUtils.drawRoundedRect(graphics, itemX + 6, centerY - 2, 5, 5, 2, dotColor);

                int textColor = isActive || isDraggingThis ? 0xFFFFFFFF : (hover ? Theme.getTextPrimary() : Theme.getTextSecondary());
                RenderUtils.drawText(graphics, el.getName(), itemX + 15, y + (height - 9) / 2, textColor);
            }
            itemX += tabW + 5;
        }

        graphics.disableScissor();

        curX = tabsEndX + 4;
        boolean hoverNext = mouseX >= curX - 4 && mouseX <= curX + 12 && mouseY >= y && mouseY <= y + height;
        drawChevron(graphics, curX + 4, centerY, false, hoverNext ? Theme.getTextPrimary() : Theme.getTextSecondary());
        curX += 16;

        int inputW = 52;
        int inputH = height - 8;
        int inputY = y + 4;
        RenderUtils.drawRoundedRect(graphics, curX, inputY, inputW, inputH, 3, inputFocused ? 0xFF2A2A30 : 0xFF18181B);
        RenderUtils.drawRoundedOutline(graphics, curX, inputY, inputW, inputH, 3, 1, inputFocused ? Theme.getAccent() : Theme.getBorder());

        graphics.enableScissor(curX + 3, inputY, curX + inputW - 3, inputY + inputH);
        String displayInput = nameInput.isEmpty() && !inputFocused ? "Element" : nameInput;
        int inputColor = nameInput.isEmpty() && !inputFocused ? Theme.getTextSecondary() : Theme.getTextPrimary();
        RenderUtils.drawText(graphics, displayInput, curX + 5, y + (height - 9) / 2, inputColor);
        graphics.disableScissor();

        curX += inputW + 6;

        boolean hoverMinus = mouseX >= curX && mouseX <= curX + 14 && mouseY >= y && mouseY <= y + height;
        RenderUtils.drawRoundedRect(graphics, curX, inputY, 14, inputH, 3, hoverMinus ? 0xFFEF4444 : 0xFF222226);
        drawMinus(graphics, curX + 7, centerY, hoverMinus ? 0xFFFFFFFF : Theme.getTextPrimary());
        curX += 18;

        boolean hoverPlus = mouseX >= curX && mouseX <= curX + 14 && mouseY >= y && mouseY <= y + height;
        RenderUtils.drawRoundedRect(graphics, curX, inputY, 14, inputH, 3, hoverPlus ? 0xFF22C55E : 0xFF222226);
        drawPlus(graphics, curX + 7, centerY, hoverPlus ? 0xFFFFFFFF : Theme.getTextPrimary());
    }

    private void drawChevron(GuiGraphicsExtractor graphics, int cx, int cy, boolean left, int color) {
        int dir = left ? 1 : -1;
        int tipX = left ? cx - 2 : cx + 2;
        for (int i = 0; i < 4; i++) {
            graphics.fill(tipX + i * dir, cy - i, tipX + i * dir + 1, cy - i + 1, color);
            graphics.fill(tipX + i * dir, cy + i, tipX + i * dir + 1, cy + i + 1, color);
        }
    }

    private void drawPlus(GuiGraphicsExtractor graphics, int cx, int cy, int color) {
        graphics.fill(cx - 3, cy, cx + 4, cy + 1, color);
        graphics.fill(cx, cy - 3, cx + 1, cy + 4, color);
    }

    private void drawMinus(GuiGraphicsExtractor graphics, int cx, int cy, int color) {
        graphics.fill(cx - 3, cy, cx + 4, cy + 1, color);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        if (mx < x || mx > x + width || my < y || my > y + height) {
            inputFocused = false;
            return false;
        }

        int curX = x + 8;

        if (mx >= curX - 4 && mx <= curX + 12) {
            scrollOffset = Math.max(0, scrollOffset - 40);
            return true;
        }
        curX += 16;

        int tabsEndX = x + width - 110;
        if (mx >= curX && mx <= tabsEndX) {
            List<HudElement> elements = HudRegistry.getElements();
            int itemX = curX - scrollOffset;
            for (HudElement el : elements) {
                int textW = RenderUtils.getTextWidth(el.getName());
                int tabW = textW + 24;
                if (mx >= itemX && mx <= itemX + tabW) {
                    if (mx >= itemX && mx <= itemX + 13) {
                        el.setEnabled(!el.isEnabled());
                        if (onPresetModified != null) {
                            onPresetModified.run();
                        }
                    } else {
                        selectedElement = el;
                        draggingElement = el;
                        dragStartX = mx;
                        isDraggingTab = false;
                        nameInput = el.getName();
                        if (onElementChanged != null) {
                            onElementChanged.accept(el);
                        }
                    }
                    return true;
                }
                itemX += tabW + 5;
            }
        }

        curX = tabsEndX + 4;
        if (mx >= curX - 4 && mx <= curX + 12) {
            int totalW = computeTotalTabsWidth();
            int maxScroll = Math.max(0, totalW - (tabsEndX - (x + 24)));
            scrollOffset = Math.min(maxScroll, scrollOffset + 40);
            return true;
        }
        curX += 16;

        int inputW = 52;
        int inputH = height - 8;
        int inputY = y + 4;
        if (mx >= curX && mx <= curX + inputW && my >= inputY && my <= inputY + inputH) {
            inputFocused = true;
            return true;
        }
        inputFocused = false;
        curX += inputW + 6;

        if (mx >= curX && mx <= curX + 14) {
            if (selectedElement != null) {
                String toDeleteId = selectedElement.getId();
                HudRegistry.unregister(toDeleteId);
                StorageManager.deleteElement(YourCustomHud.CONFIG.getActivePreset(), toDeleteId);
                List<HudElement> remaining = HudRegistry.getElements();
                selectedElement = remaining.isEmpty() ? null : remaining.get(0);
                nameInput = selectedElement != null ? selectedElement.getName() : "";
                if (onElementChanged != null) {
                    onElementChanged.accept(selectedElement);
                }
                if (onPresetModified != null) {
                    onPresetModified.run();
                }
            }
            return true;
        }
        curX += 18;

        if (mx >= curX && mx <= curX + 14) {
            String newName = nameInput.trim();
            if (newName.isEmpty() || "Element".equalsIgnoreCase(newName)) {
                newName = "el" + (HudRegistry.getElements().size() + 1);
            }
            String newId = newName.toLowerCase().replaceAll("[^a-z0-9_]", "_") + "_" + (System.currentTimeMillis() % 10000);
            HudElement newEl = new HudElement(newId, newName,
                    "let bg = 0x88000000;\n" +
                    "fn main() {\n" +
                    "    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 4, bg);\n" +
                    "    Render.drawText(\"" + newName + "\", Params.x + 8, Params.y + 8, 0xFFFFFFFF);\n" +
                    "}\n"
            );
            newEl.setX(20);
            newEl.setY(20);
            newEl.setWidth(100);
            newEl.setHeight(25);
            HudRegistry.register(newEl);
            selectedElement = newEl;
            nameInput = newName;
            if (onElementChanged != null) {
                onElementChanged.accept(newEl);
            }
            if (onPresetModified != null) {
                onPresetModified.run();
            }
            return true;
        }

        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingElement != null) {
            double mx = event.x();
            if (Math.abs(mx - dragStartX) > 4) {
                isDraggingTab = true;
            }
            if (isDraggingTab) {
                List<HudElement> elements = HudRegistry.getElements();
                int currentIndex = elements.indexOf(draggingElement);
                if (currentIndex >= 0) {
                    int curX = x + 24;
                    int itemX = curX - scrollOffset;
                    for (int i = 0; i < elements.size(); i++) {
                        HudElement el = elements.get(i);
                        int textW = RenderUtils.getTextWidth(el.getName());
                        int tabW = textW + 24;
                        if (el != draggingElement && mx >= itemX && mx <= itemX + tabW) {
                            HudRegistry.moveElement(currentIndex, i);
                            break;
                        }
                        itemX += tabW + 5;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingElement != null) {
            boolean wasDragging = isDraggingTab;
            draggingElement = null;
            isDraggingTab = false;
            if (wasDragging && onPresetModified != null) {
                onPresetModified.run();
            }
            return wasDragging;
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        if (!inputFocused) return false;

        int key = event.key();
        if (key == GLFW.GLFW_KEY_BACKSPACE && !nameInput.isEmpty()) {
            nameInput = nameInput.substring(0, nameInput.length() - 1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            String trimmed = nameInput.trim();
            if (!trimmed.isEmpty() && selectedElement != null) {
                selectedElement.setName(trimmed);
                if (onElementChanged != null) {
                    onElementChanged.accept(selectedElement);
                }
                if (onPresetModified != null) {
                    onPresetModified.run();
                }
            }
            inputFocused = false;
            return true;
        }
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        if (!inputFocused) return false;

        int codepoint = event.codepoint();
        if (Character.isBmpCodePoint(codepoint)) {
            char c = (char) codepoint;
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ' ') {
                nameInput += c;
                return true;
            }
        }
        return false;
    }
}
