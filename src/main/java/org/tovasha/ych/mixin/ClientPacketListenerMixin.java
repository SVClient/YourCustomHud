package org.tovasha.ych.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tovasha.ych.render.TpsTracker;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleSetTime", at = @At("HEAD"))
    private void ych$onHandleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        TpsTracker.onTimePacket(packet.gameTime());
    }
}
