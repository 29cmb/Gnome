package xyz.devcmb.gnome.feature

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import xyz.devcmb.gnome.Config
import xyz.devcmb.gnome.Gnome
import xyz.devcmb.gnome.isOnFishing
import xyz.devcmb.gnome.isOnIsland
import xyz.devcmb.gnome.mixin.GuiAccessor
import xyz.devcmb.gnome.withFont

class SessionStats : GnomeFeature {
    var caughtFish: Int = 0
    var caughtPearls: Int = 0
    var caughtTreasure: Int = 0
    var caughtSpirits: Int = 0

    var sessionXP: Int = 0

    val fishRegex: Regex = Regex("You caught: \\[[A-z, ]*[\uE0D6\uE0D0\uE0D8\uE0CF].*] *(?:x(?<amount>[0-9]*))?")
    val pearlRegex: Regex = Regex("You caught: \\[[A-z]* Pearl] *(?:x(?<amount>[0-9]*))?")
    val spiritRegex: Regex = Regex("You caught: \\[[A-z, ]* Spirit] *(?:x(?<amount>[0-9]*))?")
    val treasureRegex: Regex = Regex("You caught: \\[[A-z]* Treasure] *(?:x(?<amount>[0-9]*))?")
    val xpRegex: Regex = Regex("You earned: (?<amount>[0-9]*) Island XP")

    override fun init() {
        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "fishing_session_stats"), hotbarSessionStatsLayer())

        ClientReceiveMessageEvents.GAME.register { component, _ ->
            // even if its disabled, we can still track these stats in the background
            val regexValues = arrayListOf(
                ::caughtFish to fishRegex,
                ::caughtPearls to pearlRegex,
                ::caughtTreasure to treasureRegex,
                ::caughtSpirits to spiritRegex,
                ::sessionXP to xpRegex
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

    fun hotbarSessionStatsLayer(): HudElement {
        return element@{ graphics, _ ->
            if(!isOnIsland() || !isOnFishing() || !Config.values.sessionStatsEnabled) return@element

            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "textures/gui/fishing_session_stats.png"),
                ((graphics.guiWidth() - 180) / 2) - 1, graphics.guiHeight() - 60,
                0f, 0f,
                181, 16,
                181, 16
            )

            val actionBar = (Minecraft.getInstance().gui as GuiAccessor).`gnome$getOverlayMessageString`() ?: Component.empty()
            val hasXPBoost =
                actionBar.string.contains("\uE391")
                || actionBar.string.contains("\uE392")
                || actionBar.string.contains("\uE393")

            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "textures/gui/xp_amount.png"),
                ((graphics.guiWidth() - 180) / 2) + 182, graphics.guiHeight() - (if(hasXPBoost) 30 else 17),
                0f, 0f,
                39, 16,
                39, 16
            )

            val values: ArrayList<Pair<Int, Pair<Int, Int>>> = arrayListOf(
                caughtFish to (33 to 0),
                caughtPearls to (75 to 0),
                caughtTreasure to (132 to 0),
                caughtSpirits to (172 to 0),
                sessionXP to (215 to if(hasXPBoost) 30 else 43)
            )

            values.forEach { (amount, offset) ->
                val component = Component.literal(amount.toString()).withFont(Identifier.fromNamespaceAndPath("mcc", "hud"))
                val fontOffset = (Minecraft.getInstance().font.width(component) -
                    Minecraft.getInstance().font.width(Component.literal(amount.toString().takeLast(1)).withFont(Identifier.fromNamespaceAndPath("mcc", "hud")))
                )
                graphics.text(
                    Minecraft.getInstance().font,
                    component,
                    (((graphics.guiWidth() - 180) / 2) + offset.first) - fontOffset,
                    graphics.guiHeight() - 57 + offset.second,
                    ARGB.opaque(0xFFFFFF)
                )
            }
        }
    }
}