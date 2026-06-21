package xyz.devcmb.gnome.mixin.modify;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.devcmb.gnome.Gnome;
import xyz.devcmb.gnome.feature.IslandFishTracker;
import xyz.devcmb.gnome.feature.SessionStats;
import xyz.devcmb.gnome.util.UtilKt;

@Mixin(ClientPacketListener.class)
public class ClientPacketMixin {
    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void handleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if(!UtilKt.isOnIsland() || !UtilKt.isOnFishing() || !Minecraft.getInstance().isSameThread()) return;

        SessionStats sessionStats = Gnome.INSTANCE.getFeature(SessionStats.class);
        sessionStats.onChatMessage(packet.content());
    }

    @Inject(method = "handleContainerContent", at = @At("TAIL"))
    private void handleContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if(!UtilKt.isOnIsland() || !UtilKt.isOnFishing() || !Minecraft.getInstance().isSameThread()) return;

        Minecraft mc = Minecraft.getInstance();
        Screen currentScreen = mc.screen;

        if(!(currentScreen instanceof AbstractContainerScreen<?> screen)) return;

        if (!screen.getTitle().getString().contains("FISHING PROGRESS"))
            return;

        Gnome.INSTANCE
                .getFeature(IslandFishTracker.class)
                .updateIslandCompletionState(packet.items());
    }
}
