package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.data.Island
import xyz.devcmb.gnome.data.Weight
import xyz.devcmb.gnome.mixin.accessor.GuiAccessor
import xyz.devcmb.gnome.util.getFirstLoreMatch
import xyz.devcmb.gnome.util.isOnFishing
import xyz.devcmb.gnome.util.isOnIsland
import xyz.devcmb.gnome.util.withFont
import java.time.Instant
import kotlin.reflect.KMutableProperty0

class IslandFishTracker : GnomeFeature {
    override val id: String = "island_fish_tracker"
    override val name: String = "Island Fish Tracker"
    override val description: OptionDescription = OptionDescription.of(
        Component.literal("Show your fish progress on a given island on the scoreboard")
    )
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::islandFishTrackerEnabled

    var lastFishDiscovery: Long = 0

    override fun init() {
        ClientTickEvents.END_CLIENT_TICK.register {
            if(!isOnIsland() || !isOnFishing() || lastFishDiscovery + 7 > Instant.now().epochSecond) return@register
            val actionBar = (Minecraft.getInstance().gui as GuiAccessor).`gnome$getOverlayMessageString`() ?: return@register

            Weight.entries.forEach {
                if(actionBar.string.contains(it.newFishGlyph())) {
                    val currentIsland = Island.currentIsland ?: return@forEach
                    currentIsland.discoverNewFish(it)
                    lastFishDiscovery = Instant.now().epochSecond
                }
            }
        }
    }

    override fun cleanup() {
    }

    fun addScoreboardEntries(): List<Component> {
        if(!Config.values.islandFishTrackerEnabled) return emptyList()

        val island = Island.currentIsland ?: return emptyList()

        var remainingWeights: MutableComponent = Component.empty()
        Weight.entries.forEach {
            val completed = it.getCompletion(island)
            remainingWeights = remainingWeights.append(" ").append(it.icon()).append(completed.toString())
        }

        val missingData = Weight.entries.any { it.getCompletion(island) == -1 }
        if(missingData) {
            remainingWeights = Component.literal(" Data Missing!").withColor(0xFF6655)
        }

        val entries = arrayListOf(
            Component.empty(),
            Component.literal(" REMAINING FISH:").withFont(Identifier.fromNamespaceAndPath("mcc", "hud")).withColor(0xFFFF00),
            remainingWeights
        )
        if(missingData) {
            entries.add(Component.literal(" Open ANGLR Panel").withColor(0xFF6655))
        }

        return entries
    }

    fun updateIslandCompletionState(items: List<ItemStack>) {
        items.forEach { item ->
            if(!item.`is`(Items.ECHO_SHARD)) return@forEach

            val island = Island.entries.find { item.itemName.string == it.islandName } ?: return@forEach
            island.updateCompletionData(
                Config.CompletionData(
                    getCompletion(item, Regex("Average - (?<count>[0-9]*.)/18")),
                    getCompletion(item, Regex("Large - (?<count>[0-9]*.)/18")),
                    getCompletion(item, Regex("Massive - (?<count>[0-9]*.)/18")),
                    getCompletion(item, Regex("Gargantuan - (?<count>[0-9]*.)/18")),
                )
            )
        }

        Config.handler.save()
    }

    fun getCompletion(item: ItemStack, regex: Regex): Int {
        return 18 - (item.getFirstLoreMatch(regex)?.get("count")?.value?.toIntOrNull() ?: 19)
    }
}