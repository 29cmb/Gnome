package xyz.devcmb.gnome.util

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Display
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import xyz.devcmb.gnome.data.Island
import xyz.devcmb.gnome.data.SpotPerk
import xyz.devcmb.gnome.data.StockLevel

object FishingSpotInfo {
    val amountRegex = Regex("\\+(?<amount>[0-9]+)")
    val spotCache: ArrayList<FishingSpot> = ArrayList()

    var currentSpot: FishingSpot? = null

    fun handleCaching() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val bobber = client.player?.fishing
            if(bobber == null) {
                currentSpot = null
                return@register
            }

            val display = registerCurrentSpot() ?: return@register
            val spot = spotCache.find { it.display == display } ?: return@register
            currentSpot = spot
        }
    }

    fun registerCurrentSpot(): Display.TextDisplay? {
        val mc = Minecraft.getInstance()
        val bobber = mc.player?.fishing ?: return null

        // numbers from https://github.com/pe3ep/Trident/blob/master/src/main/kotlin/cc/pe3epwithyou/trident/client/listeners/FishingSpotListener.kt
        // blame pe3ep if they look arbitrary, not me!!!
        val display = mc.level
            ?.getEntities(null, AABB.ofSize(bobber.position(), 3.5, 6.0, 3.5))
            ?.filterIsInstance<Display.TextDisplay>()
            ?.firstOrNull()
            ?: return null

        val lines = display.text.string.split("\n")
        if(!lines.any { it == "Fishing Spot" }) return null

        if(spotCache.any { it.display == display }) return display

        val stock = StockLevel.entries.find {
            lines.getOrNull(2)?.contains(it.displayText) ?: false
        } ?: return null
        val perkLines = lines.drop(4)


        val perks = perkLines.mapNotNull { perkLine ->
            val perkType = SpotPerk.entries.find { perkLine.contains(it.displayText) } ?: return@mapNotNull null
            val amount = amountRegex.find(perkLine)?.groups["amount"]?.value?.toIntOrNull() ?: return@mapNotNull null

            perkType to amount
        }

        spotCache.add(FishingSpot(
            display,
            bobber.position(),
            Island.currentIsland ?: Island.UNKNOWN,
            stock,
            perks
        ))

        return display
    }

    data class FishingSpot(
        val display: Display.TextDisplay,
        val loc: Vec3,

        val island: Island,
        val stock: StockLevel,
        val perks: List<Pair<SpotPerk, Int>>
    )
}