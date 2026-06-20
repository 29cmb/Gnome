package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Items
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.util.getLore
import xyz.devcmb.gnome.util.isOnIsland
import kotlin.reflect.KMutableProperty0

class IslandCompletion : GnomeFeature {
    override val id: String = "island_completion"
    override val name: String = "Island Completion Display"
    override val description: OptionDescription = OptionDescription.of(Component.literal("Displays a checkmark over islands the player has fully completed"))
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::islandCompletionEnabled

    override fun init() {
    }

    override fun cleanup() {
    }

    fun renderSlot(graphics: GuiGraphicsExtractor, slot: Slot) {
        if(!Config.values.islandCompletionEnabled) return
        if(!isOnIsland()) return
        if(!slot.item.`is`(Items.ECHO_SHARD)) return

        val inventory = Minecraft.getInstance().screen as? ContainerScreen ?: return
        if(!inventory.title.string.contains("FISHING PROGRESS")) return

        val lore = slot.item.getLore()
        if(lore.any { it.string.contains("100%") } && lore.any { it.string.contains("Climate") }) {
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath("mcc", "textures/_fonts/icon/accept.png"),
                slot.x + 2, slot.y + 2,
                0f, 0f,
                16, 16,
                16, 16
            )
        }
    }
}