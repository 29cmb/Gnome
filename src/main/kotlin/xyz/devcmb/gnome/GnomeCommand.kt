package xyz.devcmb.gnome

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import xyz.devcmb.gnome.feature.SessionStats

object GnomeCommand {
    val sessionStats by lazy {
        Gnome.getFeature<SessionStats>()
    }

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        Command("gnome") {
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
                            if(tracker == null) {
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