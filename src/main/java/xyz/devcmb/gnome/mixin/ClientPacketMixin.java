package xyz.devcmb.gnome.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.devcmb.gnome.Gnome;
import xyz.devcmb.gnome.UtilKt;
import xyz.devcmb.gnome.feature.SessionStats;

@Mixin(ClientPacketListener.class)
public class ClientPacketMixin {
    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void handleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if(!UtilKt.isOnIsland() || !UtilKt.isOnFishing() || !Minecraft.getInstance().isSameThread()) return;

        SessionStats sessionStats = Gnome.INSTANCE.getFeature(SessionStats.class);
        sessionStats.onChatMessage(packet.content());
    }
}
