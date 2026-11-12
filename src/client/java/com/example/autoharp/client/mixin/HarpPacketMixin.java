package com.example.autoharp.client.mixin;

import com.example.autoharp.client.AutoHarpClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class HarpPacketMixin {
	// RETURN is after vanilla's thread check and inventory update, not on the Netty thread.
	@Inject(method = "handleContainerSetSlot", at = @At("RETURN"))
	private void autoHarp$slotUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
		AutoHarpClient.onServerSlot(packet);
	}

	@Inject(method = "handleContainerContent", at = @At("RETURN"))
	private void autoHarp$contentUpdate(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
		AutoHarpClient.onServerContent(packet);
	}
}
