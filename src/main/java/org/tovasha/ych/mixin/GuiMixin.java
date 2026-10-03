package org.tovasha.ych.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tovasha.ych.YourCustomHud;
import org.tovasha.ych.api.HudRegistry;
import org.tovasha.ych.event.RenderEvent;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (YourCustomHud.CONFIG != null && YourCustomHud.CONFIG.isEnabled()) {
            Minecraft mc = Minecraft.getInstance();
            if (!YourCustomHud.CONFIG.isRenderAboveAll() || mc.screen == null) {
                float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
                YourCustomHud.EVENT_BUS.post(new RenderEvent(guiGraphics, deltaTracker, partialTick));
                HudRegistry.renderAll(guiGraphics, partialTick);
            }
        }
    }
}
