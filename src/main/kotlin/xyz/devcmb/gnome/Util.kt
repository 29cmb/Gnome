package xyz.devcmb.gnome

import net.minecraft.client.Minecraft

// https://github.com/pe3ep/Trident/blob/master/src/main/kotlin/cc/pe3epwithyou/trident/state/MCCIState.kt
fun isOnIsland(): Boolean {
    val server = Minecraft.getInstance().currentServer ?: return false
    return server.ip.contains("mccisland.net", true)
}