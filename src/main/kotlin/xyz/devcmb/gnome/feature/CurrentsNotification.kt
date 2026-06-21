package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.util.Font
import xyz.devcmb.gnome.util.isOnFishing
import xyz.devcmb.gnome.util.isOnIsland
import xyz.devcmb.gnome.util.sendMessage
import xyz.devcmb.gnome.util.withBold
import java.time.Instant
import java.time.LocalDateTime
import kotlin.reflect.KMutableProperty0

class CurrentsNotification: GnomeFeature {
    override val id: String = "currents_notification"
    override val name: String = "Current Notification"
    override val description: OptionDescription = OptionDescription.of(
        Component.literal("Gives a chat message and sound when currents change at the top of every hour"),
        Component.empty(),
        Component.literal("Plays a ").append(Component.literal("Seek").withBold(true)).append(Component.literal(" goat horn."))
    )
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::currentsNotificationEnabled

    var lastNotification: Long = 0

    override fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if(!isOnIsland() || !isOnFishing()) {
                lastNotification = 0L
                return@register
            }

            val currentUnix = Instant.now().epochSecond
            val currentMinute = LocalDateTime.now().minute

            if(currentMinute == 0 && currentUnix > lastNotification + 120) {
                lastNotification = currentUnix

                client.sendMessage(Component.empty().append(
                    Component.literal("(")
                        .append(Font.getGlyph(
                            "_fonts/icon/fishing/wayfinder_data.png"
                        ).withoutShadow().withColor(0xFFFFFF))
                        .append(Component.literal(") The currents have changed!"))
                ).withColor(0x59ff58))
                client.level?.playPlayerSound(SoundEvents.GOAT_HORN_SOUND_VARIANTS[2].value(), SoundSource.UI, 1f, 1f)
            }
        }
    }
}