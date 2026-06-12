package xyz.devcmb.gnome.feature

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import xyz.devcmb.gnome.isOnIsland
import xyz.devcmb.gnome.mixin.BossEventAccessor
import xyz.devcmb.gnome.sendMessage
import xyz.devcmb.gnome.withFont

class DayNightDetection : GnomeFeature {
    var currentTime: Time? = null
    val dayGlyph: String = "\uE0EB"
    val nightGlyph: String = "\uE0E9"

    override fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if(!isOnIsland()) return@register

            val time = getCurrentTime(client)

            if(time == Time.DAY && currentTime != Time.DAY) {
                if(currentTime != null) {
                    client.sendMessage(
                        Component.empty().append(
                            Component.literal("(")
                            .append(
                                Component.literal("\uE000")
                                    .withFont(Identifier.fromNamespaceAndPath("gnome", "main"))
                                    .withColor(0xFFFFFF)
                            )
                            .append(Component.literal(") It is now day time!"))
                        ).withColor(0xf3ff60)
                    )

                    client.level?.playPlayerSound(SoundEvents.GOAT_HORN_SOUND_VARIANTS[0].value(), SoundSource.UI, 1f, 1f)
                }

                currentTime = Time.DAY
            } else if(time == Time.NIGHT && currentTime != Time.NIGHT) {
                if(currentTime != null) {
                    client.sendMessage(
                        Component.empty().append(
                            Component.literal("(")
                                .append(
                                    Component.literal("\uE001")
                                        .withFont(Identifier.fromNamespaceAndPath("gnome", "main"))
                                        .withColor(0xFFFFFF)
                                )
                                .append(Component.literal(") It is now night time!"))
                        ).withColor(0xf3ff60)
                    )

                    client.level?.playPlayerSound(SoundEvents.GOAT_HORN_SOUND_VARIANTS[1].value(), SoundSource.UI, 1f, 1f)
                }

                currentTime = Time.NIGHT
            }
        }
    }

    override fun cleanup() {
    }

    fun getCurrentTime(client: Minecraft): Time? {
        val timeBossBar = client.gui.bossOverlay as BossEventAccessor
        val events = HashMap(timeBossBar.`gnome$getEvents`())

        val fishingBar = events.toList().firstOrNull {
            val name = it.second.name.string
            name.contains(dayGlyph) || name.contains(nightGlyph)
        } ?: return null

        val name = fishingBar.second.name.string

        return if(name.contains(dayGlyph)) Time.DAY else Time.NIGHT
    }

    enum class Time {
        DAY,
        NIGHT
    }
}