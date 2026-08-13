package xyz.devcmb.gnome

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import xyz.devcmb.gnome.data.Island
import xyz.devcmb.gnome.data.Weight
import xyz.devcmb.gnome.feature.SessionStats
import xyz.devcmb.gnome.mixin.accessor.GuiAccessor
import xyz.devcmb.gnome.util.Command
import xyz.devcmb.gnome.util.FishingSpotInfo
import xyz.devcmb.gnome.util.Font
import xyz.devcmb.gnome.util.appendNewLine
import xyz.devcmb.gnome.util.isOnFishing
import xyz.devcmb.gnome.util.isOnIsland
import xyz.devcmb.gnome.util.sendMessage
import xyz.devcmb.gnome.util.toRoundedString
import xyz.devcmb.gnome.util.withBold
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

                literal("summarize") {
                    executes { sessionStats.summarize() }
                }
            }

            literal("spot") {
                literal("info") {
                    executes {
                        if(!isOnIsland() || !isOnFishing() || Island.currentIsland == null) {
                            Minecraft.getInstance()
                                .sendMessage(Component.literal("You are not on MCC Island fishing!").withColor(0xff5555))
                            return@executes
                        }

                        val spot = FishingSpotInfo.currentSpot
                        if(spot == null) {
                            Minecraft.getInstance()
                                .sendMessage(Component.literal("You are not at a fishing spot!").withColor(0xff5555))
                            return@executes
                        }

                        var message = Component.empty()
                            .append(Component.literal("Fishing Spot")
                                .withBold(true)
                                .withColor(ChatFormatting.YELLOW.color!!)
                            )
                            .appendNewLine()
                            .append(Component.literal("Starting Stock: ")
                                .append(Component.literal(spot.stock.displayText).withColor(spot.stock.color))
                                .withColor(0xA8B0B0)
                            )
                            .appendNewLine()
                            .append(Component.literal("Island: ")
                                .append(Component.literal(spot.island.islandName)
                                    .withColor(ChatFormatting.GREEN.color!!))
                                .withColor(0xA8B0B0))
                            .appendNewLine()
                            .append(Component.literal("Location: ")
                                .append(Component.literal(spot.loc.toRoundedString())
                                    .withColor(ChatFormatting.AQUA.color!!))
                                .withColor(0xA8B0B0)
                            )

                        var formattedMessage = ""
                        spot.perks.forEach {
                            message = message.append(
                                Component.literal("\n").withColor(ChatFormatting.WHITE.color!!)
                                    .append(Font.getGlyph(it.first.icon))
                                    .append(Component.literal("+${it.second}${if(!it.first.numerical) "%" else ""} "))
                                    .append(Component.literal(it.first.displayText).withColor(it.first.type.color))
                            )
                            formattedMessage += "${it.first.copyFormat(it.second)} "
                        }
                        formattedMessage += "| ${spot.stock.formattedString} stock | i${spot.island.ordinal + 1} ${spot.loc.toRoundedString().replace(",","")}"

                        message = message.appendNewLine().appendNewLine().append(
                            Component.literal("[Click to Copy]")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN.color!!)
                                    .withClickEvent(
                                        ClickEvent.CopyToClipboard(formattedMessage)
                                    )
                                    .withHoverEvent(HoverEvent.ShowText(Component.literal(formattedMessage)))
                                )
                        )

                        Minecraft.getInstance().sendMessage(
                            message
                        )
                    }
                }
            }
        }.register(dispatcher)
    }
}