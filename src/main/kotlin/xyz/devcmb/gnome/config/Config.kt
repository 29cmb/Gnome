package xyz.devcmb.gnome.config

import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import dev.isxander.yacl3.dsl.binding
import dev.isxander.yacl3.dsl.enumDropdown
import dev.isxander.yacl3.dsl.tickBox
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import xyz.devcmb.gnome.Gnome
import xyz.devcmb.gnome.data.Island
import xyz.devcmb.gnome.feature.SessionStats
import xyz.devcmb.gnome.util.withBold

class Config {
    // Day/Night Detection
    @SerialEntry
    var dayNightDetectionEnabled: Boolean = true
    @SerialEntry
    var logDayTime: Boolean = true
    @SerialEntry
    var logNightTime: Boolean = true

    // Limbo Kick
    @SerialEntry
    var limboKickWarningEnabled: Boolean = true

    // Session Stats
    @SerialEntry
    var sessionStatsEnabled: Boolean = true
    @SerialEntry
    var sessionStatsTrackingMode: SessionStats.StatTrackingMode = SessionStats.StatTrackingMode.AMOUNTS
    @SerialEntry
    var sessionStatsPearlTrackingMode: SessionStats.PearlTrackingMode = SessionStats.PearlTrackingMode.CATCHES

    // Plobby Ad Mute
    @SerialEntry
    var plobbyAdMuteEnabled: Boolean = false

    // Currents Notification
    @SerialEntry
    var currentsNotificationEnabled: Boolean = true

    // Island Completion
    @SerialEntry
    var islandCompletionEnabled: Boolean = true

    // Island Fishing Tracker
    @SerialEntry
    var islandFishTrackerEnabled: Boolean = true

    @SerialEntry
    var state: State = State()

    class State {
        @SerialEntry
        var islandProgress: HashMap<Island, CompletionData> = HashMap(
            Island.entries.associateWith { CompletionData(-1, -1, -1, -1) }
        )
    }

    data class CompletionData(
        val average: Int,
        val large: Int,
        val massive: Int,
        val gargantuan: Int
    )

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

        val state: State
            get() {
                return values.state
            }

        fun getScreen(parent: Screen): Screen = YetAnotherConfigLib("gnome") {
            title(Component.literal("Gnome"))
            save {
                handler.save()
            }

            categories.register("settings") {
                name(Component.literal("Settings"))

                groups.register("features") {
                    name(Component.literal("Features"))
                    tooltip(Component.literal("Enable or disable certain features"))

                    Gnome.features.forEach { feature ->
                        options.register(feature.id) {
                            name(Component.literal(feature.name))
                            description(feature.description)
                            controller(tickBox())
                            binding(feature.enabledProperty, true)
                        }
                    }
                }

                groups.register("day_night_detection") {
                    name(Component.literal("Day/Night Detection"))
                    tooltip(Component.literal("Configuration for the Day/Night Detection feature"))

                    options.register("detect_day") {
                        name(Component.literal("Detect Day"))
                        description(
                            OptionDescription.of(
                                Component.literal("Decides if there should be a notification for when it turns day")
                            )
                        )
                        binding(values::logDayTime, true)
                        controller(tickBox())
                    }

                    options.register("detect_night") {
                        name(Component.literal("Detect Night"))
                        description(
                            OptionDescription.of(
                                Component.literal("Decides if there should be a notification for when it turns night")
                            )
                        )
                        binding(values::logNightTime, true)
                        controller(tickBox())
                    }
                }

                groups.register("session_stats") {
                    name(Component.literal("Session Statistics"))
                    tooltip(Component.literal("Configuration for the Session Stats feature"))

                    options.register("tracking_mode") {
                        name(Component.literal("Stat Tracking Mode"))
                        description(OptionDescription.of(
                            Component.literal("Changes what the statistics feature tracks"),
                            Component.empty(),
                            Component.empty()
                                .append(Component.literal("Amounts mode").withBold(true))
                                .append(Component.literal(" tracks the amount of items you fish up. For example, if you caught 4 pearls in one catch, the number will increase by 4.")),
                            Component.empty()
                                .append(Component.literal("Catches mode").withBold(true))
                                .append(Component.literal(" tracks the amount of catches you do. For example, if you caught 4 pearls in one catch, the number will only increase by 1."))
                        ))
                        binding(values::sessionStatsTrackingMode, SessionStats.StatTrackingMode.AMOUNTS)
                        controller(enumDropdown<SessionStats.StatTrackingMode> {
                            Component.literal(it.configName)
                        })
                    }

                    options.register("pearl_tracking_mode") {
                        name(Component.literal("Pearl Tracking Mode"))
                        binding(values::sessionStatsPearlTrackingMode, SessionStats.PearlTrackingMode.CATCHES)
                        description(OptionDescription.of(
                            Component.literal("Changes what the pearl tracker is in relation to"),
                            Component.empty(),
                            Component.empty()
                                .append(Component.literal("Catches mode").withBold(true))
                                .append(Component.literal(" tracks the amount of total pearl catches, regardless of tier. If you fish up 4 pristines and 4 polished, it will increase by 8.")),
                            Component.empty(),
                            Component.empty()
                                .append(Component.literal("For each of the "))
                                .append(Component.literal("pearl tiers").withBold(true))
                                .append(Component.literal(", the mode tracks your pearls in relation to that tier. If you're on pristine mode and you fish up 4 pristines, 2 polished, and 3 rough, it will display 4.23 because a polished is 1/10th of a pristine, and a rough is 1/100th."))
                        ))
                        controller(enumDropdown {
                            Component.literal(it.configName)
                        })
                    }
                }
            }
        }.generateScreen(parent)
    }
}