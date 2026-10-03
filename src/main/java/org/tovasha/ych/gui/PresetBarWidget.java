package org.tovasha.ych.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;
import org.tovasha.ych.render.RenderUtils;
import org.tovasha.ych.storage.StorageManager;

@Getter
@Setter
public class PresetBarWidget {
    private int x;
    private int y;
    private int width;
    private int height;

    private List<String> presets = new ArrayList<>();
    private String activePreset = "default";
    private int scrollOffset = 0;
    private String nameInput = "";
    private boolean inputFocused = false;
    private Consumer<String> onPresetChanged;

    public PresetBarWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        refreshPresets();
    }

    public void refreshPresets() {
        presets = StorageManager.listPresets();
        if (presets.isEmpty()) {
            presets.add("default");
        }
        if (!presets.contains(activePreset)) {
            activePreset = presets.get(0);
        }
        nameInput = activePreset;
    }

    private int computeTotalPresetsWidth() {
        int total = 0;
        for (String p : presets) {
            total += RenderUtils.getTextWidth(p) + 18;
        }
        return total;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        RenderUtils.drawRect(graphics, x, y, width, height, Theme.getBgHeader());
        RenderUtils.drawRect(graphics, x, y + height - 1, width, 1, Theme.getBorder());

        int curX = x + 8;
        int centerY = y + height / 2;

        boolean hoverPrev = mouseX >= curX - 4 && mouseX <= curX + 12 && mouseY >= y && mouseY <= y + height;
        drawChevron(graphics, curX + 4, centerY, true, hoverPrev ? Theme.getTextPrimary() : Theme.getTextSecondary());
        curX += 16;

        int presetListEndX = x + width - 110;
        int totalW = computeTotalPresetsWidth();
        int maxScroll = Math.max(0, totalW - (presetListEndX - curX));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        graphics.enableScissor(curX, y, presetListEndX, y + height);

        int itemX = curX - scrollOffset;
        for (String preset : presets) {
            boolean isActive = preset.equals(activePreset);
            int textW = RenderUtils.getTextWidth(preset);
            int pWidth = textW + 16;

            if (itemX + pWidth >= curX && itemX <= presetListEndX) {
                boolean hover = mouseX >= itemX && mouseX <= itemX + pWidth && mouseY >= y + 3 && mouseY <= y + height - 3;
                if (isActive) {
                    RenderUtils.drawRoundedRect(graphics, itemX, y + 4, pWidth, height - 8, 4, Theme.getAccent());
                } else if (hover) {
                    RenderUtils.drawRoundedRect(graphics, itemX, y + 4, pWidth, height - 8, 4, 0xFF2A2A2E);
                } else {
                    RenderUtils.drawRoundedRect(graphics, itemX, y + 4, pWidth, height - 8, 4, 0xFF1E1E22);
                }

                int textColor = isActive ? 0xFFFFFFFF : (hover ? Theme.getTextPrimary() : Theme.getTextSecondary());
                RenderUtils.drawText(graphics, preset, itemX + 8, y + (height - 9) / 2, textColor);
            }
            itemX += pWidth + 5;
        }

        graphics.disableScissor();

        curX = presetListEndX + 4;
        boolean hoverNext = mouseX >= curX - 4 && mouseX <= curX + 12 && mouseY >= y && mouseY <= y + height;
        drawChevron(graphics, curX + 4, centerY, false, hoverNext ? Theme.getTextPrimary() : Theme.getTextSecondary());
        curX += 16;

        int inputW = 52;
        int inputH = height - 8;
        int inputY = y + 4;
        RenderUtils.drawRoundedRect(graphics, curX, inputY, inputW, inputH, 3, inputFocused ? 0xFF2A2A30 : 0xFF18181B);
        RenderUtils.drawRoundedOutline(graphics, curX, inputY, inputW, inputH, 3, 1, inputFocused ? Theme.getAccent() : Theme.getBorder());

        graphics.enableScissor(curX + 3, inputY, curX + inputW - 3, inputY + inputH);
        String displayInput = nameInput.isEmpty() && !inputFocused ? "Preset" : nameInput;
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

    private void drawChevron(GuiGraphics graphics, int cx, int cy, boolean left, int color) {
        int dir = left ? -1 : 1;
        for (int i = 0; i < 4; i++) {
            graphics.fill(cx + i * dir, cy - i, cx + i * dir + 1, cy - i + 1, color);
            graphics.fill(cx + i * dir, cy + i, cx + i * dir + 1, cy + i + 1, color);
        }
    }

    private void drawPlus(GuiGraphics graphics, int cx, int cy, int color) {
        graphics.fill(cx - 3, cy, cx + 4, cy + 1, color);
        graphics.fill(cx, cy - 3, cx + 1, cy + 4, color);
    }

    private void drawMinus(GuiGraphics graphics, int cx, int cy, int color) {
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

        int presetListEndX = x + width - 110;
        if (mx >= curX && mx <= presetListEndX) {
            int itemX = curX - scrollOffset;
            for (String preset : presets) {
                int pWidth = RenderUtils.getTextWidth(preset) + 16;
                if (mx >= itemX && mx <= itemX + pWidth) {
                    activePreset = preset;
                    nameInput = preset;
                    if (onPresetChanged != null) {
                        onPresetChanged.accept(preset);
                    }
                    return true;
                }
                itemX += pWidth + 5;
            }
        }

        curX = presetListEndX + 4;
        if (mx >= curX - 4 && mx <= curX + 12) {
            int totalW = computeTotalPresetsWidth();
            int maxScroll = Math.max(0, totalW - (presetListEndX - (x + 24)));
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
            if (presets.size() > 1) {
                StorageManager.deletePreset(activePreset);
                refreshPresets();
                activePreset = presets.get(0);
                nameInput = activePreset;
                if (onPresetChanged != null) {
                    onPresetChanged.accept(activePreset);
                }
            }
            return true;
        }
        curX += 18;

        if (mx >= curX && mx <= curX + 14) {
            String newName = nameInput.trim();
            if (newName.isEmpty() || presets.contains(newName)) {
                newName = "preset " + (presets.size() + 1);
            }
            StorageManager.savePreset(newName);
            refreshPresets();
            activePreset = newName;
            nameInput = newName;
            if (onPresetChanged != null) {
                onPresetChanged.accept(activePreset);
            }
            return true;
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
            if (!trimmed.isEmpty() && !trimmed.equals(activePreset)) {
                StorageManager.deletePreset(activePreset);
                StorageManager.savePreset(trimmed);
                refreshPresets();
                activePreset = trimmed;
                if (onPresetChanged != null) {
                    onPresetChanged.accept(activePreset);
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
