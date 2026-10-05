package org.tovasha.ych.gui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.tovasha.ych.YourCustomHud;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.api.HudRegistry;
import org.tovasha.ych.render.RenderUtils;
import org.tovasha.ych.storage.StorageManager;

public class HudEditorScreen extends Screen {
    private HudPreviewWidget previewWidget;
    private PresetBarWidget presetBar;
    private InspectorWidget inspectorWidget;
    private ElementTabsWidget elementTabs;
    private CodeEditorWidget codeEditor;

    public HudEditorScreen() {
        super(Component.translatable("yourcustomhud.editor.title"));
    }

    @Override
    protected void init() {
        int leftWidth = (int) (width * 0.65);
        int rightWidth = width - leftWidth;
        int topHeight = (int) (height * 0.65);
        int barH = 26;
        int inspectorH = height - topHeight - barH;
        int codeEditorH = height - barH;

        previewWidget = new HudPreviewWidget(0, 0, leftWidth, topHeight);
        presetBar = new PresetBarWidget(0, topHeight, leftWidth, barH);
        inspectorWidget = new InspectorWidget(0, topHeight + barH, leftWidth, inspectorH);
        elementTabs = new ElementTabsWidget(leftWidth, 0, rightWidth, barH);
        codeEditor = new CodeEditorWidget(leftWidth, barH, rightWidth, codeEditorH);

        presetBar.setActivePreset(YourCustomHud.CONFIG.getActivePreset());
        presetBar.refreshPresets();

        if (HudRegistry.getElements().isEmpty()) {
            StorageManager.loadPreset(presetBar.getActivePreset());
        }

        List<HudElement> elements = HudRegistry.getElements();
        HudElement first = elements.isEmpty() ? null : elements.get(0);
        selectElement(first);

        presetBar.setOnPresetChanged(presetName -> {
            YourCustomHud.CONFIG.setActivePreset(presetName);
            YourCustomHud.saveConfig();
            StorageManager.loadPreset(presetName);
            List<HudElement> els = HudRegistry.getElements();
            selectElement(els.isEmpty() ? null : els.get(0));
        });


        elementTabs.setOnElementChanged(this::selectElement);
        elementTabs.setOnPresetModified(this::bakePreset);

        previewWidget.setOnElementSelected(this::selectElement);
        previewWidget.setOnElementMoved(this::bakePreset);

        inspectorWidget.setOnParamChanged(this::bakePreset);
    }

    private void selectElement(HudElement element) {
        elementTabs.setSelectedElement(element);
        codeEditor.setElement(element);
        inspectorWidget.setElement(element);
        previewWidget.setSelectedElement(element);
    }

    private void bakePreset() {
        if (presetBar != null) {
            StorageManager.savePreset(presetBar.getActivePreset());
        }
        if (codeEditor != null && codeEditor.getElement() != null) {
            codeEditor.getElement().compile();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int leftWidth = (int) (width * 0.65);

        previewWidget.render(graphics, mouseX, mouseY, partialTick);
        presetBar.render(graphics, mouseX, mouseY);
        inspectorWidget.render(graphics, mouseX, mouseY);
        elementTabs.render(graphics, mouseX, mouseY);
        codeEditor.render(graphics, mouseX, mouseY);

        RenderUtils.drawRect(graphics, leftWidth, 0, 1, height, Theme.getBorder());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        if (presetBar != null && presetBar.isInputFocused()) {
            return presetBar.keyPressed(event);
        }
        if (elementTabs != null && elementTabs.isInputFocused()) {
            return elementTabs.keyPressed(event);
        }
        if (codeEditor != null) {
            return codeEditor.keyPressed(event);
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (presetBar != null && presetBar.isInputFocused()) {
            return presetBar.charTyped(event);
        }
        if (elementTabs != null && elementTabs.isInputFocused()) {
            return elementTabs.charTyped(event);
        }
        if (codeEditor != null) {
            return codeEditor.charTyped(event);
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int leftWidth = (int) (width * 0.65);

        if (presetBar != null && !isInside(mx, my, presetBar.getX(), presetBar.getY(), presetBar.getWidth(), presetBar.getHeight())) {
            presetBar.setInputFocused(false);
        }
        if (elementTabs != null && !isInside(mx, my, elementTabs.getX(), elementTabs.getY(), elementTabs.getWidth(), elementTabs.getHeight())) {
            elementTabs.setInputFocused(false);
        }

        if (previewWidget != null && previewWidget.mouseClicked(event, doubleClick)) {
            if (codeEditor != null) codeEditor.setFocused(false);
            return true;
        }
        if (presetBar != null && presetBar.mouseClicked(event, doubleClick)) {
            if (codeEditor != null) codeEditor.setFocused(false);
            return true;
        }
        if (inspectorWidget != null && inspectorWidget.mouseClicked(event, doubleClick)) {
            if (codeEditor != null) codeEditor.setFocused(false);
            return true;
        }
        if (elementTabs != null && elementTabs.mouseClicked(event, doubleClick)) {
            if (codeEditor != null) codeEditor.setFocused(false);
            return true;
        }
        if (codeEditor != null && codeEditor.mouseClicked(event, doubleClick)) {
            codeEditor.setFocused(true);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (elementTabs != null) {
            elementTabs.mouseReleased(event);
        }
        if (previewWidget != null) {
            previewWidget.mouseReleased(event);
        }
        if (codeEditor != null) {
            codeEditor.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (elementTabs != null && elementTabs.mouseDragged(event, dragX, dragY)) {
            return true;
        }
        if (previewWidget != null && previewWidget.mouseDragged(event, dragX, dragY)) {
            return true;
        }
        if (codeEditor != null && codeEditor.mouseDragged(event, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (inspectorWidget != null && inspectorWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (codeEditor != null && codeEditor.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (paths == null || paths.isEmpty()) return;
        String currentPreset = presetBar != null ? presetBar.getActivePreset() : YourCustomHud.CONFIG.getActivePreset();
        HudElement lastImported = null;

        for (Path path : paths) {
            File file = path.toFile();
            if (!file.exists()) continue;

            String fileName = file.getName();
            if (fileName.endsWith(".svhe")) {
                HudElement el = StorageManager.importElement(file, currentPreset);
                if (el != null) {
                    HudRegistry.register(el);
                    lastImported = el;
                }
            } else if (fileName.endsWith(".svhud") || fileName.endsWith(".zip")) {
                String presetName = fileName.replace(".svhud", "").replace(".zip", "");
                File targetDir = new File(StorageManager.getPresetsDir(), presetName);
                StorageManager.importZip(file, targetDir);
                if (presetBar != null) {
                    presetBar.refreshPresets();
                    presetBar.setActivePreset(presetName);
                }
                YourCustomHud.CONFIG.setActivePreset(presetName);
                YourCustomHud.saveConfig();
                StorageManager.loadPreset(presetName);
                List<HudElement> els = HudRegistry.getElements();
                selectElement(els.isEmpty() ? null : els.get(0));
                return;
            }
        }

        if (lastImported != null) {
            bakePreset();
            selectElement(lastImported);
        }
    }

    @Override
    public void onClose() {
        bakePreset();
        if (presetBar != null) {
            YourCustomHud.CONFIG.setActivePreset(presetBar.getActivePreset());
            YourCustomHud.saveConfig();
        }
        if (minecraft != null) {
            minecraft.gui.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
