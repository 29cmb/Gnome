package xyz.devcmb.gnome

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import xyz.devcmb.gnome.data.Weight
import xyz.devcmb.gnome.feature.SessionStats
import xyz.devcmb.gnome.mixin.accessor.GuiAccessor
import xyz.devcmb.gnome.util.Command
import xyz.devcmb.gnome.util.sendMessage
import xyz.devcmb.gnome.util.withFont

object GnomeCommand {
    val sessionStats by lazy {
        Gnome.getFeature<SessionStats>()
    }

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        Command("gnome") {
            literal("debug") {
                literal("simulate_discovery") {
                    argument("weight") {
                        suggests { _, builder ->
                            Weight.entries.forEach { builder.suggest(it.name.lowercase()) }
                            builder.buildFuture()
                        }

                        executes {
                            val weightArg = it.getArgument("weight", String::class.java)
                            val weight = Weight.entries
                                .find { entry -> entry.name.equals(weightArg, true) }

                            if(weight == null) {
                                Minecraft.getInstance().sendMessage(
                                    Component.literal("Invalid weight!").withColor(0x2bfb6f)
                                )
                                return@executes
                            }

                            (Minecraft.getInstance().gui as GuiAccessor).`gnome$setOverlayMessageString`(
                                Component.literal(weight.newFishGlyph()).withFont(Identifier.fromNamespaceAndPath("mcc", "callouts"))
                            )
                        }
                    }
                }

                literal("boost_session_stats") {
                    executes {
                        (sessionStats.trackers.filter { it.handler is SessionStats.GenericFishingStatHandler }).forEach {
                            (it.handler as SessionStats.GenericFishingStatHandler).value =
                                if ((0..1).random() == 0) 123_456 else 12_345_678
                        }
                    }
                }
            }

            literal("session") {
                literal("reset") {
                    executes {
                        sessionStats.trackers.forEach { it.handler.reset() }

                        Minecraft.getInstance().sendMessage(
                            Component.literal("Reset session stats successfully!").withColor(0x2bfb6f)
                        )
                    }

                    argument("stat") {
                        suggests { _, builder ->
                            sessionStats.trackers.forEach { builder.suggest(it.id) }
                            builder.buildFuture()
                        }

                        executes {
                            val stat = it.getArgument("stat", String::class.java)
                            val tracker = sessionStats.trackers.find { tracker -> tracker.id == stat }
                            if (tracker == null) {
                                Minecraft.getInstance()
                                    .sendMessage(Component.literal("Invalid stat name!").withColor(0xff5555))
                                return@executes
                            }

                            tracker.handler.reset()
                            Minecraft.getInstance().sendMessage(
                                Component.literal("Reset session stat $stat successfully!").withColor(0x2bfb6f)
                            )
                        }
                    }
                }
            }
        }.register(dispatcher)
    }
}