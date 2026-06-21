package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import net.minecraft.world.entity.HumanoidArm
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.Gnome
import xyz.devcmb.gnome.data.CatchType
import xyz.devcmb.gnome.data.PearlType
import xyz.devcmb.gnome.data.SpiritTier
import xyz.devcmb.gnome.data.TreasureTier
import xyz.devcmb.gnome.data.Weight
import xyz.devcmb.gnome.util.isOnFishing
import xyz.devcmb.gnome.util.isOnIsland
import xyz.devcmb.gnome.mixin.accessor.GuiAccessor
import xyz.devcmb.gnome.util.Font
import xyz.devcmb.gnome.util.appendNewLine
import xyz.devcmb.gnome.util.round2Places
import xyz.devcmb.gnome.util.roundPlaces
import xyz.devcmb.gnome.util.sendMessage
import xyz.devcmb.gnome.util.texture
import xyz.devcmb.gnome.util.withBold
import xyz.devcmb.gnome.util.withFont
import kotlin.reflect.KMutableProperty0

class SessionStats : GnomeFeature {
    override val id: String = "session_stats"
    override val name: String = "Session Statistics"
    override val description: OptionDescription = OptionDescription.of(
        Component.literal("Displays treasure, pearls, spirits, fish, and xp caught since you booted up the game")
    )
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::sessionStatsEnabled

    val xpRegex: Regex = Regex("You earned: (?<amount>[0-9]*) Island XP")

    val hasXPBoost: Boolean
        get() {
            val actionBar = (Minecraft.getInstance().gui as GuiAccessor).`gnome$getOverlayMessageString`()
                ?: Component.empty()

            return actionBar.string.contains(Font.getGlyphString("_fonts/icon/xp_bonus.png"))
                || actionBar.string.contains(Font.getGlyphString("_fonts/icon/xp_bonus_20.png"))
                || actionBar.string.contains(Font.getGlyphString("_fonts/icon/xp_bonus_50.png"))
        }

    val isRightSideOpen: Boolean
        get() {
            val player = Minecraft.getInstance().player
            val hand = player?.mainArm ?: HumanoidArm.RIGHT
            val offHandItem = player?.offhandItem
            return hand == HumanoidArm.RIGHT || offHandItem == null || offHandItem.isEmpty
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
        FishingStatTracker("fish", CatchType.FISH.regex, GenericTieredStatHandler(Weight::class.java) { result, stats ->
            val extractedAmount = result["amount"]?.value?.toIntOrNull() ?: 1
            val amount = Config.values.sessionStatsTrackingMode.calculate(extractedAmount)

            val extractedWeight = result["weight"]?.value ?: Weight.AVERAGE.glyph()
            val weight = Weight.entries.find { it.glyph() == extractedWeight } ?: Weight.AVERAGE

            stats[weight] = stats[weight]!! + amount
        }, (33 to 0)),
        FishingStatTracker("pearls", CatchType.PEARL.regex, object : FishingStatHandler<HashMap<PearlType, Int>> {
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

            override fun get(): HashMap<PearlType, Int> {
                return caughtPearls
            }

            override fun formatUIText(): MutableComponent {
                if(Config.values.sessionStatsPearlTrackingMode == PearlTrackingMode.CATCHES) {
                    return Component.literal(
                        Config.values.sessionStatsPrecisionMode.calculate(
                            caughtPearls.entries.sumOf { it.value }
                        )
                    )
                }

                val amount = caughtPearls
                    .map { it.key.calculate(it.value) }
                    .fold(0.0) { a, b -> a + b }


                return Component.literal(amount.round2Places())
            }

            override fun reset() {
                caughtPearls.replaceAll { _, _ -> 0 }
            }
        }, (75 to 0)),
        FishingStatTracker("treasure", CatchType.TREASURE.regex, GenericTieredStatHandler(TreasureTier::class.java) { result, stats ->
            val extractedAmount = result["amount"]?.value?.toIntOrNull() ?: 1
            val amount = Config.values.sessionStatsTrackingMode.calculate(extractedAmount)

            val extractedTier = result["tier"]?.value ?: TreasureTier.COMMON.match
            val tier = TreasureTier.entries.find { it.match == extractedTier } ?: TreasureTier.COMMON

            stats[tier] = stats[tier]!! + amount
        }, (132 to 0)),
        FishingStatTracker("spirits", CatchType.SPIRIT.regex, GenericTieredStatHandler(SpiritTier::class.java) { result, stats ->
            val extractedAmount = result["amount"]?.value?.toIntOrNull() ?: 1
            val amount = Config.values.sessionStatsTrackingMode.calculate(extractedAmount)

            val extractedTier = result["tier"]?.value ?: SpiritTier.NORMAL.match
            val tier = SpiritTier.entries.find { it.match == extractedTier } ?: SpiritTier.NORMAL

            stats[tier] = stats[tier]!! + amount
        }, (173 to 0)),
        FishingStatTracker("xp", xpRegex, object : FishingStatHandler<Int> {
            var xp: Int = 0
            override fun handle(result: MatchGroupCollection) {
                val amount = result["amount"]?.value?.toInt() ?: 0
                xp += amount
            }

            override fun get(): Int {
                return xp
            }

            override fun formatUIText(): MutableComponent {
                return Component.literal(Config.values.sessionStatsPrecisionMode.calculate(xp))
            }

            override fun reset() {
                xp = 0
            }
        }) {
            (if(isRightSideOpen) 215 else 243) to (if (hasXPBoost) 30 else 43)
        }
    )

