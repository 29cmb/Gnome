package xyz.devcmb.gnome.feature

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import java.util.Optional

class LimboKickWarning : GnomeFeature {
    override fun init() {
        ClientReceiveMessageEvents.CHAT.register { component, _, _, _, _ ->
            if(component.string.contains("Warning! You are about to be kicked for being AFK in 10 seconds.", ignoreCase = true)) {
                Minecraft.getInstance().level?.playPlayerSound(
                    SoundEvent(Identifier.fromNamespaceAndPath("mcc", "games.global.objective.map.alert"), Optional.empty()),
                    SoundSource.UI, 1.2f, 1f
                )
            }
        }
    }

    override fun cleanup() {
    }
}