package xyz.devcmb.gnome

import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import dev.isxander.yacl3.dsl.binding
import dev.isxander.yacl3.dsl.tickBox
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier

class Config {
    @SerialEntry
    var dayNightDetectionEnabled: Boolean = true

    @SerialEntry
    var limboKickWarningEnabled: Boolean = true

    @SerialEntry
    var sessionStatsEnabled: Boolean = true

    companion object {
        val handler: ConfigClassHandler<Config> = ConfigClassHandler.createBuilder(Config::class.java)
            .id(Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "config"))
            .serializer { config -> GsonConfigSerializerBuilder.create(config)
                .setPath(FabricLoader.getInstance().configDir.resolve("gnome.json"))
                .build()
            }
            .build()

        val values: Config
            get() {
                return handler.instance()
            }

        fun getScreen(parent: Screen): Screen = YetAnotherConfigLib("gnome") {
            title(Component.literal("Gnome"))
            save {
                handler.save()
            }

            categories.register("settings") {
                name(Component.literal("Settings"))

                rootOptions.register("day_night_detection_enabled") {
                    name(Component.literal("Day/Night Detection"))
                    description(OptionDescription.of(
                        Component.literal("Sends a message in chat and plays a sound when it becomes either night or day"),
                        Component.literal("Plays a ")
                            .append(Component.literal("Ponder").withStyle(Style.EMPTY.withBold(true)))
                            .append(Component.literal(" goat horn when it becomes "))
                            .append(Component.literal("day").withStyle(Style.EMPTY.withBold(true))),
                        Component.literal("Plays a ")
                            .append(Component.literal("Sing").withStyle(Style.EMPTY.withBold(true)))
                            .append(Component.literal(" goat horn when it becomes "))
                            .append(Component.literal("night").withStyle(Style.EMPTY.withBold(true)))
                    ))
                    binding(values::dayNightDetectionEnabled, true)
                    controller(tickBox())
                }

                rootOptions.register("limbo_kick_warning_enabled") {
                    name(Component.literal("Limbo Kick Warning"))
                    description(OptionDescription.of(
                        Component.literal("Plays a loud sound when you're about to be kicked for AFK")
                    ))
                    binding(values::limboKickWarningEnabled, true)
                    controller(tickBox())
                }

                rootOptions.register("session_stats_enabled") {
                    name(Component.literal("Session Statistics"))
                    description(OptionDescription.of(
                        Component.literal("Displays treasure, pearls, spirits, fish, and xp caught since you booted up the game")
                    ))
                    binding(values::sessionStatsEnabled, true)
                    controller(tickBox())
                }
            }
        }.generateScreen(parent)
    }
}