    @Suppress("unchecked_cast")
    inline fun <reified T> getTrackerHandler(id: String): FishingStatHandler<T> {
        val tracker = trackers.find { t -> t.id == id }!!
        return tracker.handler as FishingStatHandler<T>
    }

    fun onChatMessage(component: Component) {
        if(!component.string.contains("You caught:", ignoreCase = true)) return
        trackers.forEach { tracker ->
            val result = tracker.regex().find(component.string)?.groups ?: return@forEach
            tracker.handler.handle(result)
        }
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

            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath(Gnome.MOD_ID, "textures/gui/xp_amount.png"),
                ((graphics.guiWidth() - 180) / 2) + (if(isRightSideOpen) 182 else 211), graphics.guiHeight() - (if(hasXPBoost) 30 else 17),
                0f, 0f,
                39, 16,
                39, 16
            )

            trackers.forEach {
                it.render(graphics)
            }
        }
    }

    @Suppress("unchecked_cast")
    fun summarize() {
        val pearls = getTrackerHandler<HashMap<PearlType, Int>>("pearls")
        val fish = getTrackerHandler<HashMap<Weight, Int>>("fish")
        val treasure = getTrackerHandler<HashMap<TreasureTier, Int>>("treasure")
        val spirits = getTrackerHandler<HashMap<SpiritTier, Int>>("spirits")

        var message = Component.empty()
            .append(Component.literal(" ".repeat(70)).withColor(0x00FF00).withStyle(Style.EMPTY.withStrikethrough(true)))
            .appendNewLine()
            .append(Component.literal("Session Summary").withColor(0x66FF66).withBold(true))
            .appendNewLine()
            .appendNewLine()

        fish.get().toSortedMap(compareBy { it.ordinal }).forEach { (weight, amount) ->
            message = message.append(
                weight.icon()
            ).append(Component.literal(" $amount "))
        }

        message = message.appendNewLine()

        pearls.get().toSortedMap(compareBy { it.pristinePart }).forEach { (type, amount) ->
            message = message.append(
                texture(type.resource)
            ).append(Component.literal(" $amount "))
        }

        message = message.appendNewLine()

        treasure.get().toSortedMap(compareBy { it.ordinal }).forEach { (type, amount) ->
            message = message.append(
                texture(type.resource)
            ).append(Component.literal(" $amount "))
        }

        message = message.appendNewLine()

        spirits.get().toSortedMap(compareBy { it.ordinal }).forEach { (type, amount) ->
            message = message.append(
                texture(type.resource)
            ).append(Component.literal(" $amount "))
        }

        message = message.appendNewLine().append(Component.literal(" ".repeat(70)).withColor(0x00FF00).withStyle(Style.EMPTY.withStrikethrough(true)))

        Minecraft.getInstance().sendMessage(message)
    }

    @Suppress("unused")
    enum class StatTrackingMode(val configName: String, val calculate: (amount: Int) -> Int) {
        CATCHES("Catches", { 1 }),
        AMOUNTS("Amounts", { it })
    }

    @Suppress("unused")
    enum class StatPrecisionMode(val configName: String, val calculate: (amount: Int) -> String) {
        PRECISE("Precise", { it.toString() }),
        ROUNDED("Rounded", {
            when {
                it >= 1_000_000 -> "${(it / 1_000_000.0).roundPlaces(1)}M"
                it >= 1_000 -> "${(it / 1_000.0).roundPlaces(1)}K"
                else -> it.toString()
            }
        })
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
        val handler: FishingStatHandler<*>,
        val uiOffset: () -> Pair<Int, Int>,
    ) {
        constructor(id: String, regex: Regex, handler: FishingStatHandler<*>, uiOffset: () -> Pair<Int, Int>)
            : this(id, { regex }, handler, uiOffset)

        constructor(id: String, regex: () -> Regex, handler: FishingStatHandler<*>, uiOffset: Pair<Int, Int>)
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

    class GenericTieredStatHandler<T : Enum<T>>(
        enumClass: Class<T>,
        val handle: (result: MatchGroupCollection, stats: HashMap<T, Int>) -> Unit
    ) : FishingStatHandler<HashMap<T, Int>> {
        private val stats: HashMap<T, Int> = HashMap(enumClass.enumConstants.associateWith { 0 })

        override fun handle(result: MatchGroupCollection) {
            handle.invoke(result, stats)
        }

        override fun get(): HashMap<T, Int> {
            return stats
        }

        override fun reset() {
            stats.replaceAll { _, _ -> 0 }
        }

        override fun formatUIText(): MutableComponent {
            return Component.literal(Config.values.sessionStatsPrecisionMode.calculate(stats.values.sumOf { it }))
        }
    }

    interface FishingStatHandler<T> {
        fun handle(result: MatchGroupCollection)
        fun get(): T
        fun reset()
        fun formatUIText(): MutableComponent
    }
}