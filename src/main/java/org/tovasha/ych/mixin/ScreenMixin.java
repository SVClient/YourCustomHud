package org.tovasha.ych.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tovasha.ych.YourCustomHud;
import org.tovasha.ych.api.HudRegistry;
import org.tovasha.ych.event.RenderEvent;
import org.tovasha.ych.gui.HudEditorScreen;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (YourCustomHud.CONFIG != null && YourCustomHud.CONFIG.isEnabled() && YourCustomHud.CONFIG.isRenderAboveAll()) {
            if (!((Object) this instanceof HudEditorScreen)) {
                YourCustomHud.EVENT_BUS.post(new RenderEvent(guiGraphics, null, partialTick));
                HudRegistry.renderAll(guiGraphics, partialTick);
            }
        }
    }
}
