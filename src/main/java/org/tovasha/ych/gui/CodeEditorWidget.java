package org.tovasha.ych.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.render.RenderUtils;
import org.tovasha.ych.script.ScriptDiagnostic;

@Getter
@Setter
public class CodeEditorWidget {
    private int x;
    private int y;
    private int width;
    private int height;

    private HudElement element;
    private List<String> lines = new ArrayList<>();
    private int cursorLine = 0;
    private int cursorCol = 0;
    private int selectionAnchorLine = -1;
    private int selectionAnchorCol = -1;
    private int scrollY = 0;
    private int scrollX = 0;
    private boolean draggingScrollY = false;
    private boolean draggingScrollX = false;
    private double dragStartMousePos = 0;
    private int dragStartScroll = 0;
    private long lastBlink = System.currentTimeMillis();
    private boolean cursorVisible = true;
    private boolean focused = true;

    private static final int LINE_HEIGHT = 13;
    private static final int GUTTER_WIDTH = 34;

    public CodeEditorWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        lines.add("");
    }

    public void setElement(HudElement element) {
        this.element = element;
        if (element != null) {
            setText(element.getCode());
        } else {
            setText("");
        }
    }

    public void setText(String text) {
        lines.clear();
        if (text == null || text.isEmpty()) {
            lines.add("");
        } else {
            String[] split = text.split("\r?\n", -1);
            lines.addAll(Arrays.asList(split));
        }
        clampCursor();
        clearSelection();
    }

    public String getText() {
        return String.join("\n", lines);
    }

    private String getCurrentLine() {
        if (cursorLine >= 0 && cursorLine < lines.size()) {
            return lines.get(cursorLine);
        }
        return "";
    }

    private void clampCursor() {
        if (lines.isEmpty()) {
            lines.add("");
        }
        cursorLine = Math.max(0, Math.min(cursorLine, lines.size() - 1));
        cursorCol = Math.max(0, Math.min(cursorCol, getCurrentLine().length()));
    }

    private Component styled(String text) {
        return Component.literal(text).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(RenderUtils.FONT_CODE)));
    }

    private int getMaxLineWidth(Font font) {
        int maxW = 0;
        for (String line : lines) {
            int w = font.width(styled(line));
            if (w > maxW) maxW = w;
        }
        return maxW;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        RenderUtils.drawRect(graphics, x, y, width, height, Theme.getEditorBg());
        RenderUtils.drawRect(graphics, x, y, GUTTER_WIDTH, height, Theme.getEditorGutter());
        RenderUtils.drawRect(graphics, x + GUTTER_WIDTH - 1, y, 1, height, Theme.getBorder());

        int textAreaW = width - GUTTER_WIDTH - 12;
        int maxLineW = getMaxLineWidth(font);
        int maxScrollX = Math.max(0, maxLineW - textAreaW + 40);
        scrollX = Math.max(0, Math.min(scrollX, maxScrollX));

        int visibleLines = (height - 14) / LINE_HEIGHT;
        int maxScrollY = Math.max(0, lines.size() - visibleLines + 1);
        scrollY = Math.max(0, Math.min(scrollY, maxScrollY));

        int startLine = scrollY;
        int endLine = Math.min(lines.size(), scrollY + visibleLines + 2);

        String hoveredError = null;

        graphics.enableScissor(x, y, x + GUTTER_WIDTH - 2, y + height - 12);
        for (int i = startLine; i < endLine; i++) {
            int lineY = y + 4 + (i - scrollY) * LINE_HEIGHT;
            String lineNumStr = String.valueOf(i + 1);
            Component lineNumComp = styled(lineNumStr);
            int lineNumX = x + GUTTER_WIDTH - 6 - font.width(lineNumComp);
            graphics.drawString(font, lineNumComp, lineNumX, lineY, Theme.getEditorGutterText(), false);
        }
        graphics.disableScissor();

        graphics.enableScissor(x + GUTTER_WIDTH, y, x + width - 10, y + height - 12);

        for (int i = startLine; i < endLine; i++) {
            int lineY = y + 4 + (i - scrollY) * LINE_HEIGHT;
            String lineText = lines.get(i);
            int textX = x + GUTTER_WIDTH + 8 - scrollX;

            if (hasSelection()) {
                renderSelection(graphics, i, textX, lineY, lineText, font);
            }

            renderSyntaxHighlightedLine(graphics, lineText, textX, lineY, font);

            if (element != null) {
                for (ScriptDiagnostic diag : element.getDiagnostics()) {
                    if (diag.getLine() - 1 == i) {
                        String prefix = diag.getColumn() > 1 ? lineText.substring(0, Math.min(lineText.length(), diag.getColumn() - 1)) : "";
                        int errStartX = textX + font.width(styled(prefix));
                        int errWidth = Math.max(10, font.width("~~"));
                        RenderUtils.drawRect(graphics, errStartX, lineY + LINE_HEIGHT - 2, errWidth, 2, diag.isError() ? 0xFFEF4444 : 0xFFF59E0B);
                        if (mouseX >= errStartX && mouseX <= errStartX + errWidth && mouseY >= lineY && mouseY <= lineY + LINE_HEIGHT) {
                            hoveredError = diag.getMessage();
                        }
                    }
                }
            }
        }

        if (System.currentTimeMillis() - lastBlink > 500) {
            cursorVisible = !cursorVisible;
            lastBlink = System.currentTimeMillis();
        }

        if (focused && cursorVisible && cursorLine >= scrollY && cursorLine < endLine) {
            int cursorH = 10;
            int cursorY = y + 4 + (cursorLine - scrollY) * LINE_HEIGHT + (LINE_HEIGHT - cursorH) / 2;
            String curLine = getCurrentLine();
            String prefix = curLine.substring(0, Math.min(cursorCol, curLine.length()));
            int cursorX = x + GUTTER_WIDTH + 8 - scrollX + font.width(styled(prefix));
            RenderUtils.drawRect(graphics, cursorX, cursorY, 1.0f, cursorH, Theme.getTextPrimary());
        }

        graphics.disableScissor();

        if (maxScrollY > 0) {
            int vTrackX = x + width - 8;
            int vTrackY = y + 2;
            int vTrackH = height - 14;
            RenderUtils.drawRoundedRect(graphics, vTrackX, vTrackY, 6, vTrackH, 3, 0x1AFFFFFF);
            int barH = Math.max(20, (int) ((float) vTrackH * visibleLines / lines.size()));
            int barY = vTrackY + (int) ((float) scrollY * (vTrackH - barH) / maxScrollY);
            boolean vHover = (mouseX >= vTrackX && mouseX <= vTrackX + 6 && mouseY >= barY && mouseY <= barY + barH);
            int vColor = (draggingScrollY || vHover) ? 0x99A0AEC0 : 0x55A0AEC0;
            RenderUtils.drawRoundedRect(graphics, vTrackX, barY, 6, barH, 3, vColor);
        }

        if (maxScrollX > 0) {
            int hTrackX = x + GUTTER_WIDTH + 2;
            int hTrackY = y + height - 8;
            int hTrackW = width - GUTTER_WIDTH - 14;
            RenderUtils.drawRoundedRect(graphics, hTrackX, hTrackY, hTrackW, 6, 3, 0x1AFFFFFF);
            int barW = Math.max(24, (int) ((float) hTrackW * hTrackW / (hTrackW + maxScrollX)));
            int barX = hTrackX + (int) ((float) scrollX * (hTrackW - barW) / maxScrollX);
            boolean hHover = (mouseX >= barX && mouseX <= barX + barW && mouseY >= hTrackY && mouseY <= hTrackY + 6);
            int hColor = (draggingScrollX || hHover) ? 0x99A0AEC0 : 0x55A0AEC0;
            RenderUtils.drawRoundedRect(graphics, barX, hTrackY, barW, 6, 3, hColor);
        }

        if (hoveredError != null) {
            Component errComp = styled(hoveredError);
            int tipW = font.width(errComp) + 12;
            int tipH = 16;
            int tipX = Math.min(mouseX + 10, x + width - tipW);
            int tipY = Math.max(y, mouseY - 18);
            RenderUtils.drawRoundedRect(graphics, tipX, tipY, tipW, tipH, 4, 0xEE1E1E24);
            RenderUtils.drawRoundedOutline(graphics, tipX, tipY, tipW, tipH, 4, 1, 0xFFEF4444);
            graphics.drawString(font, errComp, tipX + 6, tipY + 4, 0xFFFEE2E2, false);
        }
    }

    private void renderSelection(GuiGraphics graphics, int lineIndex, int textX, int lineY, String lineText, Font font) {
        int selStartLine = Math.min(selectionAnchorLine, cursorLine);
        int selEndLine = Math.max(selectionAnchorLine, cursorLine);
        int selStartCol = selectionAnchorLine < cursorLine ? selectionAnchorCol : (selectionAnchorLine > cursorLine ? cursorCol : Math.min(selectionAnchorCol, cursorCol));
        int selEndCol = selectionAnchorLine < cursorLine ? cursorCol : (selectionAnchorLine > cursorLine ? selectionAnchorCol : Math.max(selectionAnchorCol, cursorCol));

        if (lineIndex < selStartLine || lineIndex > selEndLine) {
            return;
        }

        int col1 = lineIndex == selStartLine ? selStartCol : 0;
        int col2 = lineIndex == selEndLine ? selEndCol : lineText.length();

        col1 = Math.max(0, Math.min(col1, lineText.length()));
        col2 = Math.max(0, Math.min(col2, lineText.length()));

        if (col1 == col2 && lineIndex != selEndLine) {
            col2 = lineText.length();
        }

        int x1 = textX + font.width(styled(lineText.substring(0, col1)));
        int x2 = textX + font.width(styled(lineText.substring(0, col2)));
        if (x1 == x2) x2 += 4;

        RenderUtils.drawRect(graphics, x1, lineY, x2 - x1, LINE_HEIGHT, 0x443B82F6);
    }

    private void renderSyntaxHighlightedLine(GuiGraphics graphics, String line, int textX, int lineY, Font font) {
        int currentX = textX;
        int i = 0;
        int len = line.length();

        while (i < len) {
            char c = line.charAt(i);

            if (c == '/' && i + 1 < len && line.charAt(i + 1) == '/') {
                String comment = line.substring(i);
                Component comp = styled(comment);
                graphics.drawString(font, comp, currentX, lineY, 0xFF6272A4, false);
                break;
            }

            if (c == '"' || c == '\'') {
                int strStart = i++;
                while (i < len && line.charAt(i) != c) {
                    if (line.charAt(i) == '\\' && i + 1 < len) {
                        i++;
                    }
                    i++;
                }
                if (i < len) i++;
                String strToken = line.substring(strStart, i);
                Component comp = styled(strToken);
                graphics.drawString(font, comp, currentX, lineY, 0xFFF1FA8C, false);
                currentX += font.width(comp);
                continue;
            }

            if (Character.isDigit(c)) {
                int numStart = i;
                while (i < len && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '.' || line.charAt(i) == 'x' || line.charAt(i) == 'X' || (line.charAt(i) >= 'a' && line.charAt(i) <= 'f') || (line.charAt(i) >= 'A' && line.charAt(i) <= 'F'))) {
                    i++;
                }
                String numToken = line.substring(numStart, i);
                Component comp = styled(numToken);
                graphics.drawString(font, comp, currentX, lineY, 0xFFBD93F9, false);
                currentX += font.width(comp);
                continue;
            }

            if (Character.isJavaIdentifierStart(c)) {
                int idStart = i;
                while (i < len && Character.isJavaIdentifierPart(line.charAt(i))) {
                    i++;
                }
                String idToken = line.substring(idStart, i);
                int color = 0xFFF8F8F2;
                switch (idToken) {
                    case "let":
                    case "locate":
                    case "fn":
                    case "if":
                    case "else":
                    case "return":
                        color = 0xFFFF79C6;
                        break;
                    case "true":
                    case "false":
                    case "null":
                        color = 0xFFBD93F9;
                        break;
                    case "Params":
                    case "Variables":
                    case "Key":
                    case "Keys":
                    case "Target":
                    case "target":
                    case "Math":
                    case "Font":
                    case "Render":
                    case "Slot":
                    case "slot":
                        color = 0xFF8BE9FD;
                        break;
                }
                Component comp = styled(idToken);
                graphics.drawString(font, comp, currentX, lineY, color, false);
                currentX += font.width(comp);
                continue;
            }

            String chStr = String.valueOf(c);
            int color = (c == '{' || c == '}' || c == '(' || c == ')' || c == '[' || c == ']') ? 0xFFFFB86C : 0xFFCCCCCC;
            Component comp = styled(chStr);
            graphics.drawString(font, comp, currentX, lineY, color, false);
            currentX += font.width(comp);
            i++;
        }
    }

    public boolean keyPressed(KeyEvent event) {
        if (!focused) return false;

        int key = event.key();
        int modifiers = event.modifiers();
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        clampCursor();

        if (ctrl && key == GLFW.GLFW_KEY_A) {
            selectAll();
            return true;
        }
        if (ctrl && key == GLFW.GLFW_KEY_C) {
            copySelection();
            return true;
        }
        if (ctrl && key == GLFW.GLFW_KEY_V) {
            pasteSelection();
            return true;
        }
        if (ctrl && key == GLFW.GLFW_KEY_X) {
            copySelection();
            deleteSelection();
            notifyCodeChanged();
            return true;
        }

        if (key == GLFW.GLFW_KEY_UP) {
            moveCursor(cursorLine - 1, cursorCol, shift);
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            moveCursor(cursorLine + 1, cursorCol, shift);
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT) {
            if (cursorCol > 0) {
                moveCursor(cursorLine, cursorCol - 1, shift);
            } else if (cursorLine > 0) {
                moveCursor(cursorLine - 1, lines.get(cursorLine - 1).length(), shift);
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT) {
            if (cursorCol < getCurrentLine().length()) {
                moveCursor(cursorLine, cursorCol + 1, shift);
            } else if (cursorLine < lines.size() - 1) {
                moveCursor(cursorLine + 1, 0, shift);
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_HOME) {
            moveCursor(cursorLine, 0, shift);
            return true;
        }
        if (key == GLFW.GLFW_KEY_END) {
            moveCursor(cursorLine, getCurrentLine().length(), shift);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            deleteSelection();
            String current = getCurrentLine();
            int col = Math.min(cursorCol, current.length());
            String before = current.substring(0, col);
            String after = current.substring(col);
            lines.set(cursorLine, before);
            lines.add(cursorLine + 1, after);
            cursorLine++;
            cursorCol = 0;
            clearSelection();
            notifyCodeChanged();
            return true;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (hasSelection()) {
                deleteSelection();
            } else if (cursorCol > 0) {
                String cur = getCurrentLine();
                int col = Math.min(cursorCol, cur.length());
                lines.set(cursorLine, cur.substring(0, col - 1) + cur.substring(col));
                cursorCol = col - 1;
            } else if (cursorLine > 0) {
                String prev = lines.get(cursorLine - 1);
                String cur = getCurrentLine();
                cursorCol = prev.length();
                lines.set(cursorLine - 1, prev + cur);
                lines.remove(cursorLine);
                cursorLine--;
            }
            clearSelection();
            notifyCodeChanged();
            return true;
        }
        if (key == GLFW.GLFW_KEY_DELETE) {
            if (hasSelection()) {
                deleteSelection();
            } else {
                String cur = getCurrentLine();
                int col = Math.min(cursorCol, cur.length());
                if (col < cur.length()) {
                    lines.set(cursorLine, cur.substring(0, col) + cur.substring(col + 1));
                } else if (cursorLine < lines.size() - 1) {
                    String next = lines.get(cursorLine + 1);
                    lines.set(cursorLine, cur + next);
                    lines.remove(cursorLine + 1);
                }
            }
            clearSelection();
            notifyCodeChanged();
            return true;
        }
        if (key == GLFW.GLFW_KEY_TAB) {
            insertText("    ");
            return true;
        }

        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        if (!focused) return false;

        int codepoint = event.codepoint();
        if (Character.isBmpCodePoint(codepoint)) {
            char c = (char) codepoint;
            if (c >= 32 && c != 127) {
                insertText(String.valueOf(c));
                return true;
            }
        }
        return false;
    }

    private void insertText(String text) {
        if (text == null || text.isEmpty()) return;
        deleteSelection();

        text = text.replace("\r\n", "\n").replace('\r', '\n');

        if (!text.contains("\n")) {
            String cur = getCurrentLine();
            int col = Math.min(cursorCol, cur.length());
            String updated = cur.substring(0, col) + text + cur.substring(col);
            lines.set(cursorLine, updated);
            cursorCol = col + text.length();
        } else {
            String[] split = text.split("\n", -1);
            String cur = getCurrentLine();
            int col = Math.min(cursorCol, cur.length());
            String prefix = cur.substring(0, col);
            String suffix = cur.substring(col);

            lines.set(cursorLine, prefix + split[0]);
            for (int i = 1; i < split.length; i++) {
                cursorLine++;
                if (i == split.length - 1) {
                    lines.add(cursorLine, split[i] + suffix);
                    cursorCol = split[i].length();
                } else {
                    lines.add(cursorLine, split[i]);
                }
            }
        }

        clearSelection();
        notifyCodeChanged();
    }

    private void notifyCodeChanged() {
        if (element != null) {
            element.setCode(getText());
            element.compile();
        }
        lastBlink = System.currentTimeMillis();
        cursorVisible = true;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (mx < x || mx > x + width || my < y || my > y + height) {
            focused = false;
            return false;
        }

        focused = true;
        Font font = Minecraft.getInstance().font;
        int textAreaW = width - GUTTER_WIDTH - 12;
        int maxLineW = getMaxLineWidth(font);
        int maxScrollX = Math.max(0, maxLineW - textAreaW + 40);
        int visibleLines = (height - 14) / LINE_HEIGHT;
        int maxScrollY = Math.max(0, lines.size() - visibleLines + 1);

        if (maxScrollY > 0 && mx >= x + width - 10 && mx <= x + width) {
            int vTrackY = y + 2;
            int vTrackH = height - 14;
            int barH = Math.max(20, (int) ((float) vTrackH * visibleLines / lines.size()));
            int barY = vTrackY + (int) ((float) scrollY * (vTrackH - barH) / maxScrollY);
            if (my >= barY && my <= barY + barH) {
                draggingScrollY = true;
                dragStartMousePos = my;
                dragStartScroll = scrollY;
            } else if (my >= vTrackY && my <= vTrackY + vTrackH) {
                float ratio = (float) (my - vTrackY - barH / 2.0) / Math.max(1, vTrackH - barH);
                scrollY = (int) Math.max(0, Math.min(ratio * maxScrollY, maxScrollY));
                draggingScrollY = true;
                dragStartMousePos = my;
                dragStartScroll = scrollY;
            }
            return true;
        }

        if (maxScrollX > 0 && my >= y + height - 10 && my <= y + height) {
            int hTrackX = x + GUTTER_WIDTH + 2;
            int hTrackW = width - GUTTER_WIDTH - 14;
            int barW = Math.max(24, (int) ((float) hTrackW * hTrackW / (hTrackW + maxScrollX)));
            int barX = hTrackX + (int) ((float) scrollX * (hTrackW - barW) / maxScrollX);
            if (mx >= barX && mx <= barX + barW) {
                draggingScrollX = true;
                dragStartMousePos = mx;
                dragStartScroll = scrollX;
            } else if (mx >= hTrackX && mx <= hTrackX + hTrackW) {
                float ratio = (float) (mx - hTrackX - barW / 2.0) / Math.max(1, hTrackW - barW);
                scrollX = (int) Math.max(0, Math.min(ratio * maxScrollX, maxScrollX));
                draggingScrollX = true;
                dragStartMousePos = mx;
                dragStartScroll = scrollX;
            }
            return true;
        }

        int clickedLine = scrollY + (int) ((my - y - 4) / LINE_HEIGHT);
        clickedLine = Math.max(0, Math.min(clickedLine, lines.size() - 1));

        String line = lines.get(clickedLine);
        int textStartX = x + GUTTER_WIDTH + 8 - scrollX;
        int clickedCol = 0;
        int accumX = textStartX;

        for (int i = 0; i < line.length(); i++) {
            int charW = font.width(styled(String.valueOf(line.charAt(i))));
            if (mx >= accumX + charW / 2) {
                clickedCol = i + 1;
            }
            accumX += charW;
        }

        cursorLine = clickedLine;
        cursorCol = clickedCol;
        clearSelection();
        lastBlink = System.currentTimeMillis();
        cursorVisible = true;
        return true;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mx = event.x();
        double my = event.y();

        Font font = Minecraft.getInstance().font;
        int textAreaW = width - GUTTER_WIDTH - 12;
        int maxLineW = getMaxLineWidth(font);
        int maxScrollX = Math.max(0, maxLineW - textAreaW + 40);
        int visibleLines = (height - 14) / LINE_HEIGHT;
        int maxScrollY = Math.max(0, lines.size() - visibleLines + 1);

        if (draggingScrollY) {
            int vTrackH = height - 14;
            int barH = Math.max(20, (int) ((float) vTrackH * visibleLines / lines.size()));
            int scrollable = vTrackH - barH;
            if (scrollable > 0) {
                double delta = my - dragStartMousePos;
                int newScroll = (int) (dragStartScroll + (delta / scrollable) * maxScrollY);
                scrollY = Math.max(0, Math.min(newScroll, maxScrollY));
            }
            return true;
        }

        if (draggingScrollX) {
            int hTrackW = width - GUTTER_WIDTH - 14;
            int barW = Math.max(24, (int) ((float) hTrackW * hTrackW / (hTrackW + maxScrollX)));
            int scrollable = hTrackW - barW;
            if (scrollable > 0) {
                double delta = mx - dragStartMousePos;
                int newScroll = (int) (dragStartScroll + (delta / scrollable) * maxScrollX);
                scrollX = Math.max(0, Math.min(newScroll, maxScrollX));
            }
            return true;
        }

        if (mx < x || mx > x + width || my < y || my > y + height) {
            return false;
        }

        int dragLine = scrollY + (int) ((my - y - 4) / LINE_HEIGHT);
        dragLine = Math.max(0, Math.min(dragLine, lines.size() - 1));

        String line = lines.get(dragLine);
        int textStartX = x + GUTTER_WIDTH + 8 - scrollX;
        int dragCol = 0;
        int accumX = textStartX;

        for (int i = 0; i < line.length(); i++) {
            int charW = font.width(styled(String.valueOf(line.charAt(i))));
            if (mx >= accumX + charW / 2) {
                dragCol = i + 1;
            }
            accumX += charW;
        }

        if (!hasSelection() && selectionAnchorLine == -1) {
            selectionAnchorLine = cursorLine;
            selectionAnchorCol = cursorCol;
        }

        cursorLine = dragLine;
        cursorCol = dragCol;
        return true;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollY = false;
        draggingScrollX = false;
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            Font font = Minecraft.getInstance().font;
            int maxLineW = getMaxLineWidth(font);
            int textAreaW = width - GUTTER_WIDTH - 12;
            int maxScrollX = Math.max(0, maxLineW - textAreaW + 40);

            int visibleLines = (height - 14) / LINE_HEIGHT;
            int maxScrollY = Math.max(0, lines.size() - visibleLines + 1);

            long handle = Minecraft.getInstance().getWindow().handle();
            boolean isShift = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (isShift || horizontalAmount != 0) {
                double hAmt = horizontalAmount != 0 ? horizontalAmount : -verticalAmount;
                scrollX = (int) Math.max(0, Math.min(scrollX + hAmt * 20, maxScrollX));
            } else {
                scrollY = (int) Math.max(0, Math.min(scrollY - verticalAmount * 3, maxScrollY));
            }
            return true;
        }
        return false;
    }

    private void moveCursor(int newLine, int newCol, boolean shift) {
        if (shift) {
            if (!hasSelection()) {
                selectionAnchorLine = cursorLine;
                selectionAnchorCol = cursorCol;
            }
        } else {
            clearSelection();
        }
        cursorLine = Math.max(0, Math.min(newLine, lines.size() - 1));
        cursorCol = Math.max(0, Math.min(newCol, getCurrentLine().length()));
        lastBlink = System.currentTimeMillis();
        cursorVisible = true;
    }

    private boolean hasSelection() {
        return selectionAnchorLine != -1 && (selectionAnchorLine != cursorLine || selectionAnchorCol != cursorCol);
    }

    private void clearSelection() {
        selectionAnchorLine = -1;
        selectionAnchorCol = -1;
    }

    private void selectAll() {
        selectionAnchorLine = 0;
        selectionAnchorCol = 0;
        cursorLine = lines.size() - 1;
        cursorCol = lines.get(cursorLine).length();
    }

    private void copySelection() {
        if (!hasSelection()) return;
        String sel = getSelectedText();
        if (!sel.isEmpty()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(sel);
        }
    }

    private void pasteSelection() {
        String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
        if (clip != null && !clip.isEmpty()) {
            insertText(clip);
        }
    }

    private String getSelectedText() {
        if (!hasSelection()) return "";
        int selStartLine = Math.min(selectionAnchorLine, cursorLine);
        int selEndLine = Math.max(selectionAnchorLine, cursorLine);
        int selStartCol = selectionAnchorLine < cursorLine ? selectionAnchorCol : (selectionAnchorLine > cursorLine ? cursorCol : Math.min(selectionAnchorCol, cursorCol));
        int selEndCol = selectionAnchorLine < cursorLine ? cursorCol : (selectionAnchorLine > cursorLine ? selectionAnchorCol : Math.max(selectionAnchorCol, cursorCol));

        selStartLine = Math.max(0, Math.min(selStartLine, lines.size() - 1));
        selEndLine = Math.max(0, Math.min(selEndLine, lines.size() - 1));

        String startLine = lines.get(selStartLine);
        String endLine = lines.get(selEndLine);
        selStartCol = Math.max(0, Math.min(selStartCol, startLine.length()));
        selEndCol = Math.max(0, Math.min(selEndCol, endLine.length()));

        if (selStartLine == selEndLine) {
            return startLine.substring(selStartCol, selEndCol);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(startLine.substring(selStartCol)).append("\n");
        for (int i = selStartLine + 1; i < selEndLine; i++) {
            sb.append(lines.get(i)).append("\n");
        }
        sb.append(endLine.substring(0, selEndCol));
        return sb.toString();
    }

    private void deleteSelection() {
        if (!hasSelection()) return;
        int selStartLine = Math.min(selectionAnchorLine, cursorLine);
        int selEndLine = Math.max(selectionAnchorLine, cursorLine);
        int selStartCol = selectionAnchorLine < cursorLine ? selectionAnchorCol : (selectionAnchorLine > cursorLine ? cursorCol : Math.min(selectionAnchorCol, cursorCol));
        int selEndCol = selectionAnchorLine < cursorLine ? cursorCol : (selectionAnchorLine > cursorLine ? selectionAnchorCol : Math.max(selectionAnchorCol, cursorCol));

        selStartLine = Math.max(0, Math.min(selStartLine, lines.size() - 1));
        selEndLine = Math.max(0, Math.min(selEndLine, lines.size() - 1));

        String startLineStr = lines.get(selStartLine);
        String endLineStr = lines.get(selEndLine);

        selStartCol = Math.max(0, Math.min(selStartCol, startLineStr.length()));
        selEndCol = Math.max(0, Math.min(selEndCol, endLineStr.length()));

        String merged = startLineStr.substring(0, selStartCol) + endLineStr.substring(selEndCol);

        for (int i = selEndLine; i > selStartLine; i--) {
            lines.remove(i);
        }
        lines.set(selStartLine, merged);

        cursorLine = selStartLine;
        cursorCol = selStartCol;
        clearSelection();
    }
}
