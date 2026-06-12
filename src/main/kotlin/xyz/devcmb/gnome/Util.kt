package xyz.devcmb.gnome

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier

// https://github.com/pe3ep/Trident/blob/master/src/main/kotlin/cc/pe3epwithyou/trident/state/MCCIState.kt
fun isOnIsland(): Boolean {
    val server = Minecraft.getInstance().currentServer ?: return false
    return server.ip.contains("mccisland.net", true)
}

fun MutableComponent.withFont(identifier: Identifier)
    = this.withStyle(Style.EMPTY.withFont(FontDescription.Resource(identifier)))

fun Minecraft.sendMessage(message: Component) = this.gui.chat.addClientSystemMessage(message)