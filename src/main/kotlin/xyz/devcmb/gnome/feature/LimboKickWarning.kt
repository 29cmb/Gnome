package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.util.isOnIsland
import java.util.Optional
import kotlin.reflect.KMutableProperty0

class LimboKickWarning : GnomeFeature {
    override val id: String = "limbo_kick_warning"
    override val name: String = "Limbo Kick Warning"
    override val description: OptionDescription = OptionDescription.of(
        Component.literal("Plays a loud sound when you're about to be kicked for AFK")
    )
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::limboKickWarningEnabled

    override fun init() {
        ClientReceiveMessageEvents.GAME.register { component, _ ->
            if(!isOnIsland() || !Config.values.limboKickWarningEnabled) return@register
            if(component.string.contains("Warning! You are about to be kicked for being AFK in 10 seconds.", ignoreCase = true)) {
                Minecraft.getInstance().level?.playPlayerSound(
                    SoundEvent(Identifier.fromNamespaceAndPath("mcc", "games.global.objective.map.alert"), Optional.empty()),
                    SoundSource.UI, 1.2f, 1f
                )
            }
        }
    }

}