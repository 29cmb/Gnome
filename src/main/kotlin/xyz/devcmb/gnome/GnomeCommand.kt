package xyz.devcmb.gnome

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import xyz.devcmb.gnome.feature.SessionStats

object GnomeCommand {
    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        Command("gnome") {
            literal("session") {
                literal("reset") {
                    executes {
                        val feature = Gnome.getFeature<SessionStats>()
                        feature.trackers.forEach { it.handler.reset() }

                        Minecraft.getInstance().sendMessage(
                            Component.literal("Reset session stats successfully!").withColor(0x2bfb6f)
                        )
                    }
                }
            }
        }.register(dispatcher)
    }
}