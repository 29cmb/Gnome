package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import net.minecraft.world.entity.HumanoidArm
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.Gnome
import xyz.devcmb.gnome.data.PearlType
import xyz.devcmb.gnome.data.Weight
import xyz.devcmb.gnome.util.isOnFishing
import xyz.devcmb.gnome.util.isOnIsland
import xyz.devcmb.gnome.mixin.accessor.GuiAccessor
import xyz.devcmb.gnome.util.Font
import xyz.devcmb.gnome.util.round2Places
import xyz.devcmb.gnome.util.withFont
import kotlin.reflect.KMutableProperty0

class SessionStats : GnomeFeature {
    override val id: String = "session_stats"
    override val name: String = "Session Statistics"
    override val description: OptionDescription = OptionDescription.of(
        Component.literal("Displays treasure, pearls, spirits, fish, and xp caught since you booted up the game")
    )
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::sessionStatsEnabled

    val pearlRegex: Regex = Regex("You caught: \\[(?<type>[A-z]*) Pearl] *(?:x(?<amount>[0-9]*))?")
    val spiritRegex: Regex = Regex("You caught: \\[[A-z, ]* Spirit] *(?:x(?<amount>[0-9]*))?")
    val treasureRegex: Regex = Regex("You caught: \\[[A-z]* Treasure] *(?:x(?<amount>[0-9]*))?")
    val xpRegex: Regex = Regex("You earned: (?<amount>[0-9]*) Island XP")

    val hasXPBoost: Boolean
        get() {
            val actionBar = (Minecraft.getInstance().gui as GuiAccessor).`gnome$getOverlayMessageString`()
                ?: Component.empty()

            return actionBar.string.contains(Font.getGlyphString("_fonts/icon/xp_bonus.png"))
                || actionBar.string.contains(Font.getGlyphString("_fonts/icon/xp_bonus_20.png"))
                || actionBar.string.contains(Font.getGlyphString("_fonts/icon/xp_bonus_50.png"))
        }

