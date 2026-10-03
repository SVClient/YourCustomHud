package org.tovasha.ych.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tovasha.ych.render.CpsTracker;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void onMouseButton(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS && button != null) {
            CpsTracker.onClick(button.button());
        }
    }
}
