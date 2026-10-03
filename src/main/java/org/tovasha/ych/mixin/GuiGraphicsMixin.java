package org.tovasha.ych.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tovasha.ych.render.GuiGraphicsBridge;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements GuiGraphicsBridge {
    @Shadow @Final
    GuiRenderState guiRenderState;

    @Unique
    private final Deque<ScreenRectangle> ych$scissorStack = new ArrayDeque<>();

    @Inject(method = "enableScissor", at = @At("HEAD"))
    private void ych$onEnableScissor(int x1, int y1, int x2, int y2, CallbackInfo ci) {
        ScreenRectangle rect = new ScreenRectangle(x1, y1, x2 - x1, y2 - y1);
        ScreenRectangle current = ych$scissorStack.peek();
        if (current != null) {
            rect = current.intersection(rect);
        }
        ych$scissorStack.push(rect != null ? rect : new ScreenRectangle(0, 0, 0, 0));
    }

    @Inject(method = "disableScissor", at = @At("HEAD"))
    private void ych$onDisableScissor(CallbackInfo ci) {
        if (!ych$scissorStack.isEmpty()) {
            ych$scissorStack.pop();
        }
    }

    @Override
    public GuiRenderState ych$getGuiRenderState() {
        return this.guiRenderState;
    }

    @Override
    public ScreenRectangle ych$getCurrentScissor() {
        return ych$scissorStack.peek();
    }
}
