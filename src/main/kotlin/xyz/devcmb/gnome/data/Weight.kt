package xyz.devcmb.gnome.data

import net.minecraft.network.chat.Component
import xyz.devcmb.gnome.util.Font

enum class Weight(val glyph: String, val newFishGlyph: String) {
    AVERAGE(
        "_fonts/icon/fishing/average_bubble.png",
        "_fonts/callouts/fishing_discovery/blue/18.png"
    ),
    LARGE(
        "_fonts/icon/fishing/large_bubble.png",
        "_fonts/callouts/fishing_discovery/purple/18.png"
    ),
    MASSIVE(
        "_fonts/icon/fishing/massive_bubble.png",
        "_fonts/callouts/fishing_discovery/orange/18.png"
    ),
    GARGANTUAN(
        "_fonts/icon/fishing/gargantuan_bubble.png",
        "_fonts/callouts/fishing_discovery/red/18.png"
    );

    fun glyph(): String {
        return Font.getGlyphString(glyph)
    }

    fun newFishGlyph(): String {
        return Font.getGlyphString(newFishGlyph)
    }

    fun icon(): Component {
        return Font.getGlyph(glyph)
    }

    fun getCompletion(island: Island): Int {
        val completion = island.getCompletionData()
        return when(this) {
            AVERAGE -> completion.average
            LARGE -> completion.large
            MASSIVE -> completion.massive
            GARGANTUAN -> completion.gargantuan
        }
    }
}