package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.isOnFishing
import xyz.devcmb.gnome.isOnIsland
import xyz.devcmb.gnome.mixin.BossEventAccessor
import xyz.devcmb.gnome.sendMessage
import xyz.devcmb.gnome.withFont
import kotlin.reflect.KMutableProperty0

class DayNightDetection : GnomeFeature {
    override val id: String = "day_night_detection"
    override val name: String = "Day/Night Detection"
    override val description: OptionDescription = OptionDescription.of(
        Component.literal("Sends a message in chat and plays a sound when it becomes either night or day"),
        Component.empty(),
        Component.literal("Plays a ")
            .append(Component.literal("Ponder").withStyle(Style.EMPTY.withBold(true)))
            .append(Component.literal(" goat horn when it becomes "))
            .append(Component.literal("day").withStyle(Style.EMPTY.withBold(true))),
        Component.literal("Plays a ")
            .append(Component.literal("Sing").withStyle(Style.EMPTY.withBold(true)))
            .append(Component.literal(" goat horn when it becomes "))
            .append(Component.literal("night").withStyle(Style.EMPTY.withBold(true)))
    )
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::dayNightDetectionEnabled

    var currentTime: Time? = null
    val dayGlyph: String = "\uE0EB"
    val nightGlyph: String = "\uE0E9"

    override fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if(!isOnIsland() || !isOnFishing() || !Config.values.dayNightDetectionEnabled) return@register

            val time = getCurrentTime(client)
            if(time != null && currentTime != null && time != currentTime && time.shouldLog.get()) {
                client.sendMessage(Component.empty().append(
                    Component.literal("(")
                        .append(
                            Component.literal(time.clock)
                                .withFont(Identifier.fromNamespaceAndPath("gnome", "main"))
                                .withColor(0xFFFFFF)
                        )
                        .append(Component.literal(") It is now ${time.time} time!"))
                ).withColor(0xf3ff60))
                client.level?.playPlayerSound(time.sound, SoundSource.UI, 1f, 1f)
            }

            currentTime = time
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

    enum class Time(
        val clock: String,
        val time: String,
        val sound: SoundEvent,
        val shouldLog: KMutableProperty0<Boolean>
    ) {
        DAY("\uE000", "day", SoundEvents.GOAT_HORN_SOUND_VARIANTS[0].value(), Config.values::logDayTime),
        NIGHT("\uE001", "night", SoundEvents.GOAT_HORN_SOUND_VARIANTS[1].value(), Config.values::logNightTime),
    }
}