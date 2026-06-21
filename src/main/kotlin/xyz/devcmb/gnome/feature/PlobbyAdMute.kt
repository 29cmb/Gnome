package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component
import xyz.devcmb.gnome.config.Config
import kotlin.reflect.KMutableProperty0

class PlobbyAdMute : GnomeFeature {
    override val id: String = "plobby_ad_mute"
    override val name: String = "Plobby Ad Mute"
    override val description: OptionDescription = OptionDescription.of(Component.literal("Hides private lobby advertisements"))
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::plobbyAdMuteEnabled

    override fun init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { component, _ ->
            !(Config.values.plobbyAdMuteEnabled && component.string.contains("Plobby Advert"))
        }
    }

}