package org.tovasha.ych.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tovasha.ych.YourCustomHud;
import org.tovasha.ych.api.HudRegistry;
import org.tovasha.ych.event.InputEvent;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        YourCustomHud.EVENT_BUS.post(new InputEvent(event.key(), event.scancode(), action, event.modifiers()));
        HudRegistry.keyPressedAll(event.key(), action);
    }
}
