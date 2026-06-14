package xyz.devcmb.gnome.feature

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import xyz.devcmb.gnome.Gnome
import xyz.devcmb.gnome.withFont

class SessionStats : GnomeFeature {
    var caughtFish: Int = 0
    var caughtPearls: Int = 0
    var caughtTreasure: Int = 0
    var caughtSpirits: Int = 0

    val fishRegex: Regex = Regex("You caught: \\[[A-z, ]*[\uE0D6\uE0D0\uE0D8\uE0CF].*] *(?:x(?<amount>[0-9]*))?")
    val pearlRegex: Regex = Regex("You caught: \\[[A-z]* Pearl] *(?:x(?<amount>[0-9]*))?")
    val spiritRegex: Regex = Regex("You caught: \\[[A-z, ]* Spirit] *(?:x(?<amount>[0-9]*))?")
    val treasureRegex: Regex = Regex("You caught: \\[[A-z]* Treasure] *(?:x(?<amount>[0-9]*))?")

    override fun init() {
        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "fishing_session_stats"), statsLayer())

        ClientReceiveMessageEvents.GAME.register { component, _ ->
            val regexValues = arrayListOf(
                ::caughtFish to fishRegex,
                ::caughtPearls to pearlRegex,
                ::caughtTreasure to treasureRegex,
                ::caughtSpirits to spiritRegex,
            )

            regexValues.forEach { (field, regex) ->
                val result = regex.find(component.string)?.groups ?: return@forEach
                val amount = result["amount"]?.value?.toIntOrNull() ?: 1
                field.set(field.get() + amount)
            }
        }
    }

    override fun cleanup() {
    }

    fun statsLayer(): HudElement {
        return { graphics, _ ->
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "textures/gui/fishing_session_stats.png"),
                ((graphics.guiWidth() - 180) / 2) - 1, graphics.guiHeight() - 60,
                0f, 0f,
                180, 16,
                180, 16
            )

            val values: ArrayList<Pair<Int, Int>> = arrayListOf(
                caughtFish to 33,
                caughtPearls to 75,
                caughtTreasure to 132,
                caughtSpirits to 173
            )

            values.forEach { (amount, offset) ->
                val component = Component.literal(amount.toString()).withFont(Identifier.fromNamespaceAndPath("mcc", "hud"))
                val fontOffset = (Minecraft.getInstance().font.width(component) -
                    Minecraft.getInstance().font.width(Component.literal(amount.toString().takeLast(1)).withFont(Identifier.fromNamespaceAndPath("mcc", "hud")))
                )
                graphics.text(
                    Minecraft.getInstance().font,
                    component,
                    (((graphics.guiWidth() - 180) / 2) + offset) - fontOffset,
                    graphics.guiHeight() - 57,
                    ARGB.opaque(0xFFFFFF)
                )
            }
        }
    }
}