    override fun init() {
        HudElementRegistry.addFirst(
            Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "fishing_session_stats"),
            hotbarSessionStatsLayer()
        )
    }

    // I wanted to replace this with noxesium stats, but theres a problem
    // The fishing stat double-counts when glitched rod is triggered
    // That and i'd have to rewrite even more of this logic
    val trackers: ArrayList<FishingStatTracker> = arrayListOf(
        // its in here to prevent a NPE since the font isn't loaded yet
        FishingStatTracker("fish", {
            Regex("You caught: \\[[A-z, ]*[" +
                Weight.entries.joinToString("") { it.glyph() } +
            "].*] *(?:x(?<amount>[0-9]*))?")
        }, GenericFishingStatHandler(), (33 to 0)),

        FishingStatTracker("pearls", pearlRegex, object : FishingStatHandler {
            val caughtPearls: HashMap<PearlType, Int> = hashMapOf(
                PearlType.ROUGH to 0,
                PearlType.POLISHED to 0,
                PearlType.PRISTINE to 0
            )

            override fun handle(result: MatchGroupCollection) {
                val extractedAmount = result["amount"]?.value?.toIntOrNull() ?: 1
                val amount = Config.values.sessionStatsTrackingMode.calculate(extractedAmount)

                val resultType = result["type"].toString()
                val pearlType = PearlType.entries.find { resultType.contains(it.match) } ?: return

                caughtPearls[pearlType] = caughtPearls[pearlType]!! + amount
            }

            override fun formatUIText(): MutableComponent {
                val amount = caughtPearls
                    .map { it.key.calculate(it.value) }
                    .fold(0.0) { a, b -> a + b }


                return Component.literal(amount.round2Places())
            }

            override fun reset() {
                caughtPearls.replaceAll { _, _ -> 0 }
            }
        }, (75 to 0)),
        FishingStatTracker("treasure", treasureRegex, GenericFishingStatHandler(), (132 to 0)),
        FishingStatTracker("spirits", spiritRegex, GenericFishingStatHandler(), (173 to 0)),
        FishingStatTracker("xp", xpRegex, object : FishingStatHandler {
            var xp: Int = 0
            override fun handle(result: MatchGroupCollection) {
                val amount = result["amount"]?.value?.toInt() ?: 0
                xp += amount
            }

            override fun formatUIText(): MutableComponent {
                return Component.literal(xp.toString())
            }

            override fun reset() {
                xp = 0
            }
        }) {
            val hand = Minecraft.getInstance().player?.mainArm?.opposite ?: HumanoidArm.RIGHT
            (if(hand == HumanoidArm.LEFT) 215 else 243) to (if (hasXPBoost) 30 else 43)
        }
    )

    fun onChatMessage(component: Component) {
        trackers.forEach { tracker ->
            val result = tracker.regex().find(component.string)?.groups ?: return@forEach
            tracker.handler.handle(result)
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

            val hand = Minecraft.getInstance().player?.mainArm?.opposite ?: HumanoidArm.RIGHT
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "textures/gui/xp_amount.png"),
                ((graphics.guiWidth() - 180) / 2) + (if(hand == HumanoidArm.LEFT) 182 else 211), graphics.guiHeight() - (if(hasXPBoost) 30 else 17),
                0f, 0f,
                39, 16,
                39, 16
            )

            trackers.forEach {
                it.render(graphics)
            }
        }
    }

    @Suppress("unused")
    enum class StatTrackingMode(val configName: String, val calculate: (amount: Int) -> Int) {
        CATCHES("Catches", { 1 }),
        AMOUNTS("Amounts", { it })
    }

    @Suppress("unused")
    enum class PearlTrackingMode(
        val configName: String,
        val calculate: (type: PearlType, amount: Int) -> Double
    ) {
        PRISTINES("Pristine", { type, amount ->
            type.pristinePart * amount
        }),
        POLISHED("Polished", { type, amount ->
            type.pristinePart * 10 * amount
        }),
        ROUGH("Rough", { type, amount ->
            type.pristinePart * 100 * amount
        }),
        CATCHES("Catches", { _, amount -> amount.toDouble() })
    }

    class FishingStatTracker(
        val id: String,
        val regex: () -> Regex,
        val handler: FishingStatHandler,
        val uiOffset: () -> Pair<Int, Int>,
    ) {
        constructor(id: String, regex: Regex, handler: FishingStatHandler, uiOffset: Pair<Int, Int>)
            : this(id, { regex }, handler, { uiOffset })

        constructor(id: String, regex: Regex, handler: FishingStatHandler, uiOffset: () -> Pair<Int, Int>)
            : this(id, { regex }, handler, uiOffset)

        constructor(id: String, regex: () -> Regex, handler: FishingStatHandler, uiOffset: Pair<Int, Int>)
            : this(id, regex, handler, { uiOffset })

        fun render(graphics: GuiGraphicsExtractor) {
            val text = handler.formatUIText()
            val component = text.withFont(Identifier.fromNamespaceAndPath("mcc", "hud"))
            val fontOffset = (Minecraft.getInstance().font.width(component) -
                Minecraft.getInstance().font.width(
                    Component.literal(component.string.takeLast(1))
                        .withFont(Identifier.fromNamespaceAndPath("mcc", "hud"))
                )
            )

            val offset = uiOffset()

            graphics.text(
                Minecraft.getInstance().font,
                component,
                (((graphics.guiWidth() - 180) / 2) + offset.first) - fontOffset,
                graphics.guiHeight() - 57 + offset.second,
                ARGB.opaque(0xFFFFFF)
            )
        }
    }

    class GenericFishingStatHandler : FishingStatHandler {
        var value: Int = 0
        override fun handle(result: MatchGroupCollection) {
            val extractedAmount = result["amount"]?.value?.toIntOrNull() ?: 1
            val amount = Config.values.sessionStatsTrackingMode.calculate(extractedAmount)

            value += amount
        }

        override fun reset() {
            value = 0
        }

        override fun formatUIText(): MutableComponent {
            return Component.literal(value.toString())
        }
    }

    interface FishingStatHandler {
        fun handle(result: MatchGroupCollection)
        fun reset()
        fun formatUIText(): MutableComponent
    }
}